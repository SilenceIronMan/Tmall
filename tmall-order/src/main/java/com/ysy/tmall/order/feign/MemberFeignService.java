package com.ysy.tmall.order.feign;

import com.ysy.tmall.order.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @anthor silenceYin
 * @date 2020/8/6 - 22:00
 */
@FeignClient("tmall-member")
public interface MemberFeignService {

    @GetMapping("/member/memberreceiveaddress/{memberId}/addresses")
    List<MemberAddressVo> listAddress(@PathVariable("memberId") Long memberId);
}
