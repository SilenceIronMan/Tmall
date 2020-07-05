package com.ysy.tmall.common.to.producttocoupon;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @anthor silenceYin
 * @date 2020/7/5 - 21:55
 */
/**
 * 会员价格
 */
@Data
public class MemberPrice {

    private Long id;
    private String name;
    private BigDecimal price;

}
