package com.ysy.tmall.product;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ysy.tmall.common.to.producttocoupon.SpuBoundsTO;
import com.ysy.tmall.product.dao.AttrGroupDao;
import com.ysy.tmall.product.entity.AttrGroupEntity;
import com.ysy.tmall.product.entity.BrandEntity;
import com.ysy.tmall.product.feign.CouponFeignService;
import com.ysy.tmall.product.service.AttrService;
import com.ysy.tmall.product.service.BrandService;
import com.ysy.tmall.product.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
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

    @Resource
    CouponFeignService couponFeignService;

    @Resource
    AttrService attrService;

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

        AttrGroupEntity catelog_id = attrGroupDao.selectOne(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", 225).last("LIMIT 1"));

        log.info(catelog_id.toString());
    }

    @Test
    void testFeinCoupon() {
        SpuBoundsTO spuBoundsTO = new SpuBoundsTO();
        spuBoundsTO.setSpuId(22299L);
        spuBoundsTO.setBuyBounds(new BigDecimal("20"));
        spuBoundsTO.setGrowBounds(new BigDecimal("30"));
        couponFeignService.saveSpuBounds(spuBoundsTO);
    }

    @Test
    void testSearchAttr() {
        List<Long> attrIds = attrService.selectSerchAttrIds(Arrays.asList(1L));
        System.out.println(attrIds);
    }

    @Test
    void testReference() {

        ArrayList<Integer> objects = new ArrayList<>();
        this.getClass();
        String s = JSON.toJSONString(objects);
        TypeReference<ArrayList<Integer>> typeReference = new TypeReference<ArrayList<Integer>>() {
        };
        JSON.parseObject(s, typeReference);
        typeReference.getClass().getGenericSuperclass();
    }
}
