package com.example.converter;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.dto.admin.AdminProductDetailDto;
import com.example.dto.admin.AdminProductDto;
import com.example.entity.Product;
import com.example.entity.view.ProductCoreView;
import com.example.request.admin.ProductUpsertRequest;

@Mapper(componentModel = "spring", uses = TaxConverter.class)
public interface AdminProductConverter {

    @Mapping(target = "price", source="price", qualifiedByName = "withTax")
    AdminProductDto toDto(ProductCoreView product);
    
    List<AdminProductDto> toDtoList(List<ProductCoreView> products);
    
    Product toEntity(String productId, ProductUpsertRequest req);
    
    @Mapping(target = "price", source="price", qualifiedByName = "withTax")
    AdminProductDetailDto toDetailDto(ProductCoreView product);
}
