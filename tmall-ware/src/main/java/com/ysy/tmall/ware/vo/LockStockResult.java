package com.ysy.tmall.ware.vo;

import lombok.Data;

/**
 * 库存锁定结果
 * @anthor silenceYin
 * @date 2020/8/14 - 1:02
 */
@Data
public class LockStockResult {

    /**
     * 商品skuid
     */
    private Long skuId;

    /**
     * 商品数量
     */
    private Integer num;

    /**
     * 是否锁定成功
     */
    private Boolean locked;


}
