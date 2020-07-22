package com.ysy.tmall.product.vo.web;

import lombok.Data;

import java.util.List;

/**
 * @anthor silenceYin
 * @date 2020/7/20 - 6:40
 */
@Data
public class SkuItemSaleAttrVo {
    private Long attrId;
    private String attrName;
    private List<AttrValueWithSkuIdVo> attrValues;
}
