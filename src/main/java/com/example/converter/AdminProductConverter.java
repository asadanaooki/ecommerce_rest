package com.example.converter;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.example.dto.admin.AdminProductDto;
import com.example.entity.Product;
import com.example.enums.SaleStatus;
import com.example.request.admin.ProductUpsertRequest;

@Mapper(componentModel = "spring", uses = TaxConverter.class)
public interface AdminProductConverter {

    @Mapping(target = "price", source="price", qualifiedByName = "withTax")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusFromCode")
    AdminProductDto toDto(Product product);
    
    List<AdminProductDto> toDtoList(List<Product> products);
    
    @Named("statusFromCode")
    default SaleStatus mapStatus(String code) {
        return SaleStatus.fromCode(code);
    }
    
    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "productName", source = "req.productName")
    @Mapping(target = "price",       source = "req.price")
    @Mapping(target = "productDescription", source = "req.productDescription")
    @Mapping(target = "stock",       source = "req.stock")
    @Mapping(target = "status",       expression = "java(req.getStatus().getCode())")
    Product toEntity(String productId, ProductUpsertRequest req);
}
