package com.example.service.admin;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.csv.CSVPrinter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.example.dto.admin.AdminFileDto;
import com.example.dto.admin.AdminOrderDetailDto;
import com.example.dto.admin.AdminOrderDetailItemDto;
import com.example.entity.Order;
import com.example.entity.OrderItem;
import com.example.entity.User;
import com.example.enums.order.PaymentStatus;
import com.example.enums.order.ShippingStatus;
import com.example.mapper.OrderMapper;
import com.example.mapper.ProductMapper;
import com.example.mapper.UserMapper;
import com.example.mapper.admin.AdminOrderMapper;
import com.example.request.admin.AdminOrderEditRequest;
import com.example.service.admin.AdminOrderService.DeliveryNoteRow;
import com.example.service.admin.AdminOrderService.DeliveryNoteView;
import com.example.service.admin.AdminOrderService.ReceiptView;
import com.example.support.MailGateway;
import com.example.util.OrderUtil;
import com.openhtmltopdf.extend.FSSupplier;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

@ExtendWith(MockitoExtension.class)
class AdminOrderServiceTest {

    @InjectMocks
    AdminOrderService adminOrderService;

    @Mock
    AdminOrderMapper adminOrderMapper;

    @Mock
    OrderMapper orderMapper;

    @Mock
    UserMapper userMapper;

    @Mock
    ProductMapper productMapper;

    @Mock
    MailGateway gateway;

    @Mock
    SpringTemplateEngine templateEngine;

    @Nested
    class editOrder {
        String orderId = "id";

        String userId = "user";

        @Test
        void editOrder_reduceAndDelete() {
            Order o = new Order() {
                {
                    setOrderId(orderId);
                    setOrderNumber(20);
                    setUserId(userId);
                    setPaymentStatus(PaymentStatus.UNPAID);
                    setShippingStatus(ShippingStatus.UNSHIPPED);
                }
            };
            doReturn(o).when(orderMapper).selectOrderByPrimaryKey(orderId);

            OrderItem i1 = item(orderId, "p1", 5, 1000);
            OrderItem i2 = item(orderId, "p2", 3, 2000);
            OrderItem i3 = item(orderId, "p3", 1, 500);
            OrderItem i4 = item(orderId, "p4", 4, 800);
            OrderItem i5 = item(orderId, "p5", 1, 100);

            // For the updated items after quantity reduction and deletion
            OrderItem i1Updated = item(orderId, "p1", 3, 1000);
            OrderItem i2Updated = item(orderId, "p2", 1, 2000);
            OrderItem i3Updated = item(orderId, "p3", 1, 500);

            // Mock the three calls to selectOrderItems in order
            when(orderMapper.selectOrderItems(orderId))
                    .thenReturn(List.of(i1, i2, i3, i4, i5)) // First call in prepareContext
                    .thenReturn(List.of(i1Updated, i2Updated, i3Updated)) // Second call for totals
                    .thenReturn(List.of(i1Updated, i2Updated, i3Updated)); // Third call for email

            User user = new User();
            user.setEmail("user@example.com");
            when(userMapper.selectUserByPrimaryKey(userId)).thenReturn(user);

            AdminOrderEditRequest req = new AdminOrderEditRequest() {
                {
                    setItems(Map.of("p1", 3, "p2", 1)); // Only items with quantity changes
                    setDeleted(List.of("p4", "p5"));
                }
            };

            adminOrderService.editOrder(orderId, req);

            verify(productMapper).increaseStock("p1", 2, null);
            verify(productMapper).increaseStock("p2", 2, null);
            verify(productMapper).increaseStock("p4", 4, null);
            verify(productMapper).increaseStock("p5", 1, null);
            verify(productMapper, times(4)).increaseStock(anyString(), anyInt(), any());

            verify(orderMapper).deleteOrderItem(orderId, "p4");
            verify(orderMapper).deleteOrderItem(orderId, "p5");
            verify(orderMapper, times(2)).deleteOrderItem(anyString(), anyString());

            ArgumentCaptor<OrderItem> cap = ArgumentCaptor.forClass(OrderItem.class);
            verify(orderMapper, times(2)).updateItemQty(cap.capture());
            List<OrderItem> updated = cap.getAllValues();
            assertThat(updated).extracting(
                    OrderItem::getOrderId,
                    OrderItem::getProductId,
                    OrderItem::getQty)
                    .containsExactlyInAnyOrder(
                            tuple(
                                    orderId,
                                    "p1",
                                    3),
                            tuple(
                                    orderId,
                                    "p2",
                                    1));

            verify(orderMapper).updateTotals(eq(orderId), eq(5500), eq(500));

            verify(gateway).send(any());
        }

