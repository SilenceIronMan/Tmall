package com.ysy.tmall.order.vo;

import com.ysy.tmall.order.entity.OrderEntity;
import com.ysy.tmall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @anthor silenceYin
 * @date 2020/8/13 - 1:08
 */
@Data
public class OrderCreateTo {
    /**
     * 订单信息
     */
    private OrderEntity order;

    /**
     * 订单商品信息
     */
    private List<OrderItemEntity> items;

}
