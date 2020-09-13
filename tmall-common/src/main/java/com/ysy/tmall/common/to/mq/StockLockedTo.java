package com.ysy.tmall.common.to.mq;

import lombok.Data;

/**
 * 库存锁定TO
 * @anthor silenceYin
 * @date 2020/8/20 - 1:21
 */
@Data
public class StockLockedTo {
    /**
     * 库存工作单ID
     */
    private Long id;

    /**
     * 库存工作单详情
     */
    private StockDetailTo detail;
}
