package com.ysy.tmall.product.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @anthor silenceYin
 * @date 2020/7/20 - 5:36
 */
@Controller
public class ItemController {

    /**
     * 商品详情页面
     * @param skuId
     * @return
     */
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId) {

        return "item";
    }
}
