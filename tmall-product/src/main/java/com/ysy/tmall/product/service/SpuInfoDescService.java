package com.ysy.tmall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ysy.tmall.common.utils.PageUtils;
import com.ysy.tmall.product.entity.SpuInfoDescEntity;

import java.util.Map;

/**
 * spu信息介绍
 *
 * @author SilenceIronMan
 * @email yinshiyu_2008@126.com
 * @date 2020-06-24 01:03:28
 */
public interface SpuInfoDescService extends IService<SpuInfoDescEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

