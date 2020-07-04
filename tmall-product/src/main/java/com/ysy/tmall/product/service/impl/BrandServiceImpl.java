package com.ysy.tmall.product.service.impl;

import com.ysy.tmall.product.dao.CategoryBrandRelationDao;
import com.ysy.tmall.product.service.CategoryBrandRelationService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ysy.tmall.common.utils.PageUtils;
import com.ysy.tmall.common.utils.Query;

import com.ysy.tmall.product.dao.BrandDao;
import com.ysy.tmall.product.entity.BrandEntity;
import com.ysy.tmall.product.service.BrandService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Resource
    private CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                new QueryWrapper<BrandEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 保证荣誉字段的数据同步
     * @param brand
     */
    @Override
    @Transactional
    public void updateDetail(BrandEntity brand) {
        // 不仅更新本表还要更新关联表
        // pms_category_brand_relation
        updateById(brand);
        String name = brand.getName();
        if (StringUtils.isNotEmpty(name)) {

            categoryBrandRelationService.updateBrand(brand.getBrandId(), name);

        }

    }

}
