package com.ysy.tmall.order.feign;

import com.ysy.tmall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @anthor silenceYin
 * @date 2020/8/13 - 22:40
 */
@FeignClient("tmall-product")
public interface ProductFeignService {

    /**
     * 根据skuId获取spu信息
     */
    @GetMapping("/product/spuinfo/spuinfo/{skuId}")
    R getSpuInfoBySkuId(@PathVariable("skuId") Long skuId);
}
