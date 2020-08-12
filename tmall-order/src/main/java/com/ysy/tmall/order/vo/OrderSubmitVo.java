package com.ysy.tmall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单提交数据
 * @anthor silenceYin
 * @date 2020/8/12 - 23:28
 */
@Data
public class OrderSubmitVo {
    /**
     * 收货地址id
     */
    private Long addId;

    /**
     * 支付方式
     */
    private Integer payType;

    /**
     * 提交token 防重复提交
     */
    private String orderToken;
    // 无需提交商品 从购物车实时取 (JD做法)
    // TODO 优惠发票

    /**
     * 应付总额 (验证价格 用户友好程度)
     */
    private BigDecimal payPrice;

    // 用户相关信息 从session中取
}
