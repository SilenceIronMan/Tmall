package com.ysy.tmall.member.feign;

import com.ysy.tmall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @anthor silenceYin
 * @date 2020/8/30 - 20:23
 */
@FeignClient("tmall-order")
public interface OrderFeignService {


    /**
     * 查询当前登录用户所有订单
     */
    @PostMapping("order/order/listWithItem")
    R listWithItem(@RequestBody Map<String, Object> params);
}
