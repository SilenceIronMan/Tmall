package com.ysy.tmall.search.service;

import com.ysy.tmall.common.to.to.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * @anthor silenceYin
 * @date 2020/7/13 - 1:30
 */
public interface ProductSaveService {
    /**
     * 上架商品信息存入 ES
     * @param skuEsModelList
     */
    boolean prductStatusUp(List<SkuEsModel> skuEsModelList)  throws IOException;
}
