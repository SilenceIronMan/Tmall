package com.ysy.tmall.product.dao;

import com.ysy.tmall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 *
 * @author SilenceIronMan
 * @date 2020-06-27 21:47:45
 * @email yinshiyu_2008@126.com
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {

}
