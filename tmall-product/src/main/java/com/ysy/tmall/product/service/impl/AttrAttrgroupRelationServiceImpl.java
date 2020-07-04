package com.ysy.tmall.product.service.impl;

import com.ysy.tmall.product.vo.AttrRelationVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ysy.tmall.common.utils.PageUtils;
import com.ysy.tmall.common.utils.Query;

import com.ysy.tmall.product.dao.AttrAttrgroupRelationDao;
import com.ysy.tmall.product.entity.AttrAttrgroupRelationEntity;
import com.ysy.tmall.product.service.AttrAttrgroupRelationService;


@Service("attrAttrgroupRelationService")
public class AttrAttrgroupRelationServiceImpl extends ServiceImpl<AttrAttrgroupRelationDao, AttrAttrgroupRelationEntity> implements AttrAttrgroupRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrAttrgroupRelationEntity> page = this.page(
                new Query<AttrAttrgroupRelationEntity>().getPage(params),
                new QueryWrapper<AttrAttrgroupRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void addAttrRelation(List<AttrRelationVo> attrRelations) {
        List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities = attrRelations.stream().map(attrRelationVo ->
                {
                    AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
                    BeanUtils.copyProperties(attrRelationVo, attrAttrgroupRelationEntity);
                    return attrAttrgroupRelationEntity;
                }
        ).collect(Collectors.toList());
        this.saveOrUpdateBatch(attrAttrgroupRelationEntities);

    }

    @Override
    public void deleteRelation(List<AttrRelationVo> attrRelations) {
        List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities = attrRelations.stream().map(attrRelationVo ->
                {
                    AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
                    BeanUtils.copyProperties(attrRelationVo, attrAttrgroupRelationEntity);
                    return attrAttrgroupRelationEntity;
                }
        ).collect(Collectors.toList());

        this.baseMapper.deleteBatchRelation(attrAttrgroupRelationEntities);

    }

}
