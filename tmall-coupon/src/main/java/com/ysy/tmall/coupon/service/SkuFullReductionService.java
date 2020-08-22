package com.ysy.tmall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ysy.tmall.common.to.SkuReductionTO;
import com.ysy.tmall.common.utils.PageUtils;
import com.ysy.tmall.coupon.entity.SkuFullReductionEntity;

import java.util.Map;

/**
 * 商品满减信息
 *
 * @author SilenceIronMan
 * @email yinshiyu_2008@126.com
 * @date 2020-06-26 21:41:17
 */
public interface SkuFullReductionService extends IService<SkuFullReductionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuReduction(SkuReductionTO skuReductionTO);
}

