package com.ysy.tmall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ysy.tmall.common.utils.PageUtils;
import com.ysy.tmall.ware.entity.PurchaseDetailEntity;

import java.util.Map;

/**
 * 
 *
 * @author SilenceIronMan
 * @email yinshiyu_2008@126.com
 * @date 2020-06-27 21:43:42
 */
public interface PurchaseDetailService extends IService<PurchaseDetailEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

