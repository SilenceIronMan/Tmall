package com.ysy.tmall.product.feign;

import org.springframework.cloud.openfeign.FeignClient;

/**
 * @anthor silenceYin
 * @date 2020/7/5 - 21:53
 */
@FeignClient(value = "tmall-member", path = "/member")
public interface MemberFeigService {
}
