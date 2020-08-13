package com.ysy.tmall.order.feign;

import com.ysy.tmall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @anthor silenceYin
 * @date 2020/8/6 - 22:49
 */
@FeignClient("tmall-cart")
public interface CartFeignService {

    @GetMapping("/currentUserCartItems")
    List<OrderItemVo> getCurrentUserCartItems();

}
