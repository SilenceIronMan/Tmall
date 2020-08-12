package com.ysy.tmall.order.vo;

import com.ysy.tmall.order.entity.OrderEntity;
import lombok.Data;

/**
 * 下订单返回
 * @anthor silenceYin
 * @date 2020/8/13 - 0:15
 */
@Data
public class SubmitOrderResponseVo {
    /**
     * 订单信息
     */
    private OrderEntity order;

    /**
     * 状态码 0 成功
     */
    private  Integer code;
}
