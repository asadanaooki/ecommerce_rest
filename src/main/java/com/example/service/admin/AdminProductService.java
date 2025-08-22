package com.example.service.admin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.dto.admin.AdminProductDetailDto;
import com.example.dto.admin.AdminProductDto;
import com.example.dto.admin.AdminProductListDto;
import com.example.entity.Product;
import com.example.entity.view.ProductCoreView;
import com.example.mapper.ProductMapper;
import com.example.mapper.admin.AdminProductMapper;
import com.example.request.admin.ProductSearchRequest;
import com.example.request.admin.ProductUpsertRequest;
import com.example.util.PaginationUtil;

import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;

@Service
@RequiredArgsConstructor
public class AdminProductService {
    // TODO:
    // 毎回countsメソッド呼ぶのか？
    // throwsで画像エラー回避してるが、フロントに500で返り何のエラーかわからん。ここどうするか検討
    // 拡張子チェックは、軽量チェックにしている。バイナリまで見た方がよりよい
    // 同時編集時、後から更新しようとした内容がすでに変更されていたら、通知するほうがよいかも
    // 商品詳細画面で、レビューも出す？
    // 税抜きで価格表示
    

    private final AdminProductMapper adminProductMapper;
    
    private final ProductMapper productMapper;

    @Value("${settings.admin.product.size}")
    private int pageSize;

    @Value("${settings.admin.product.upload.max-size}")
    private DataSize maxSize;

    private final Set<String> ALLOWED_MIME = Set.of("image/jpeg", "image/png");

    // TODO:
    // 仮の保存先
    private final Path IMAGE_DIR = Paths
            .get("C:/pleiades/2024-06/ecommerce/ecommerce_rest/src/main/resources/static/images");

    public AdminProductListDto searchProducts(ProductSearchRequest req) {
        int total = adminProductMapper.countProducts(req);
        int offset = PaginationUtil.calculateOffset(req.getPage(), pageSize);

        List<AdminProductDto> items = adminProductMapper.searchProducts(req, pageSize, offset);

        return new AdminProductListDto(items, total, pageSize);
    }
    
    public AdminProductDetailDto findDetail(String productId) {
        ProductCoreView product = productMapper.selectViewByPrimaryKey(productId);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        AdminProductDetailDto dto = new AdminProductDetailDto();
        dto.setProductId(product.getProductId());
        dto.setSku(product.getSku()); // DTO は String 型なので注意
        dto.setProductName(product.getProductName());
        dto.setProductDescription(product.getProductDescription());
        dto.setPriceExcl(product.getPriceExcl());
        dto.setAvailable(product.getAvailable());
        dto.setStatus(product.getStatus());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());

        return dto;
    }

    @Transactional
    public void create(ProductUpsertRequest req) {
        IMageData data = preprocessImage(req.getImage());
        String productId = UUID.randomUUID().toString();

     // Productエンティティに詰め替え
        Product entity = new Product();
        entity.setProductId(UUID.randomUUID().toString());
        entity.setProductName(req.getProductName());
        entity.setProductDescription(req.getProductDescription());
        entity.setPriceExcl(req.getPriceExcl());
        entity.setStock(0);            // 新規作成なので0で初期化
        entity.setReserved(0);         // 同上
        entity.setStatus(req.getStatus());
        
        adminProductMapper.insert(entity);

        // TODO:
        // 画像削除の場合に対応してない
        if (data != null) {
            saveImage(productId, data);
        }
    }

    @Transactional
    public void update(String productId, ProductUpsertRequest req) {
        IMageData data = preprocessImage(req.getImage());
        
        Product entity = new Product();
        entity.setProductId(productId);
        entity.setProductName(req.getProductName());
        entity.setProductDescription(req.getProductDescription());
        entity.setPriceExcl(req.getPriceExcl());
        entity.setStatus(req.getStatus());
        
        adminProductMapper.update(entity);

        if (data != null) {
            saveImage(productId, data);
        }
    }

    private IMageData preprocessImage(MultipartFile file) {
        if (file == null) {
            return null;
        }
        if (file.getSize() > maxSize.toBytes()) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE);
        }

        if (!ALLOWED_MIME.contains(file.getContentType())) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }

        String ext = file.getContentType().equals("image/png") ? "png" : "jpg";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (InputStream in = file.getInputStream()) {
            Thumbnails.of(in)
                    .size(1200, 1200)
                    .outputFormat(ext)
                    .toOutputStream(baos);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        byte[] data = baos.toByteArray();
        if (data.length > maxSize.toBytes()) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE);
        }

        return new IMageData(data, ext);
    }

    private void saveImage(String productId, IMageData data) {
        try {
            Files.createDirectories(IMAGE_DIR);
            Path out = IMAGE_DIR.resolve(productId + "." + data.ext());
            Files.write(out, data.bytes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private record IMageData(byte[] bytes, String ext) {
    }
}
