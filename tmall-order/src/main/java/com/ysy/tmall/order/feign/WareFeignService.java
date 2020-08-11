package com.ysy.tmall.order.feign;

import com.ysy.tmall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 仓库
 * @anthor silenceYin
 * @date 2020/8/8 - 23:55
 */
@FeignClient("tmall-ware")
public interface WareFeignService {


    @PostMapping("/ware/waresku/hasstock")
    R getSkuHasStock(@RequestBody List<Long> skuIds);

}
