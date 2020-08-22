package com.ysy.tmall.product;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ysy.tmall.common.to.SpuBoundsTO;
import com.ysy.tmall.product.dao.AttrGroupDao;
import com.ysy.tmall.product.entity.AttrGroupEntity;
import com.ysy.tmall.product.entity.BrandEntity;
import com.ysy.tmall.product.feign.CouponFeignService;
import com.ysy.tmall.product.service.AttrGroupService;
import com.ysy.tmall.product.service.AttrService;
import com.ysy.tmall.product.service.BrandService;
import com.ysy.tmall.product.service.CategoryService;
import com.ysy.tmall.product.vo.web.SpuItemAttrGroupVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


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

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    RedissonClient redissonClient;

    @Resource
    private CacheProperties cacheProperties;

    @Resource
    AttrGroupService attrGroupService;
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
    void testCategoryPath() {

        Long[] catelogPath = categoryService.findCatelogPath(225L);

        log.info(Arrays.toString(catelogPath));
    }


    @Test
    void testSelectOne() {

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


    @Test
    void testRedis() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();

        ops.set("hello", "world!" + UUID.randomUUID());
        System.out.println(redissonClient);
        String hello = ops.get("hello");
        log.info(hello + "redis");
        System.out.println(cacheProperties);
    }

    @Test
    void testRedisLock() {
        RLock lock = redissonClient.getLock("test-lock");
        lock.lock();
        try {
            log.info("redis 锁");
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {

            lock.unlock();
        }
    }

    @Test
    void testAttrGroup() {
        List<SpuItemAttrGroupVo> attrGroupWithAttrsBySpuId = attrGroupService.getAttrGroupWithAttrsBySpuId(4L, 225L);

        log.info(attrGroupWithAttrsBySpuId.toString());

    }
}
