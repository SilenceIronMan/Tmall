package com.ysy.tmall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ysy.tmall.common.utils.PageUtils;
import com.ysy.tmall.product.entity.CommentReplayEntity;

import java.util.Map;

/**
 * 商品评价回复关系
 *
 * @author SilenceIronMan
 * @email yinshiyu_2008@126.com
 * @date 2020-06-27 21:47:45
 */
public interface CommentReplayService extends IService<CommentReplayEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