        @Test
        void editOrder_updateOnly() {
            Order o = new Order() {
                {
                    setOrderId(orderId);
                    setOrderNumber(20);
                    setUserId(userId);
                    setPaymentStatus(PaymentStatus.UNPAID);
                    setShippingStatus(ShippingStatus.UNSHIPPED);
                }
            };
            doReturn(o).when(orderMapper).selectOrderByPrimaryKey(orderId);

            OrderItem i3 = item(orderId, "p3", 2, 500);

            // For the updated item after quantity reduction
            OrderItem i3Updated = item(orderId, "p3", 1, 500);

            // Mock the three calls to selectOrderItems in order
            when(orderMapper.selectOrderItems(orderId))
                    .thenReturn(List.of(i3)) // First call in prepareContext
                    .thenReturn(List.of(i3Updated)) // Second call for totals
                    .thenReturn(List.of(i3Updated)); // Third call for email

            User user = new User();
            user.setEmail("user@example.com");
            when(userMapper.selectUserByPrimaryKey(userId)).thenReturn(user);

            AdminOrderEditRequest req = new AdminOrderEditRequest() {
                {
                    setItems(Map.of("p3", 1));
                }
            };

            adminOrderService.editOrder(orderId, req);

            verify(productMapper).increaseStock("p3", 1, null);
            verify(orderMapper, never()).deleteOrderItem(anyString(), anyString());

            ArgumentCaptor<OrderItem> cap = ArgumentCaptor.forClass(OrderItem.class);
            verify(orderMapper).updateItemQty(cap.capture());
            OrderItem updated = cap.getValue();
            assertThat(updated).extracting(
                    OrderItem::getOrderId,
                    OrderItem::getProductId,
                    OrderItem::getQty)
                    .containsExactly(
                            orderId,
                            "p3",
                            1);

            verify(orderMapper).updateTotals(eq(orderId), eq(500), eq(0));

            verify(gateway).send(any());
        }

        @Test
        void editOrder_statusViolation() {

            Order order = new Order() {
                {
                    setOrderId(orderId);
                    setPaymentStatus(PaymentStatus.UNPAID);
                    setShippingStatus(ShippingStatus.SHIPPED);
                }
            };
            doReturn(order).when(orderMapper).selectOrderByPrimaryKey(orderId);
            AdminOrderEditRequest req = new AdminOrderEditRequest();

            assertThatThrownBy(() -> adminOrderService.editOrder(orderId, req))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(
                            e -> {
                                assertThat(((ResponseStatusException) e).getStatusCode())
                                        .isEqualTo(HttpStatus.CONFLICT);
                                assertThat(((ResponseStatusException) e).getReason())
                                        .isEqualTo("STATUS_NOT_EDITABLE");
                            });
        }

        @Test
        void editOrder_qtyIncrease() {
            Order order = new Order();
            order.setOrderId(orderId);
            order.setPaymentStatus(PaymentStatus.UNPAID);
            order.setShippingStatus(ShippingStatus.UNSHIPPED);

            OrderItem oldItem = new OrderItem();
            oldItem.setOrderId(orderId);
            oldItem.setProductId("p1");
            oldItem.setQty(5);
            oldItem.setUnitPriceIncl(1000);

            doReturn(order).when(orderMapper).selectOrderByPrimaryKey(orderId);
            doReturn(List.of(oldItem)).when(orderMapper).selectOrderItems(orderId);

            AdminOrderEditRequest req = new AdminOrderEditRequest() {
                {
                    setItems(Map.of("p1", 6));
                }
            };

            assertThatThrownBy(() -> adminOrderService.editOrder(orderId, req))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(
                            e -> {
                                assertThat(((ResponseStatusException) e).getStatusCode())
                                        .isEqualTo(HttpStatus.CONFLICT);
                                assertThat(((ResponseStatusException) e).getReason())
                                        .isEqualTo("QUANTITY_INCREASE_NOT_ALLOWED");
                            });
        }

        private OrderItem item(String oid, String pid, int qty, int price) {
            OrderItem oi = new OrderItem();
            oi.setOrderId(oid);
            oi.setProductId(pid);
            oi.setProductName(pid + "_name");
            oi.setQty(qty);
            oi.setUnitPriceIncl(price);

            // Set subtotal using reflection since setter is private
            try {
                Field subtotalField = OrderItem.class.getDeclaredField("subtotalIncl");
                subtotalField.setAccessible(true);
                subtotalField.set(oi, qty * price);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set subtotalIncl", e);
            }

            return oi;
        }
    }

