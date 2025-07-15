package com.example.converter;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.dto.FavoritePageDto;
import com.example.entity.Product;
import com.example.enums.SaleStatus;

@Mapper(componentModel = "spring", uses = TaxConverter.class, imports = SaleStatus.class)
public interface CartConverter {

    @Mapping(target = "price", source="price", qualifiedByName = "withTax")
    @Mapping(target = "status", expression = "java(SaleStatus.fromCode(product.getStatus()))")
    FavoritePageDto.FavoriteRow toRow(Product product);
    
    List<FavoritePageDto.FavoriteRow> toRowList(List<Product> products);
    
//    @Named("statusFromCode")
//    default SaleStatus mapStatus(String code) {
//        return SaleStatus.fromCode(code);
//    }
}
