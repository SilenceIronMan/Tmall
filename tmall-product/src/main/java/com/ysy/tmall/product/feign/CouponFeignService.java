package com.ysy.tmall.product.feign;

import com.ysy.tmall.common.to.SkuReductionTO;
import com.ysy.tmall.common.to.SpuBoundsTO;
import com.ysy.tmall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @anthor silenceYin
 * @date 2020/7/5 - 20:23
 */
@FeignClient(value = "tmall-coupon", path = "/coupon")
public interface CouponFeignService {

    /**
     * 保存spu积分
     * @param spuBounds
     * @return
     */
    @RequestMapping("/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundsTO spuBounds);

    /**
     * 保存sku满减
     * @param skuFullReduction
     * @return
     */
    @RequestMapping("/skufullreduction/saveskureduction")
    R saveSkuReduction(@RequestBody SkuReductionTO skuFullReduction);

}
