package com.ysy.tmall.ware.vo;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @anthor silenceYin
 * @date 2020/7/8 - 21:24
 */
@Data
public class PuchaseDoneVO {
    /**
     * 采購單id
     */
    @NotNull
    private Long id;

    /**
     * 采購明細列表
     */
    @NotEmpty
    @Valid
    private List<PuchaseItemVO> items;
}
