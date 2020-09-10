package com.ysy.tmall.product.feign;

import org.springframework.cloud.openfeign.FeignClient;

/**
 * @anthor silenceYin
 * @date 2020/9/11 - 2:12
 */
@FeignClient("tmall-seckill")
public interface SeckillFeignService {
}
