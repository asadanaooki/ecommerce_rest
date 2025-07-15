package com.example.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.util.TaxCalculator;

@Mapper(componentModel = "spring")
public abstract class TaxConverter {
    // TODO:
    // mavenのビルドでしかImplできない。eclipseの自動ビルドが反映されない

    @Autowired
    private TaxCalculator taxCalculator;
    
    @Named("withTax")
    public int calculatePriceIncludingTax(int price) {
        return taxCalculator.calculatePriceIncludingTax(price);
    }
}
