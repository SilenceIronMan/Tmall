package com.ysy.tmall.member.Feign;

import com.ysy.tmall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @anthor silenceYin
 * @date 2020/6/26 - 22:43
 */
@FeignClient(value = "tmall-coupon", path = "coupon/coupon")
public interface CouponFeignService {


    @RequestMapping("/member/list")
    R memberCoupons();
}
