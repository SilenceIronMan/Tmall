package com.ysy.tmall.common.to.mq;

import lombok.Data;

/**
 * 库存宋丁任务详情
 * @anthor silenceYin
 * @date 2020/8/20 - 1:49
 */
@Data
public class StockDetailTo {
    /**
     * id
     */
    private Long id;
    /**
     * sku_id
     */
    private Long skuId;
    /**
     * sku_name
     */
    private String skuName;
    /**
     * 购买个数
     */
    private Integer skuNum;
    /**
     * 工作单id
     */
    private Long taskId;

    /**
     * 仓库id
     */
    private Long wareId;
    /**
     * 锁定状态
     */
    private Integer lockStatus;
}
