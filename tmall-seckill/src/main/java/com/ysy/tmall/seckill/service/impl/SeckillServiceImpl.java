package com.ysy.tmall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.ysy.tmall.common.to.mq.SeckillOrderTo;
import com.ysy.tmall.common.utils.R;
import com.ysy.tmall.common.vo.MemberResponseVO;
import com.ysy.tmall.seckill.feign.CouponFeignService;
import com.ysy.tmall.seckill.feign.ProductFeignService;
import com.ysy.tmall.seckill.interceptor.LoginUserInterceptor;
import com.ysy.tmall.seckill.service.SeckillService;
import com.ysy.tmall.seckill.to.SeckillSkuRedisTo;
import com.ysy.tmall.seckill.vo.SeckillSessionsWithSkus;
import com.ysy.tmall.seckill.vo.SkuInfoVo;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private RabbitTemplate rabbitTemplate;

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

    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
        long nowTime = new Date().getTime();
        Set<String> keys = redisTemplate.keys(SESSIONS_CACHE_PREFIX + "*");
        for (String key : keys) {
            String[] split = key.replace(SESSIONS_CACHE_PREFIX, StringUtils.EMPTY).split("_");
            long startTime = Long.parseLong(split[0]);
            long endTime = Long.parseLong(split[1]);

            if (nowTime >= startTime && nowTime <= endTime) {
                List<String> sessionSkuList = redisTemplate.opsForList().range(key, -100, 100);

                BoundHashOperations<String, String, String> operations = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);

                List<String> list = operations.multiGet(sessionSkuList);

                if (Objects.nonNull(list)) {
                    List<SeckillSkuRedisTo> collect = list.stream().map(x -> {
                        SeckillSkuRedisTo seckillSkuRedisTo = JSON.parseObject(x, SeckillSkuRedisTo.class);
                        return seckillSkuRedisTo;

                    }).collect(Collectors.toList());
                    return collect;
                }

                // 匹配上就跳出(时间段不交叉)
                break;
            }

        }
        return null;
    }

    @Override
    public SeckillSkuRedisTo getSkuSeckillInfo(Long skuId) {

        // 1. 找到所有参与秒杀的key

        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        Set<String> keys = ops.keys();

        if (keys != null && keys.size() > 0) {
            String regex = "\\d_" + skuId;
            Pattern compile = Pattern.compile(regex);
            for (String key : keys) {
                Matcher matcher = compile.matcher(key);
                boolean matches = matcher.matches();
                if (matches) {
                    String redisToStr = ops.get(key);
                    SeckillSkuRedisTo seckillSkuRedisTo = JSON.parseObject(redisToStr, SeckillSkuRedisTo.class);
                    long nowTime = new Date().getTime();
                    long startTime = seckillSkuRedisTo.getStartTime();
                    long endTime = seckillSkuRedisTo.getEndTime();
                    if (nowTime >= startTime && nowTime <= endTime) {

                    } else {
                        seckillSkuRedisTo.setRandomCode(null);
                    }
                    return seckillSkuRedisTo;
                }


            }
        }
        return null;
    }

    @Override
    public String kill(String killId, String key, Integer num) {
        // 1. 合法性校验 (killId 是 sessionId_skuId)
        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);

        String redisTo = ops.get(killId);
        if (StringUtils.isEmpty(redisTo)) {
            return null;
        } else {
            SeckillSkuRedisTo seckillSkuRedisTo = JSON.parseObject(redisTo, SeckillSkuRedisTo.class);
            // 1.校验时间合法性
            Long startTime = seckillSkuRedisTo.getStartTime();
            Long endTime = seckillSkuRedisTo.getEndTime();
            long time = new Date().getTime();
            if (time >= startTime && time <= endTime) {
                // 2.校验随机码 (killId的话 上面校验过了)
                if (key.equals(seckillSkuRedisTo.getRandomCode())){

                    // 3.验证购物数量是否合理
                    if (num <= seckillSkuRedisTo.getSeckillLimit().intValue()) {
                        // 4.验证是否已经购买过了/ 防止恶意购买 重复购买 (幂等性处理)
                        MemberResponseVO memberResponseVO = LoginUserInterceptor.loginUser.get();
                        String redisKey = memberResponseVO.getId() + "_" + killId;
                        // 自动过期
                        Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(redisKey, num.toString(), endTime - startTime, TimeUnit.MILLISECONDS);


                        if (aBoolean) {
                            // 说明从来没买过
                            RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + key);
                            try {

                                boolean b = semaphore.tryAcquire(num, 100, TimeUnit.MILLISECONDS);
                                if (b) {
                                    // 秒杀成功
                                    // 快速下单 发送mq消息
                                    String orderSn = IdWorker.getTimeId();
                                    SeckillOrderTo seckillOrderTo = new SeckillOrderTo();
                                    seckillOrderTo.setOrderSn(orderSn);
                                    seckillOrderTo.setMemberId(memberResponseVO.getId());
                                    seckillOrderTo.setNum(num);
                                    seckillOrderTo.setPromotionSessionId(seckillSkuRedisTo.getPromotionSessionId());
                                    seckillOrderTo.setSkuId(seckillSkuRedisTo.getSkuId());
                                    seckillOrderTo.setSeckillPrice(seckillSkuRedisTo.getSeckillPrice());

                                    rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", seckillOrderTo);


                                    return orderSn;
                                } else {
                                    return null;
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();

                                return null;
                            }


                        } else {
                            // 已买
                            return null;

                        }

                    }


                } else {
                    return null;
                }


            } else {
                return null;
            }






        }
        return null;
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
            Boolean aBoolean = redisTemplate.hasKey(SESSIONS_CACHE_PREFIX + key);

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
