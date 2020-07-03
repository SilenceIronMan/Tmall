package com.ysy.tmall.product.dao;

import com.ysy.tmall.product.entity.CategoryBrandRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 品牌分类关联
 *
 * @author SilenceIronMan
 * @email yinshiyu_2008@126.com
 * @date 2020-06-27 21:47:45
 */
@Mapper
public interface CategoryBrandRelationDao extends BaseMapper<CategoryBrandRelationEntity> {

    int updateCategory(@Param("catelogId") Long catId, @Param("catelogName") String name);

}
