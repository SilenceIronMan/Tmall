package com.ysy.tmall.product;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ysy.tmall.product.dao.AttrGroupDao;
import com.ysy.tmall.product.entity.AttrGroupEntity;
import com.ysy.tmall.product.entity.BrandEntity;
import com.ysy.tmall.product.service.AttrGroupService;
import com.ysy.tmall.product.service.BrandService;
import com.ysy.tmall.product.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;


@SpringBootTest
@Slf4j
class TmallProductApplicationTests {

    @Resource
    BrandService brandService;

    @Resource
    AttrGroupDao attrGroupDao;

    @Resource
    CategoryService categoryService;

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

    @Test
    void testCategoryPath(){

        Long[] catelogPath = categoryService.findCatelogPath(225L);

        log.info(Arrays.toString(catelogPath));
    }


    @Test
    void testSelectOne(){

        AttrGroupEntity catelog_id = attrGroupDao.selectOne(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", 225));

        log.info(catelog_id.toString());
    }

}
