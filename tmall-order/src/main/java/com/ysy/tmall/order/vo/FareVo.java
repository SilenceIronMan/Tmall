package com.ysy.tmall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 运费
 * @anthor silenceYin
 * @date 2020/8/9 - 0:58
 */
@Data
public class FareVo {
    private MemberAddressVo address;
    private BigDecimal fare;
}
