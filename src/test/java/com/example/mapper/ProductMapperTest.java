package com.example.mapper;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import com.example.entity.Product;
import com.example.testUtil.FlywayResetExtension;

@ExtendWith(FlywayResetExtension.class)
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductMapperTest {

    @Autowired
    ProductMapper productMapper;
    
    @Test
    void decreaseStock() {
        String productId = "f9c9cfb2-0893-4f1c-b508-f9e909ba5274";
        int rows = productMapper.decreaseStock(productId, 4);
        
        assertThat(rows).isOne();
        
       Product p = productMapper.selectByPrimaryKey(productId);
        assertThat(p.getStock()).isEqualTo(11);
    }

}
