package com.ysy.tmall.product.controller;

import com.ysy.tmall.common.utils.R;
import com.ysy.tmall.product.entity.CategoryEntity;
import com.ysy.tmall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

//import org.apache.shiro.authz.annotation.RequiresPermissions;



/**
 * 商品三级分类
 *
 * @author SilenceIronMan
 * @email yinshiyu_2008@126.com
 * @date 2020-06-27 21:47:45
 */
@RestController
@RequestMapping("product/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 查询所有分类以及子类，以树形结构组装起来
     */
    @RequestMapping("/list/tree")
    public R list(){

        List<CategoryEntity> categoryList =  categoryService.listWithTree();

        return R.ok().put("data", categoryList);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{catId}")
    //@RequiresPermissions("product:category:info")
    public R info(@PathVariable("catId") Long catId){
		CategoryEntity category = categoryService.getById(catId);

        return R.ok().put("data", category);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:category:save")
    public R save(@RequestBody CategoryEntity category){
		categoryService.save(category);

        return R.ok();
    }


    /**
     * 批量保存
     */
    @RequestMapping("/update/sort")
    //@RequiresPermissions("product:category:save")
    public R save(@RequestBody CategoryEntity[] category){
        categoryService.saveBatch(Arrays.asList(category));

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:category:update")
    public R update(@RequestBody CategoryEntity category){
		categoryService.updateById(category);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:category:delete")
    public R delete(@RequestBody Long[] catIds){
        // 1.删除前先判断是否有子id

        categoryService.removeMenuByIds(Arrays.asList(catIds));
        return R.ok();
    }

}
