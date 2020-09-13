package com.ysy.tmall.product.feign;

import com.ysy.tmall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @anthor silenceYin
 * @date 2020/9/11 - 2:12
 */
@FeignClient("tmall-seckill")
public interface SeckillFeignService {

    /**
     * 获取当前时间秒杀商品信息
     * @return
     */
    @GetMapping("/sku/seckill/{skuId}")
    R getSkuSeckillInfo(@PathVariable("skuId")Long skuId);

}
