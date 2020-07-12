package com.ysy.tmall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @anthor silenceYin
 * @date 2020/7/8 - 1:25
 */
@Data
public class PurchaseVO {
    /**
     * 整单id.
     */
    private Long purchaseId;

    /**
     * 合并项集合.
     */
    private List<Long> items;
}
