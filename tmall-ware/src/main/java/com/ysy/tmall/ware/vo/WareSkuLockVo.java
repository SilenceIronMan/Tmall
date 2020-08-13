package com.ysy.tmall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * 库存锁定vo
 * @anthor silenceYin
 * @date 2020/8/14 - 0:56
 */
@Data
public class WareSkuLockVo {

    /**
     * 訂單號
     */
    private String orderSn;

    /**
     * 锁定的库存信息
     */
    private List<OrderItemVo> locks;
}
