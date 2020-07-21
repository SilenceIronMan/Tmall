package com.ysy.tmall.product.web;

import com.ysy.tmall.product.service.SkuInfoService;
import com.ysy.tmall.product.vo.web.SkuItemVo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;

/**
 * @anthor silenceYin
 * @date 2020/7/20 - 5:36
 */
@Controller
public class ItemController {


    @Resource
    private SkuInfoService skuInfoService;

    /**
     * 商品详情页面
     * @param skuId
     * @return
     */
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId, Model model) throws ExecutionException, InterruptedException {
        SkuItemVo vo=skuInfoService.item(skuId);
        model.addAttribute("item",vo);
        return "item";
    }
}
