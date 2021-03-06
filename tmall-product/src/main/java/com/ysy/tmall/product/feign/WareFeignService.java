package com.ysy.tmall.product.feign;

import com.ysy.tmall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @anthor silenceYin
 * @date 2020/7/13 - 0:06
 */
@FeignClient(value = "tmall-ware", path = "ware/waresku")
public interface WareFeignService {

    /**
     * 库存判断
     * @param skuIds
     * @return
     */
    @PostMapping("/hasstock")
    R getSkuHasStock(@RequestBody List<Long> skuIds);

}
