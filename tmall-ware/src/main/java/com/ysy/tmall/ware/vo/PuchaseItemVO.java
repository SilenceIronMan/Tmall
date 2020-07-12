package com.ysy.tmall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @anthor silenceYin
 * @date 2020/7/8 - 21:25
 */
@Data
public class PuchaseItemVO {

    /**
     * 采購明細id
     */
    @NotNull
    private Long itemId;

    /**
     * 更新狀態
     */
    @NotNull
    private Integer status;

    /**
     * 原因。
     */
    private String reason;
}
