package com.ysy.tmall.ware.feign;

import com.ysy.tmall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @anthor silenceYin
 * @date 2020/8/22 - 15:58
 */
@FeignClient("tmall-order")
public interface OrderFeignService {

    /**
     * 根据订单号 获取订单状态
     * @param orderSn
     * @return
     */
    @GetMapping("/order/order/status/{orderSn}")
    R getOrderStatus(@PathVariable("orderSn") String orderSn);
}
