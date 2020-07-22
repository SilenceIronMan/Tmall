package com.ysy.tmall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * 封装页面查询条件参数
 * @anthor silenceYin
 * @date 2020/7/18 - 21:23
 */
@Data
public class SearchParam {

    /**
     * 关键字
     */
    private String keyword;

    /**
     * 三级分类id
     */
    private Long category3Id;

    /**
     * 排序条件
     *
     * sort = saleCount_asc/desc
     * sort = skuPrice_asc/desc
     * sort = hotScore_asc/desc
     *
     */
    private String sort;


    /**
     * hasStock(是否有货) skuPrice区间 品牌id catalog3id attrs
     *  hasStock = 0/1
     *
     *
     */
    private Integer hasStock = 1;
    /**
     * 价格区间 skuPrice = 1_500/_500/550_
     */
    private String skuPrice;

    /**
     * 可以多选
     */
    private List<Long> brandId;

    /**
     * 按照属性进行筛选 1_3G:4G:5G
     */
    private List<String> attrs;

    /**
     * 页码
     */
    private Integer pageNum = 1;


}
