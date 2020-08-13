package com.ysy.tmall.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车物品
 * @anthor silenceYin
 * @date 2020/7/26 - 2:10
 */
@Data
public class CartItem {
    /**
     * spu 商品id
     */
    private Long skuId;

    /**
     * 是否勾选 默认勾选
     */
    private Boolean check = true;

    /**
     * 标题
     */
    private String title;

    /**
     * 图片
     */
    private String image;

    /**
     * 套餐属性
     */
    private List<String> skuAttr;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 数量
     */
    private Integer count;

    /**
     * 总价合集 数量 * 价格
     */
    private BigDecimal totalPrice;

    public BigDecimal getTotalPrice() {
        return this.price.multiply(new BigDecimal(this.count));

    }
}
