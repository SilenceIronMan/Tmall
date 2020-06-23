package com.ysy.tmall.product;

import com.ysy.tmall.product.entity.BrandEntity;
import com.ysy.tmall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;


@SpringBootTest
class TmallProductApplicationTests {

    @Resource
    BrandService brandService;

    @Test
    void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setName("Huawei2");
        brandService.save(brandEntity);

    }

}
