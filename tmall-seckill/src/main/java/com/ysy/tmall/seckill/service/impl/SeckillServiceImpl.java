package com.ysy.tmall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.ysy.tmall.common.utils.R;
import com.ysy.tmall.seckill.feign.CouponFeignService;
import com.ysy.tmall.seckill.feign.ProductFeignService;
import com.ysy.tmall.seckill.service.SeckillService;
import com.ysy.tmall.seckill.to.SeckillSkuRedisTo;
import com.ysy.tmall.seckill.vo.SeckillSessionsWithSkus;
import com.ysy.tmall.seckill.vo.SkuInfoVo;
import org.redisson.Redisson;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @anthor silenceYin
 * @date 2020/9/1 - 1:10
 */
@Service
public class SeckillServiceImpl implements SeckillService {

    @Resource
    private CouponFeignService couponFeignService;

    @Resource
    private ProductFeignService productFeignService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    private final String SESSIONS_CACHE_PREFIX = "seckill:session:"; // + 开始时间_结束时间
    private final String SKUKILL_CACHE_PREFIX = "seckill:skus";
    private final String SKU_STOCK_SEMAPHORE = "seckill:stock:"; // + 商品随机码

    @Override
    public void uploadSeckillSkuLatest3Days() {
        // 1. 扫描需要参与秒杀的活动
        R latest3DaysSession = couponFeignService.getLatest3DaysSession();
        if (latest3DaysSession.getCode() == 0) {
            // 上架商品
            List<SeckillSessionsWithSkus> sessionData = latest3DaysSession.getData(new TypeReference<List<SeckillSessionsWithSkus>>() {
            });

            // 緩存redis
            // 缓存活动信息
            saveSessionInfos(sessionData);
            // 缓存活动相关商品信息
            saveSessionSkuInfos(sessionData);


        }




    }

    /**
     * 保存场次信息
     * @param sessionData
     */
    private void saveSessionSkuInfos(List<SeckillSessionsWithSkus> sessionData) {
        sessionData.stream().forEach(session -> {
            long startTime = session.getStartTime().getTime();
            long endTime = session.getEndTime().getTime();
            String key = startTime + "_" +endTime;
            Boolean aBoolean = redisTemplate.hasKey(key);

            if (!aBoolean) {
                List<String> skuIds = session.getRelationSkus().stream()
                        .map(seckillSkuVo -> seckillSkuVo.getPromotionSessionId().toString() + "_" + seckillSkuVo.getSkuId().toString())
                        .collect(Collectors.toList());


                // 缓存活动信息
                redisTemplate.opsForList().leftPushAll(SESSIONS_CACHE_PREFIX + key, skuIds);
            }


        });

    }

    /**
     * 保存场次sku信息
     * @param sessionData
     */
    private void saveSessionInfos(List<SeckillSessionsWithSkus> sessionData) {
        // 准备hash
        BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);

        sessionData.stream().forEach(seckillSessionsWithSkus -> {


            // 缓存商品
            seckillSessionsWithSkus.getRelationSkus().stream().forEach(seckillSkuVo -> {
                String sessionIdSkuIdKey = seckillSkuVo.getPromotionSessionId().toString() + "_" + seckillSkuVo.getSkuId().toString();
                if(!ops.hasKey(sessionIdSkuIdKey)) {


                SeckillSkuRedisTo seckillSkuRedisTo = new SeckillSkuRedisTo();
                // 1. sku 基本信息
                R info = productFeignService.info(seckillSkuVo.getSkuId());
                if (info.getCode() == 0) {
                    SkuInfoVo skuInfo = info.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                    });
                    seckillSkuRedisTo.setSkuInfoVo(skuInfo);
                }

                // 2. sku 秒杀信息
                BeanUtils.copyProperties(seckillSkuVo, seckillSkuRedisTo);

                // 3. 设置当前商品秒杀时间
                seckillSkuRedisTo.setStartTime(seckillSessionsWithSkus.getStartTime().getTime());
                seckillSkuRedisTo.setEndTime(seckillSessionsWithSkus.getEndTime().getTime());

                // 4. 随机码 ?skuId=1&key=1231%@# (防止直接通过连接访问该商品)
                String randomCode = UUID.randomUUID().toString().replace("-", "");
                seckillSkuRedisTo.setRandomCode(randomCode);

                // 5. 引入分布式信号量 库存信号量设置 (限流)
                RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + randomCode);
                semaphore.trySetPermits(seckillSkuVo.getSeckillCount().intValue());

                ops.put(sessionIdSkuIdKey, JSON.toJSONString(seckillSkuRedisTo));

                }
            });
        });

    }
}
