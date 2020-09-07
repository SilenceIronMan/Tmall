package com.ysy.tmall.seckill.feign;

import com.ysy.tmall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 优惠券远程调用
 * @anthor silenceYin
 * @date 2020/9/1 - 1:12
 */
@FeignClient("tmall-coupon")
public interface CouponFeignService {

    /**
     * 列表
     */
    @GetMapping("coupon/seckillsession/latest3DaysSession")
    R getLatest3DaysSession();

}
