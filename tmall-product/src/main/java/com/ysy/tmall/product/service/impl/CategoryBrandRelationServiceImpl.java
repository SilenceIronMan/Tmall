package com.ysy.tmall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.ysy.tmall.product.dao.BrandDao;
import com.ysy.tmall.product.dao.CategoryDao;
import com.ysy.tmall.product.entity.BrandEntity;
import com.ysy.tmall.product.entity.CategoryEntity;
import com.ysy.tmall.product.service.BrandService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ysy.tmall.common.utils.PageUtils;
import com.ysy.tmall.common.utils.Query;

import com.ysy.tmall.product.dao.CategoryBrandRelationDao;
import com.ysy.tmall.product.entity.CategoryBrandRelationEntity;
import com.ysy.tmall.product.service.CategoryBrandRelationService;

import javax.annotation.Resource;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

    @Resource
    private BrandDao brandDao;

    @Resource
    private CategoryDao categoryDao;

    @Resource
    private BrandService brandService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {
        Long brandId = categoryBrandRelation.getBrandId();
        Long catelogId = categoryBrandRelation.getCatelogId();
        BrandEntity brandEntity = brandDao.selectById(brandId);
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);
        categoryBrandRelation.setBrandName(brandEntity.getName());
        categoryBrandRelation.setCatelogName(categoryEntity.getName());
        baseMapper.insert(categoryBrandRelation);

    }

    @Override
    public void updateBrand(Long brandId, String name) {
        CategoryBrandRelationEntity categoryBrandRelationEntity = new CategoryBrandRelationEntity();
        categoryBrandRelationEntity.setBrandId(brandId);
        categoryBrandRelationEntity.setBrandName(name);
        update(categoryBrandRelationEntity, new UpdateWrapper<CategoryBrandRelationEntity>().eq("brand_Id", brandId));
    }

    @Override
    public void updateCategory(Long catId, String name) {

        baseMapper.updateCategory(catId, name);

    }


    /**
     * 根據分類id獲取品牌詳情
     * @param catId
     * @return
     */
    @Override
    public List<BrandEntity> getBrandList(Long catId) {
        List<CategoryBrandRelationEntity> categoryBrandRelationEntities = this.baseMapper
                .selectList(new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId));
        List<BrandEntity> brandEntities = categoryBrandRelationEntities.stream()
                .map(relation ->
                        brandService.getById(relation.getBrandId()
                        )).collect(Collectors.toList());

        return brandEntities;
    }


}
