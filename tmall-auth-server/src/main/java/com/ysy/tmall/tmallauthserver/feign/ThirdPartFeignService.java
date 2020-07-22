package com.ysy.tmall.tmallauthserver.feign;

import com.ysy.tmall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @anthor silenceYin
 * @date 2020/7/22 - 23:19
 */
@FeignClient(value = "tmall-third-party")
public interface ThirdPartFeignService {


    @GetMapping("/sms/sendCode")
    R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code);
}
