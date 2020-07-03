package com.ysy.tmall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ysy.tmall.common.utils.PageUtils;
import com.ysy.tmall.product.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author SilenceIronMan
 * @email yinshiyu_2008@126.com
 * @date 2020-06-27 21:47:45
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

    void removeMenuByIds(List<Long> asList);

    /**
     * 获取分类id完整路径
     * @param catelogId
     * @return
     */
    Long[] findCatelogPath(Long catelogId);

    void updateCascade(CategoryEntity category);
}