    @Nested
    class GenerateDeliveryNote {
        AdminOrderDetailDto dto;

        @BeforeEach
        void setup() {
            dto = new AdminOrderDetailDto();
            dto.setOrderId("ORDER-001");
            dto.setOrderNumber("0001");
            dto.setItemsSubtotalIncl(500);
            dto.setShippingFeeIncl(500);
            dto.setCodFeeIncl(OrderUtil.obtainCodFeeIncl());
            dto.setGrandTotalIncl(1000);
            dto.setName("山田 太郎");
            dto.setEmail("taro@example.com");
            dto.setPostalCode("1000001");
            dto.setAddress("東京都千代田区丸の内1-1-1");
            dto.setPhoneNumber("0312345678");
            dto.setCreatedAt(LocalDateTime.of(2025, 9, 4, 10, 44, 21));

            // 明細2件
            AdminOrderDetailItemDto item1 = new AdminOrderDetailItemDto();
            item1.setProductId("P001");
            item1.setSku("SKU-1");
            item1.setProductName("ノート");
            item1.setUnitPriceIncl(200);
            item1.setQty(2);
            item1.setSubtotalIncl(400);

            AdminOrderDetailItemDto item2 = new AdminOrderDetailItemDto();
            item2.setProductId("P002");
            item2.setSku("SKU-2");
            item2.setProductName("ペン");
            item2.setUnitPriceIncl(100);
            item2.setQty(1);
            item2.setSubtotalIncl(100);

            // items にセット
            dto.setItems(List.of(item1, item2));
            doReturn(dto).when(adminOrderMapper).selectOrderDetail(anyString());
        }

        @Test
        void generateDeliveryNote_success() {
            try (MockedConstruction<PdfRendererBuilder> mocked = mockConstruction(
                    PdfRendererBuilder.class,
                    (builder, ctx) -> {
                        doReturn(builder).when(builder).withHtmlContent(anyString(), isNull());
                        doReturn(builder).when(builder).useFont(Mockito.any(FSSupplier.class),
                                anyString());

                        AtomicReference<OutputStream> out = new AtomicReference<OutputStream>();
                        doAnswer(inv -> {
                            out.set(inv.getArgument(0));
                            return builder;
                        }).when(builder).toStream(any());
                        doAnswer(inv -> {
                            OutputStream s = out.get();
                            s.write("%PDF-1.7\\n%ok\\n%%EOF".getBytes());
                            return null;
                        }).when(builder).run();

                    })) {
                AdminFileDto res = adminOrderService.generateDeliveryNote("ORDER-001");

                assertThat(res.getFileName()).isEqualTo("納品書_0001.pdf");
                assertThat(res.getBytes()).isNotEmpty();

                ArgumentCaptor<Context> cap = ArgumentCaptor.forClass(Context.class);
                verify(templateEngine).process(anyString(), cap.capture());
                Context ctx = cap.getValue();
                DeliveryNoteView arg = (DeliveryNoteView) ctx.getVariable("v");

                assertThat(arg.getOrderNumber()).isEqualTo("0001");
                assertThat(arg.getOrderDate()).isEqualTo("2025年09月04日");
                assertThat(arg.getItems()).hasSize(2);

                DeliveryNoteRow r = arg.getItems().get(0);
                assertThat(r.getSku()).isEqualTo("SKU-1");
                assertThat(r.getProductName()).isEqualTo("ノート");
                assertThat(r.getUnitPriceIncl()).isEqualTo(200);
                assertThat(r.getQty()).isEqualTo(2);
                assertThat(r.getSubtotalIncl()).isEqualTo(400);
            }

        }

        @Test
        void generateDeliveryNote_onClose() {
            try (MockedConstruction<PdfRendererBuilder> mockedBuilder = mockConstruction(PdfRendererBuilder.class,
                    (builder, ctx) -> {
                        doReturn(builder).when(builder).withHtmlContent(any(), any());
                        doReturn(builder).when(builder).useFont(Mockito.any(FSSupplier.class), any());
                    });
                    MockedConstruction<ByteArrayOutputStream> mockedOut = mockConstruction(ByteArrayOutputStream.class,
                            (out, ctx) -> {
                                doAnswer(inv -> {
                                    throw new IOException();
                                }).when(out).close();
                            })) {
                assertThatThrownBy(() -> adminOrderService.generateDeliveryNote("ORDER-001"))
                        .isInstanceOf(UncheckedIOException.class)
                        .hasCauseInstanceOf(IOException.class);

            }
        }

