package com.example.converter;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.dto.FavoritePageDto;
import com.example.entity.Product;

@Mapper(componentModel = "spring", uses = TaxConverter.class)
public interface CartConverter {

    @Mapping(target = "price", source="price", qualifiedByName = "withTax")
    FavoritePageDto.FavoriteRow toRow(Product product);
    
    List<FavoritePageDto.FavoriteRow> toRowList(List<Product> products);

}
