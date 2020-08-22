package com.ysy.tmall.common.to.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @anthor silenceYin
 * @date 2020/7/12 - 20:23
 */
@Data
public class SkuEsModel {
    /**
     * skuId
     */
    private Long skuId;

    /**
     * spuId
     */
    private Long spuId;

    /**
     * sku標題
     */
    private String skuTitle;

    /**
     * sku价格
     */
    private BigDecimal skuPrice;

    /**
     * sku图片
     */
    private String skuImg;

    /**
     * 销量
     */
    private Long saleCount;

    /**
     * 是否库存
     */
    private Boolean hasStock;

    /**
     * 热点评分
     */
    private Long hotScore;

    /**
     * 品牌id
     */
    private Long brandId;

    /**
     * 分类id
     */
    private Long catalogId;

    /**
     * 品牌名
     */
    private String brandName;

    /**
     * 品牌图片
     */
    private String brandImg;

    /**
     * 分类名
     */
    private String catalogName;

    /**
     * 属性
     */
    private List<Attr> attrs;

    @Data
    public static class Attr {
        private Long attrId;
        private String attrName;
        private String attrValue;

    }

}
