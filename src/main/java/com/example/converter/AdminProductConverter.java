package com.example.converter;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.dto.admin.AdminProductDetailDto;
import com.example.dto.admin.AdminProductDto;
import com.example.entity.Product;
import com.example.request.admin.ProductUpsertRequest;

@Mapper(componentModel = "spring", uses = TaxConverter.class)
public interface AdminProductConverter {

    @Mapping(target = "price", source="price", qualifiedByName = "withTax")
    AdminProductDto toDto(Product product);
    
    List<AdminProductDto> toDtoList(List<Product> products);
    
    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "productName", source = "req.productName")
    @Mapping(target = "price",       source = "req.price")
    @Mapping(target = "productDescription", source = "req.productDescription")
    @Mapping(target = "stock",       source = "req.stock")
    Product toEntity(String productId, ProductUpsertRequest req);
    
    @Mapping(target = "price", source="price", qualifiedByName = "withTax")
    AdminProductDetailDto toDetailDto(Product product);
}
