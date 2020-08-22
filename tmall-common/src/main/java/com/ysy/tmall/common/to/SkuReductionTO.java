package com.ysy.tmall.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * sku滿減
 * @anthor silenceYin
 * @date 2020/7/5 - 21:44
 */
@Data
public class SkuReductionTO {

    private Long skuId;
    private Integer fullCount;
    private BigDecimal discount;
    private Integer countStatus;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private Integer priceStatus;
    private List<MemberPrice> memberPrice;

}
