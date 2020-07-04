package com.ysy.tmall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ysy.tmall.common.constant.ProductConstant;
import com.ysy.tmall.common.utils.PageUtils;
import com.ysy.tmall.common.utils.Query;
import com.ysy.tmall.product.dao.AttrAttrgroupRelationDao;
import com.ysy.tmall.product.dao.AttrDao;
import com.ysy.tmall.product.dao.AttrGroupDao;
import com.ysy.tmall.product.dao.CategoryDao;
import com.ysy.tmall.product.entity.AttrAttrgroupRelationEntity;
import com.ysy.tmall.product.entity.AttrEntity;
import com.ysy.tmall.product.entity.AttrGroupEntity;
import com.ysy.tmall.product.entity.CategoryEntity;
import com.ysy.tmall.product.service.AttrService;
import com.ysy.tmall.product.service.CategoryService;
import com.ysy.tmall.product.vo.AttrRelationVo;
import com.ysy.tmall.product.vo.AttrRespVo;
import com.ysy.tmall.product.vo.AttrVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Resource
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Resource
    private AttrGroupDao attrGroupDao;

    @Resource
    private AttrDao attrDao;

    @Resource
    private CategoryDao categoryDao;

    @Resource
    private CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();

        BeanUtils.copyProperties(attr, attrEntity);
        // 1.保存基本数据
        this.save(attrEntity);
        // 销售属性没有分组 不需要存中间表 (为什么会有中间表也是因为分组的原因 才把字段拆开 使表更合理化)
        if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            // 2.保存关联关系
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();

            attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
            attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
        }
    }

    @Override
    public PageUtils queryBaseAttr(Map<String, Object> params, Long catelogId, String attrType) {
        IPage<AttrEntity> page;
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("attr_type", "base".equalsIgnoreCase(attrType) ?
                ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() : ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());
        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            wrapper.and(w -> {
                w.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        if (catelogId != 0) {
            wrapper.eq("catelog_id", catelogId);
        }
        page = this.page(
                new Query<AttrEntity>().getPage(params),
                wrapper
        );
        List<AttrEntity> records = page.getRecords();
        List<AttrRespVo> respVos = records.stream().map(attrEntity -> {
            AttrRespVo attrRespVo = new AttrRespVo();
            BeanUtils.copyProperties(attrEntity, attrRespVo);

            // 基本属性才设置
            if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
                // 分组name
                AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationDao.selectOne
                        (new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
                // 因为分组id不是必须项目  属性可能还没有分组
                if (Objects.nonNull(attrAttrgroupRelationEntity)) {
                    Long attrGroupId = attrAttrgroupRelationEntity.getAttrGroupId();
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupId);
                    attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
            // 分类name
            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            // 非空判断
            if (Objects.nonNull(categoryEntity)) {
                String name = categoryEntity.getName();
                attrRespVo.setCatelogName(name);
            }


            //attrGroupDao.selectById(attrEntity.)
            return attrRespVo;
        }).collect(Collectors.toList());


        PageUtils pageUtils = new PageUtils(page);
        pageUtils.setList(respVos);
        return pageUtils;

    }

    @Override
    public AttrVo getAttrInfo(Long attrId) {
        AttrRespVo attrRespVo = new AttrRespVo();
        AttrEntity attrEntity = this.getById(attrId);
        BeanUtils.copyProperties(attrEntity, attrRespVo);

        AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationDao
                .selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>()
                        .eq("attr_id", attrEntity.getAttrId()));
        if (Objects.nonNull(attrAttrgroupRelationEntity)) {
            attrRespVo.setAttrGroupId(attrAttrgroupRelationEntity.getAttrGroupId());
        }

        Long[] catelogPath = categoryService.findCatelogPath(attrEntity.getCatelogId());
        attrRespVo.setCatelogPath(catelogPath);
        return attrRespVo;
    }

    @Override
    public void updateAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.updateById(attrEntity);


        // base attr 更新 分组
        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
            Long attrId = attr.getAttrId();
            Integer attrAttrgroupCount = attrAttrgroupRelationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>()
                    .eq("attr_id", attrId));
            // 分组有可能之前就没有
            if (attrAttrgroupCount > 0) {
                attrAttrgroupRelationDao.update(attrAttrgroupRelationEntity,
                        new UpdateWrapper<AttrAttrgroupRelationEntity>()
                                .eq("attr_id", attrId));
            } else {
                attrAttrgroupRelationEntity.setAttrId(attrId);
                attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
            }

        }
    }


    @Override
    public List<AttrEntity> listAttrRelation(Long attrGroupId) {
        List<AttrAttrgroupRelationEntity> attrgroupRelationEntityList = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>()
                .eq("attr_group_id", attrGroupId));

        List<Long> attrIds = attrgroupRelationEntityList.stream()
                .map((attrGroup -> attrGroup.getAttrId())).collect(Collectors.toList());
        if (attrIds.size() == 0){
            return null;
        }
        Collection<AttrEntity> attrGroupEntities = this.listByIds(attrIds);
        return (List<AttrEntity>) attrGroupEntities;
    }

    @Override
    public PageUtils getAttrNoRelation(Map<String, Object> params, Long attrgroupId) {

        // 1. 当前分组只能关联自己所属分类的所有属性
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();
        Long attrGroupId = attrGroupEntity.getAttrGroupId();

        // 2 当前分类下的所有分组
        List<AttrGroupEntity> attrGroupEntities = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().
                eq("catelog_id", catelogId));
        List<Long> attrGroupIds = attrGroupEntities.stream().map(attrGroup ->
                attrGroup.getAttrGroupId()
        ).collect(Collectors.toList());

        //3 这些分组下已被使用的所有属性
        List<AttrAttrgroupRelationEntity> attrgroupRelationEntityList = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>()
                .in("attr_group_id", attrGroupIds));
        List<Long> attrIds = attrgroupRelationEntityList.stream().map(attrAttrgroupRelationEntity ->
                attrAttrgroupRelationEntity.getAttrId()).collect(Collectors.toList());

        //4 从当前分类所有属性中删除这些属性 (并且是基础属性)
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>().notIn("attr_id", attrIds)
                        .eq("attr_type", ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()));

        return new PageUtils(page);
    }

}
