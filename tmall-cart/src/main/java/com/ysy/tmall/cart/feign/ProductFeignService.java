package com.ysy.tmall.cart.feign;

import com.ysy.tmall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @anthor silenceYin
 * @date 2020/7/26 - 21:55
 */
@FeignClient(value = "tmall-product", path = "/product")
public interface ProductFeignService {

    /**
     * 信息
     */
    @RequestMapping("/skuinfo/info/{skuId}")
    R info(@PathVariable("skuId") Long skuId);

    /**
     * 根据skuId 获取商品的属性组合
     * @param skuId
     * @return
     */
    @GetMapping("/skusaleattrvalue/stringlist/{skuId}")
    List<String> getSkuSaleAttrValues(@PathVariable("skuId") Long skuId);
}
