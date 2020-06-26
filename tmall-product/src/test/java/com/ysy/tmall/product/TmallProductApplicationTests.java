package com.ysy.tmall.product;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ysy.tmall.product.entity.BrandEntity;
import com.ysy.tmall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;


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

    @Test
    void listBrand() {
        List<BrandEntity> brandList = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1L));
        brandList.stream().forEach(b -> System.out.println(b));
    }

}
