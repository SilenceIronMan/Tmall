package com.ysy.tmall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @anthor silenceYin
 * @date 2020/8/6 - 21:50
 */
@Data
public class OrderItemVo {
    private Long skuId;

    private String title;

    private String image;

    //套餐属性
    private List<String> skuAttr;

    private BigDecimal price;

    private Integer count;

    private BigDecimal totalPrice;

    //库存状态  是否有货
    //private Boolean hasStock;

    //商品重量
    private BigDecimal weight;
}
