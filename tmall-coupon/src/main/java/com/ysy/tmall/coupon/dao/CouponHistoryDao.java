package com.ysy.tmall.coupon.dao;

import com.ysy.tmall.coupon.entity.CouponHistoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券领取历史记录
 * 
 * @author SilenceIronMan
 * @email yinshiyu_2008@126.com
 * @date 2020-06-26 21:41:17
 */
@Mapper
public interface CouponHistoryDao extends BaseMapper<CouponHistoryEntity> {
	
}
