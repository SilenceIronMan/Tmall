package com.ysy.tmall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.ysy.tmall.product.entity.CategoryBrandRelationEntity;
import com.ysy.tmall.product.service.CategoryBrandRelationService;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ysy.tmall.common.utils.PageUtils;
import com.ysy.tmall.common.utils.Query;

import com.ysy.tmall.product.dao.CategoryDao;
import com.ysy.tmall.product.entity.CategoryEntity;
import com.ysy.tmall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Resource
    private CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1.查出所有分类
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);

        //2.生成树状结构
        //一级目录 Category
        List<CategoryEntity> categoryTreeList = categoryEntities.stream().filter(c -> c.getParentCid().equals(0L)).map(menu -> {
            menu.setChildren(getChildren(menu, categoryEntities));
            return menu;
        }).sorted((menu1, menu2) ->
                (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort())
        ).collect(Collectors.toList());


        return categoryTreeList;

    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        // 1.TODO 检查是否有子菜单
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {

        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);


        return parentPath.toArray(new Long[parentPath.size()]);
    }

    @Override
    @Transactional
    public void updateCascade(CategoryEntity category) {
        updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }


    /**
     * 递归记录自身id 和父id
     * @param catelogId
     * @param paths 存储容器
     * @return
     */
    private List<Long> findParentPath(Long catelogId, List<Long> paths) {

        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        Long parentCid = byId.getParentCid();
        if (parentCid != 0) {
            findParentPath(parentCid, paths);
        }
        Collections.reverse(paths);
        return paths;
    }

    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {

        List<CategoryEntity> children = all.stream().filter(c ->
                c.getParentCid().equals(root.getCatId())
        ).map(menu -> {
            menu.setChildren(getChildren(menu, all));
            return menu;
        }).sorted((menu1, menu2) ->
                (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort())
        ).collect(Collectors.toList());
        return children;

    }

}
