package com.ysy.tmall.product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ysy.tmall.common.utils.PageUtils;
import com.ysy.tmall.product.entity.AttrEntity;
import com.ysy.tmall.product.entity.ProductAttrValueEntity;
import com.ysy.tmall.product.vo.AttrRelationVo;
import com.ysy.tmall.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author SilenceIronMan
 * @email yinshiyu_2008@126.com
 * @date 2020-06-27 21:47:45
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVo attr);

    PageUtils queryBaseAttr(Map<String, Object> params, Long catelogId, String attrType);

    AttrVo getAttrInfo(Long attrId);

    void updateAttr(AttrVo attr);

    List<AttrEntity> listAttrRelation(Long attrGroupId);

    PageUtils getAttrNoRelation(Map<String, Object> params, Long attrgroupId);

}

