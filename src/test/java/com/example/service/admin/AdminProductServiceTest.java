package com.example.service.admin;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.web.server.ResponseStatusException;

import com.example.dto.admin.AdminProductDto;
import com.example.dto.admin.AdminProductListDto;
import com.example.entity.Product;
import com.example.enums.SaleStatus;
import com.example.mapper.admin.AdminProductMapper;
import com.example.request.admin.ProductSearchRequest;
import com.example.request.admin.ProductUpsertRequest;

@ExtendWith(MockitoExtension.class)
class AdminProductServiceTest {
    // TODO:
    // converterを本物にするとテストできない、現状モック

    @InjectMocks
    AdminProductService adminProductService;


    @Mock
    AdminProductMapper adminProductMapper;

    @TempDir
    Path imageDir;

    @Test
    void searchProducts() {
//        TaxConverter tc = Mappers.getMapper(TaxConverter.class);
//        ReflectionTestUtils.setField(tc, "taxCalculator", new TaxCalculator(10));
//        ReflectionTestUtils.setField(adminProductConverter, "taxConverter",tc);
        ReflectionTestUtils.setField(adminProductService, "pageSize", 10);
        ProductSearchRequest req = new ProductSearchRequest();

        AdminProductDto p1 = new AdminProductDto();
        p1.setProductId("id-1");
        p1.setSku(1);
        p1.setProductName("Product One");
        p1.setPriceExcl(110);
        p1.setAvailable(5);
        p1.setStatus(SaleStatus.UNPUBLISHED);
        p1.setUpdatedAt(LocalDateTime.of(2025, 7, 1, 12, 0));
        
        AdminProductDto p2 = new AdminProductDto();

        doReturn(2).when(adminProductMapper).countProducts(req);
        doReturn(List.of(p1, p2)).when(adminProductMapper).searchProducts(req, 10, 0);

        AdminProductListDto res = adminProductService.searchProducts(req);

        assertThat(res.getItems()).hasSize(2);
        assertThat(res.getPageSize()).isEqualTo(10);
        assertThat(res.getTotal()).isEqualTo(2);

        assertThat(res.getItems()).first().extracting(
                AdminProductDto::getProductId,
                AdminProductDto::getSku,
                AdminProductDto::getProductName,
                AdminProductDto::getPriceExcl,
                AdminProductDto::getAvailable,
                AdminProductDto::getStatus,
                AdminProductDto::getUpdatedAt)
                .containsExactly(
                        "id-1",
                        1,
                        "Product One",
                        110,
                        5,
                        SaleStatus.UNPUBLISHED,
                        LocalDateTime.of(2025, 7, 1, 12, 0));
    }

    @Nested
    class Create {
        ProductUpsertRequest req;

        @BeforeEach
        void setup() throws IOException {
            ReflectionTestUtils.setField(adminProductService, "maxSize", DataSize.ofKilobytes(128));
            ReflectionTestUtils.setField(adminProductService, "IMAGE_DIR", imageDir);

            req = new ProductUpsertRequest();
            req.setProductName("test");
            req.setProductDescription("testDesc");
            req.setPriceExcl(100);
            req.setStatus(SaleStatus.UNPUBLISHED);

            BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            byte[] pngBytes = baos.toByteArray();

            MockMultipartFile file = new MockMultipartFile("image", "one-px.png", "image/png", pngBytes);
            req.setImage(file);
        }

        @Test
        void create_noImage_success() throws IOException {
            req.setImage(null);
            adminProductService.create(req);

            verify(adminProductMapper).insert(any());
        }

        @Test
        void create_withImage_success() throws IOException {
            adminProductService.create(req);

            verify(adminProductMapper).insert(any());
            assertThat(Files.list(imageDir)).hasSize(1);
        }

        @Test
        void create_sizeTooLarge() throws IOException {
            ReflectionTestUtils.setField(adminProductService, "maxSize", DataSize.ofBytes(10));

            assertThatThrownBy(() -> adminProductService.create(req))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                            .isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE));

            verify(adminProductMapper, never()).insert(any(Product.class));
        }

        @Test
        void create_invalidMime() {
            byte[] b = "GIF89a".getBytes();
            MockMultipartFile file = new MockMultipartFile("image", "anim.gif", "image/gif", b);
            req.setImage(file);

            assertThatThrownBy(() -> adminProductService.create(req))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                            .isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE));

            verify(adminProductMapper, never()).insert(any(Product.class));
        }

        @Test
        void create_resizeTooLarge() throws IOException {
            ReflectionTestUtils.setField(adminProductService, "maxSize", DataSize.ofBytes(1));

            BufferedImage img = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            byte[] pngBytes = baos.toByteArray();

            MockMultipartFile file = new MockMultipartFile("image", "large.png", "image/png", pngBytes);
            MockMultipartFile spyFile = spy(file);
            doReturn((long) 1).when(spyFile).getSize();
            req.setImage(spyFile);

            assertThatThrownBy(() -> adminProductService.create(req))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                            .isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE));

            verify(adminProductMapper, never()).insert(any(Product.class));
        }
    }

}
