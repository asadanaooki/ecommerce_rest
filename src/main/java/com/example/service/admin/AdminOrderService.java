package com.example.service.admin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.example.dto.admin.AdminFileDto;
import com.example.dto.admin.AdminOrderDetailDto;
import com.example.dto.admin.AdminOrderDetailItemDto;
import com.example.dto.admin.AdminOrderListDto;
import com.example.dto.admin.AdminOrderRowDto;
import com.example.entity.Order;
import com.example.entity.OrderItem;
import com.example.entity.User;
import com.example.enums.MailTemplate;
import com.example.enums.MailTemplate.OrderEditCompletedContext;
import com.example.enums.order.PaymentStatus;
import com.example.enums.order.ShippingStatus;
import com.example.mapper.OrderMapper;
import com.example.mapper.ProductMapper;
import com.example.mapper.UserMapper;
import com.example.mapper.admin.AdminOrderMapper;
import com.example.request.admin.OrderEditRequest;
import com.example.request.admin.OrderSearchRequest;
import com.example.support.MailGateway;
import com.example.util.OrderUtil;
import com.example.util.PaginationUtil;
import com.example.util.UserUtil;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminOrderService {
    /* TODO:
     * 検索の便利さのために、カナも表示したほうがよいかも
     * ステータスの状態遷移ルールや定義。手動から自動遷移にしたい
     * Webhook or ポーリング
     * 現状、顧客に請求している金額（＝税込み） をベースに表示、管理や集計のための金額を考慮する場合は税抜きも検討
     * PDF
         請求書、領収書の印鑑、改ざん検知、DF一括作成、A4以外のサイズを考慮するか？
         レイアウト調整
     * 領収書
         支払方法可変にする
         会社情報未記載
         宛名自由に入力できるようにする？
     * CSV
         商品データ
         会員データ
         顧客データ
         配送システム連携用 
         会計ソフト連携
     */

    private final AdminOrderMapper adminOrderMapper;

    private final OrderMapper orderMapper;

    private final UserMapper userMapper;

    private final ProductMapper productMapper;

    @Value("${settings.admin.order.size}")
    private int pageSize;

    private final MailGateway mailGateway;

    private final SpringTemplateEngine templateEngine;

    private static final DateTimeFormatter DATE_VIEW = DateTimeFormatter.ofPattern("yyyy年MM月dd日");

    private static final String PDF_EXTENSION = ".pdf";
    
    private static final String CSV_EXTENSION = ".csv";

    public AdminOrderListDto search(OrderSearchRequest req) {
        int offset = PaginationUtil.calculateOffset(req.getPage(), pageSize);

        List<AdminOrderRowDto> content = adminOrderMapper.selectPage(req, pageSize, offset);
        int total = adminOrderMapper.count(req);

        return new AdminOrderListDto(content, total, pageSize);
    }

    public AdminOrderDetailDto findDetail(String orderId) {
        return adminOrderMapper.selectOrderDetail(orderId);
    }

    @Transactional
    public void editOrder(String orderId, OrderEditRequest req) {
        // 準備
        Order o = orderMapper.selectOrderByPrimaryKey(orderId);
        EditContext ctx = prepareContext(o, req);

        // 処理
        processQtyReduction(ctx);
        processDeletion(ctx);

        List<OrderItem> items = orderMapper.selectOrderItems(orderId);
        int itemsSubtotalInclAfter = OrderUtil.sumBy(items, OrderItem::getSubtotalIncl);
        int shippingFeeInclAfter = OrderUtil.calculateShippingFeeIncl(itemsSubtotalInclAfter);
        orderMapper.updateTotals(
                orderId,
                itemsSubtotalInclAfter,
                shippingFeeInclAfter);

        // メール送信
        Order updatedOrder = orderMapper.selectOrderByPrimaryKey(orderId);
        List<OrderItem> updatedItems = orderMapper.selectOrderItems(orderId);
        User user = userMapper.selectUserByPrimaryKey(o.getUserId());

        mailGateway.send(MailTemplate.ORDER_EDIT_COMPLETED.build(
                new OrderEditCompletedContext(
                        user.getEmail(),
                        UserUtil.buildFullName(user),
                        UserUtil.buildFullAddress(user),
                        OrderUtil.formatOrderNumber(o.getOrderNumber()),
                        updatedItems,
                        updatedOrder.getItemsSubtotalIncl(),
                        updatedOrder.getShippingFeeIncl(),
                        updatedOrder.getCodFeeIncl(),
                        updatedOrder.getGrandTotalIncl())));
    }

    public AdminFileDto generateDeliveryNote(String orderId) {
        AdminOrderDetailDto d = adminOrderMapper.selectOrderDetail(orderId);

        DeliveryNoteView v = new DeliveryNoteView();
        v.setOrderNumber(d.getOrderNumber());
        v.setOrderDate(DATE_VIEW.format(d.getCreatedAt()));

        for (AdminOrderDetailItemDto it : d.getItems()) {
            DeliveryNoteRow r = new DeliveryNoteRow();
            r.setSku(it.getSku());
            r.setProductName(it.getProductName());
            r.setUnitPriceIncl(it.getUnitPriceIncl());
            r.setQty(it.getQty());
            r.setSubtotalIncl(it.getSubtotalIncl());
            v.getItems().add(r);
        }

        byte[] bytes = render("delivery", Map.of("v", v));
        String fileName = "納品書_" + v.getOrderNumber() + PDF_EXTENSION;
        return new AdminFileDto(fileName, bytes);
    }

    public AdminFileDto generateReceipt(String orderId) {
        Order o = orderMapper.selectOrderByPrimaryKey(orderId);

        ReceiptView rv = new ReceiptView();
        rv.setName(o.getName());
        rv.setGrandTotalIncl(o.getGrandTotalIncl());
        var s = LocalDate.now();
        rv.setIssueDate(DATE_VIEW.format(LocalDate.now()));

        byte[] bytes = render("receipt", Map.of("v", rv));
        String fileName = "領収書_" + OrderUtil.formatOrderNumber(o.getOrderNumber()) + PDF_EXTENSION;

        return new AdminFileDto(fileName, bytes);
    }

    public AdminFileDto generateMonthlySales(String period) {
        YearMonth ym = YearMonth.parse(period);

        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime endExclusive = ym.atDay(1).atStartOfDay();
        LocalDate monthEnd = ym.atEndOfMonth();

        int total = adminOrderMapper.selectMonthlySalesTotal(start, endExclusive);

        // 摘要用の文字列（例: "2025年9月売上合計"）
        String summary = ym.format(DateTimeFormatter.ofPattern("yyyy年M月")) + "売上合計";
        String dateStr = monthEnd.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        CSVFormat format = CSVFormat.Builder.create(CSVFormat.DEFAULT)
                .setHeader("取引日", "勘定科目", "金額", "摘要")
                .setRecordSeparator("\r\n")
                .get();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
                CSVPrinter printer = new CSVPrinter(writer, format)) {
            printer.printRecord(dateStr, "売上高", total, summary);
            printer.flush();
            
            byte[] bytes = out.toByteArray();
            String fileName = "売上_" + ym.toString() + CSV_EXTENSION;
            
            return new AdminFileDto(fileName, bytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private byte[] render(String template, Map<String, Object> model) {
        String html = templateEngine.process(template, new Context(Locale.JAPAN, model));

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);

            ClassPathResource font = new ClassPathResource("fonts/NotoSansJP-Regular.ttf");
            builder.useFont(() -> {
                try {
                    return font.getInputStream();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }, "Noto Sans JP");

            builder.useFastMode();
            builder.toStream(out);
            builder.run();

            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private EditContext prepareContext(Order order, OrderEditRequest req) {

        if (!(order.getPaymentStatus() == PaymentStatus.UNPAID
                && order.getShippingStatus() == ShippingStatus.UNSHIPPED)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "STATUS_NOT_EDITABLE");
        }

        Map<String, OrderItem> oldm = orderMapper.selectOrderItems(order.getOrderId()).stream()
                .collect(Collectors.toMap(OrderItem::getProductId, Function.identity()));

        Map<String, Integer> newm = req.getItems();
        List<String> deleteIds = req.getDeleted();

        return new EditContext(order.getOrderId(), oldm, newm, deleteIds);
    }

    private void processQtyReduction(EditContext ctx) {
        for (Entry<String, Integer> e : ctx.newm().entrySet()) {
            String productId = e.getKey();
            int newQty = e.getValue();
            OrderItem old = ctx.oldm.get(productId);

            if (newQty > old.getQty()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "QUANTITY_INCREASE_NOT_ALLOWED");
            }
            if (newQty == old.getQty()) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);
            }
            productMapper.increaseStock(productId, old.getQty() - newQty, null);
            old.setQty(newQty);
            orderMapper.updateItemQty(old);
        }
    }

    private void processDeletion(EditContext ctx) {
        for (String pid : ctx.deleteIds()) {
            OrderItem old = ctx.oldm().get(pid);

            productMapper.increaseStock(pid, old.getQty(), null);
            orderMapper.deleteOrderItem(ctx.orderId(), pid);
        }
    }

    private record EditContext(
            String orderId,
            Map<String, OrderItem> oldm,
            Map<String, Integer> newm,
            List<String> deleteIds) {
    }

    @Data
    public static class DeliveryNoteView {
        private String orderNumber;
        private String orderDate;
        private List<DeliveryNoteRow> items = new ArrayList<DeliveryNoteRow>();
    }

    @Data
    public static class DeliveryNoteRow {
        private String sku;
        private String productName;
        private int unitPriceIncl;
        private int qty;
        private int subtotalIncl;
    }

    @Data
    public static class ReceiptView {
        private String name;
        private int grandTotalIncl;
        private String issueDate;
    }
}
