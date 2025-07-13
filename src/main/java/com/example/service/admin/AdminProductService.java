package com.example.service.admin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

import com.example.dto.admin.AdminProductDto;
import com.example.dto.admin.AdminProductListDto;
import com.example.entity.Product;
import com.example.enums.SaleStatus;
import com.example.mapper.admin.AdminProductMapper;
import com.example.request.admin.ProductRegistrationRequest;
import com.example.request.admin.ProductSearchRequest;
import com.example.util.PaginationUtil;
import com.example.util.TaxCalculator;

import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;

@Service
@RequiredArgsConstructor
public class AdminProductService {
    // TODO:
    // 毎回countsメソッド呼ぶのか？
    // throwsで画像エラー回避してるが、フロントに500で返り何のエラーかわからん。ここどうするか検討
    // 拡張子チェックは、軽量チェックにしている。バイナリまで見た方がよりよい

    private final AdminProductMapper adminProductMapper;
    
    private final TaxCalculator calculator;
    
    @Value("${settings.admin.product.size}")
    private int pageSize;
    
    @Value("${settings.admin.product.upload.max-size}")
    private DataSize maxSize;
    
    private final Set<String> ALLOWED_MIME = Set.of("image/jpeg", "image/png");
    
    // TODO:
    // 仮の保存先
    private final Path IMAGE_DIR = Paths.get( "C:/pleiades/2024-06/ecommerce/ecommerce_rest/src/main/resources/static/images");
    
    public AdminProductListDto searchProducts(ProductSearchRequest req) {
        int total = adminProductMapper.countProducts(req);
        int offset = PaginationUtil.calculateOffset(req.getPage(), pageSize);
        
        List<AdminProductDto> items = adminProductMapper.searchProducts(req, pageSize, offset)
                .stream()
                .map(e -> {
                    AdminProductDto dto = new AdminProductDto();
                    dto.setProductId(e.getProductId());
                    dto.setSku(e.getSku());
                    dto.setProductName(e.getProductName());
                    dto.setPrice(calculator.calculatePriceIncludingTax(e.getPrice()));
                    dto.setStock(e.getStock());
                    dto.setStatus(SaleStatus.fromCode(e.getStatus()));
                    dto.setUpdatedAt(e.getUpdatedAt());
                    return dto;
                }).toList();
        
        return new AdminProductListDto(items, total, pageSize);
    }
    
    @Transactional
    public void create(ProductRegistrationRequest req) throws IOException {
        MultipartFile img = req.getImage();
        IMageData data = null;
        
        if (img != null) {
            data = preprocessImage(img);
        }
        
        String productId = UUID.randomUUID().toString();
        
        Product product = new Product() {
            {
                setProductId(productId);
                setProductName(req.getProductName());
                setPrice(req.getPrice());
                setProductDescription(req.getProductDescription());
                setStock(req.getStock());
                setStatus(req.getStatus().getCode());
            }
        };
        adminProductMapper.insert(product);
        
        if (data != null) {
            Files.createDirectories(IMAGE_DIR);
            Path out = IMAGE_DIR.resolve(productId + "." + data.ext());
            Files.write(out, data.bytes());
        }
    }
    
    private IMageData preprocessImage(MultipartFile file) throws IOException {
        if (file.getSize() > maxSize.toBytes()) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE);
        }
        
        if (!ALLOWED_MIME.contains(file.getContentType())) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }
        
        String ext = file.getContentType().equals("image/png") ? "png" : "jpg";
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try(InputStream in = file.getInputStream()){
            Thumbnails.of(in)
            .size(1200, 1200)
            .outputFormat(ext)
            .toOutputStream(baos);
        }
        
        byte[] data = baos.toByteArray();
        if (data.length > maxSize.toBytes()) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE);
        }
        
        return new IMageData(data, ext);
    }
    
    private record IMageData(byte[] bytes, String ext) {}
}