        @Test
        void generateDeliveryNote_onStart() {
            try (MockedConstruction<ClassPathResource> mockedRes = mockConstruction(ClassPathResource.class,
                    (res, ctx) -> {
                        doThrow(new IOException()).when(res).getInputStream();
                    });
                    MockedConstruction<PdfRendererBuilder> mockedBuilder = mockConstruction(PdfRendererBuilder.class,
                            (builder, ctx) -> {
                                doReturn(builder).when(builder).withHtmlContent(any(), any());
                                doAnswer(inv -> {
                                    FSSupplier<InputStream> sup = (FSSupplier<InputStream>) inv.getArgument(0);
                                    sup.supply();
                                    return builder;
                                }).when(builder).useFont(Mockito.any(FSSupplier.class), any());
                            });) {
                assertThatThrownBy(() -> adminOrderService.generateDeliveryNote("ORDER-001"))
                        .isInstanceOf(UncheckedIOException.class)
                        .hasCauseInstanceOf(IOException.class);

            }
        }
    }

    @Test
    void generateReceipt() {
        Order o = new Order();
        o.setOrderId("ORDER-001");
        o.setOrderNumber(1);
        o.setItemsSubtotalIncl(500);
        o.setShippingFeeIncl(0);
        ReflectionTestUtils.setField(o, "grandTotalIncl", 500);
        o.setName("山田 太郎");
        o.setPostalCode("1000001");
        o.setAddress("東京都千代田区丸の内1-1-1");
        o.setCreatedAt(LocalDateTime.of(2025, 9, 4, 10, 44, 21));
        o.setUpdatedAt(LocalDateTime.of(2025, 9, 4, 10, 44, 21));

        doReturn(o).when(orderMapper).selectOrderByPrimaryKey(anyString());

        LocalDate date = LocalDate.of(2025, 9, 5);

        try (MockedStatic<LocalDate> mockedNow = Mockito.mockStatic(LocalDate.class);
                MockedConstruction<PdfRendererBuilder> mockedPdf = mockConstruction(PdfRendererBuilder.class,
                        (builder, ctx) -> {
                            AtomicReference<OutputStream> outRef = new AtomicReference<OutputStream>();

                            doAnswer(inv -> {
                                outRef.set(inv.getArgument(0));
                                return builder;
                            }).when(builder).toStream(any());
                            doAnswer(inv -> {
                                outRef.get().write("%PDF-1.7\n%ok\n%%EOF".getBytes());
                                return null;
                            }).when(builder).run();
                        })) {

            mockedNow.when(() -> LocalDate.now()).thenReturn(date);

            AdminFileDto res = adminOrderService.generateReceipt("ORDER-001");

            assertThat(res.getFileName()).isEqualTo("領収書_0001.pdf");
            assertThat(res.getBytes()).isNotEmpty();

            ArgumentCaptor<Context> cap = ArgumentCaptor.forClass(Context.class);
            verify(templateEngine).process(anyString(), cap.capture());
            Context ctx = cap.getValue();
            ReceiptView arg = (ReceiptView) ctx.getVariable("v");

            assertThat(arg.getName()).isEqualTo("山田 太郎");
            assertThat(arg.getIssueDate()).isEqualTo("2025年09月05日");
            assertThat(arg.getGrandTotalIncl()).isEqualTo(500);
        }
    }

    @Nested
    class GenerateMonthlySales {
        String period = "2025-08";

        @BeforeEach
        void setup() {
            doReturn(1234).when(adminOrderMapper).selectMonthlySalesTotal(any(), any());
        }

        @Test
        void generateMonthlySales_ok() {
            AdminFileDto dto = adminOrderService.generateMonthlySales(period);
            String csv = new String(dto.getBytes(), StandardCharsets.UTF_8);

            assertThat(csv).startsWith("取引日,勘定科目,金額,摘要\r\n");
            assertThat(csv).contains("2025/08/31,売上高,1234,2025年8月売上合計");
        }

        @Test
        void generateMonthlySales_error() {
            try (MockedConstruction<CSVPrinter> mocked = mockConstruction(
                    CSVPrinter.class,
                    (printer, ctx) -> {
                        doThrow(new IOException()).when(printer).printRecord(any(Object[].class));
                    })) {
                assertThatThrownBy(() -> adminOrderService.generateMonthlySales(period))
                        .isInstanceOf(UncheckedIOException.class)
                        .hasCauseInstanceOf(IOException.class);
            }
        }
    }

}
