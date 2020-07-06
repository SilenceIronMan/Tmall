package com.ysy.tmall.common.to.producttocoupon;

import lombok.Data;

import java.math.BigDecimal;

/**
 * spu積分
 * @anthor silenceYin
 * @date 2020/7/5 - 20:28
 */
@Data
public class SpuBoundsTO {

    /**
     * spuid
     */
    private Long spuId;
    /**
     * 购物积分
     */
    private BigDecimal buyBounds;

    /**
     * 成长积分
     */
    private BigDecimal growBounds;
    /**
     * 优惠生效情况[1111（四个状态位，从右到左）;0 - 无优惠，成长积分是否赠送;1 - 无优惠，购物积分是否赠送;2 - 有优惠，成长积分是否赠送;3 - 有优惠，购物积分是否赠送【状态位0：不赠送，1：赠送】]
     */
    private Integer work;

}
