package com.ysy.tmall.search.controller;

import com.ysy.tmall.common.exception.BizCodeEnum;
import com.ysy.tmall.common.to.producttocoupon.to.SkuEsModel;
import com.ysy.tmall.common.utils.R;
import com.ysy.tmall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * @anthor silenceYin
 * @date 2020/7/13 - 1:26
 */
@RequestMapping("/search")
@RestController
@Slf4j
public class ElasticSearchController {

    @Resource
    private ProductSaveService productSaveService;
    /**
     * 上架商品
     * @param skuEsModelList
     * @return
     */
    @RequestMapping("/product/save")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModelList) {
        boolean flg = false;
        try {
            flg = productSaveService.prductStatusUp(skuEsModelList);

        } catch (IOException e) {
            log.error("ElasticSearchController 商品上架错误 {}", e);
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_UP_EXCEPTION.getMessage());
        }

        if (flg) {
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_UP_EXCEPTION.getMessage());
        } else {
            return R.ok();
        }

    }

}
