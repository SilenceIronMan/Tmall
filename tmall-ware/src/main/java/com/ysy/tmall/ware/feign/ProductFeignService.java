package com.ysy.tmall.ware.feign;

import com.ysy.tmall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @anthor silenceYin
 * @date 2020/8/11 - 23:34
 */
@FeignClient("tmall-product")
public interface ProductFeignService {

    /**
     * /product/skuinfo/info/{skuId}
     *
     * 1)、让所有请求过网关；
     *    1、@FeignClient("tmall-gateway")：给glmall-gateway所在的机器发请求
     *    2、/api/product/skuinfo/info/{skuId}
     * 2）、直接让后台指定服务处理
     *    1、@FeignClient("tmall-product")
     *    2、/product/skuinfo/info/{skuId}
     * @param skuId
     * @return
     */
    @RequestMapping("/product/skuinfo/info/{skuId}")
    R info(@PathVariable("skuId") Long skuId);


}
