package com.ysy.tmall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ysy.tmall.product.entity.AttrEntity;
import com.ysy.tmall.product.service.AttrAttrgroupRelationService;
import com.ysy.tmall.product.service.AttrService;
import com.ysy.tmall.product.service.CategoryService;
import com.ysy.tmall.product.vo.AttrRelationVo;
import com.ysy.tmall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.ysy.tmall.product.entity.AttrGroupEntity;
import com.ysy.tmall.product.service.AttrGroupService;
import com.ysy.tmall.common.utils.PageUtils;
import com.ysy.tmall.common.utils.R;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;


/**
 * 属性分组
 *
 * @author SilenceIronMan
 * @email yinshiyu_2008@126.com
 * @date 2020-06-27 21:47:45
 */
@RestController
@RequestMapping("product/attrgroup")
@Validated
public class AttrGroupController {
    @Resource
    private AttrGroupService attrGroupService;


    @Resource
    private AttrService attrService;


    @Resource
    private CategoryService categoryService;


    @Resource
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    /**
     * 获取当前属性分组所有关联属性
     * @param attrGroupId
     * @return
     */
    @GetMapping("/{attrGroupId}/attr/relation")
    public R getttrRelation(@PathVariable Long attrGroupId) {

        List<AttrEntity> attrRelationList= attrService.listAttrRelation(attrGroupId);

        return R.ok().put("data", attrRelationList);

    }

    /**
     * 获取当前分组同一分类id下所有未被关联的属性
     * @param params
     * @return
     */
    @GetMapping("/{attrgroupId}/noattr/relation")
    public R attrNoRelation(@RequestParam Map<String, Object> params, @PathVariable("attrgroupId") Long attrgroupId) {

        PageUtils page = attrService.getAttrNoRelation(params, attrgroupId);

        return R.ok().put("page", page);

    }
    /**
     * 添加属性关联
     * @param attrRelations
     * @return
     */
    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody @NotEmpty List<AttrRelationVo> attrRelations) {

        attrAttrgroupRelationService.addAttrRelation(attrRelations);

        return R.ok();

    }

    /**
     * 删除属性关联
     * @param attrRelations
     * @return
     */
    @PostMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody @NotEmpty List<AttrRelationVo> attrRelations) {

        attrAttrgroupRelationService.deleteRelation(attrRelations);

        return R.ok();

    }


    /**
     * 列表
     */
    @RequestMapping("/list/{catelogId}")
    //@RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params, @PathVariable("catelogId")Long catelogId){
        // PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPage(params, catelogId);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    //@RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);

        Long catelogId = attrGroup.getCatelogId();
        Long[] catelogPath = categoryService.findCatelogPath(catelogId);
        attrGroup.setCatelogPath(catelogPath);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
