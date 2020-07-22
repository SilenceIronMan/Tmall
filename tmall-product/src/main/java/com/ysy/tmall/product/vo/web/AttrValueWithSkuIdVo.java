package com.ysy.tmall.product.vo.web;

import lombok.Data;

/**
 * 某个属性值关联的商品的id
 * 例如：  颜色：黑色 的 关联的sku
 * @anthor silenceYin
 * @date 2020/7/22 - 2:18
 */
@Data
public class AttrValueWithSkuIdVo {

    private String attrValue;
    private String skuIds; //多个以,分隔
}
