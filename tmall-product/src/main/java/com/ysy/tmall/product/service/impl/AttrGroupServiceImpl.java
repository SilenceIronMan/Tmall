package com.ysy.tmall.product.service.impl;

import com.ysy.tmall.product.entity.AttrEntity;
import com.ysy.tmall.product.service.AttrService;
import com.ysy.tmall.product.vo.AttrGroupWithAttrRespVo;
import com.ysy.tmall.product.vo.web.SpuItemAttrGroupVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ysy.tmall.common.utils.PageUtils;
import com.ysy.tmall.common.utils.Query;

import com.ysy.tmall.product.dao.AttrGroupDao;
import com.ysy.tmall.product.entity.AttrGroupEntity;
import com.ysy.tmall.product.service.AttrGroupService;

import javax.annotation.Resource;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Resource
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        IPage<AttrGroupEntity> page;
        String key = (String) params.get("key");
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(key)) {
            wrapper.and(obj -> {
                obj.eq("attr_group_id", key).or().like("attr_group_name", key);

            });
        }


        if (catelogId == 0) {
            page = this.page(
                    new Query<AttrGroupEntity>().getPage(params),
                    wrapper
            );
        } else {

            wrapper.eq("catelog_id", catelogId);
            // select * from pms_attr_group where catelog_id = ? and （attr_group_id = key or attr_group_name like '%key%'



            page = this.page(
                    new Query<AttrGroupEntity>().getPage(params),
                    wrapper
            );
        }
        return new PageUtils(page);
    }

    @Override
    public List<AttrGroupWithAttrRespVo> getAttrGroupWithattrList(Long catelogId) {
        List<AttrGroupEntity> attrGroupEntities = this.baseMapper.selectList(new QueryWrapper<AttrGroupEntity>()
                .eq("catelog_id", catelogId));

        List<AttrGroupWithAttrRespVo> attrGroupWithAttrRespVoList = attrGroupEntities.stream().map(attrGroupEntity -> {
            AttrGroupWithAttrRespVo attrGroupWithAttrRespVo = new AttrGroupWithAttrRespVo();
            BeanUtils.copyProperties(attrGroupEntity, attrGroupWithAttrRespVo);
            Long attrGroupId = attrGroupEntity.getAttrGroupId();
            List<AttrEntity> attrs = attrService.listAttrRelation(attrGroupId);
            attrGroupWithAttrRespVo.setAttrs(attrs);
            return attrGroupWithAttrRespVo;
        }).collect(Collectors.toList());

        return attrGroupWithAttrRespVoList;
    }

    @Override
    public List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId) {

        List<SpuItemAttrGroupVo> attrGroupWithAttrsBySpuId  = this.baseMapper.getAttrGroupWithAttrsBySpuId(spuId, catalogId);
        return attrGroupWithAttrsBySpuId;
    }

}
