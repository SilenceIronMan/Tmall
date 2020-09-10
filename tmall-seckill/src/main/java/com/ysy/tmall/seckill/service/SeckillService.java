package com.ysy.tmall.seckill.service;

import com.ysy.tmall.seckill.to.SeckillSkuRedisTo;

import java.util.List;

/**
 * @anthor silenceYin
 * @date 2020/9/1 - 1:09
 */
public interface SeckillService {
    void uploadSeckillSkuLatest3Days();

    List<SeckillSkuRedisTo> getCurrentSeckillSkus();

    SeckillSkuRedisTo getSkuSeckillInfo(Long skuId);
}
