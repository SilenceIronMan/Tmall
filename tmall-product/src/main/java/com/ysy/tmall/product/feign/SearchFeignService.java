package com.ysy.tmall.product.feign;

import com.ysy.tmall.common.to.producttocoupon.to.SkuEsModel;
import com.ysy.tmall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @anthor silenceYin
 * @date 2020/7/13 - 2:01
 */
@FeignClient(value = "tmall-search", path = "/search")
public interface SearchFeignService {

    @RequestMapping("/product/save")
    R productStatusUp(@RequestBody List<SkuEsModel> skuEsModelList);
}
