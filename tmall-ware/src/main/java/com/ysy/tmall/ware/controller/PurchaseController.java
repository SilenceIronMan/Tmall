package com.ysy.tmall.ware.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.ysy.tmall.ware.vo.PuchaseDoneVO;
import com.ysy.tmall.ware.vo.PurchaseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ysy.tmall.ware.entity.PurchaseEntity;
import com.ysy.tmall.ware.service.PurchaseService;
import com.ysy.tmall.common.utils.PageUtils;
import com.ysy.tmall.common.utils.R;



/**
 * 采购信息
 *
 * @author SilenceIronMan
 * @email yinshiyu_2008@126.com
 * @date 2020-06-27 21:43:42
 */
@RestController
@RequestMapping("ware/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;


    /**
     * 完成采購
     */
    @RequestMapping("/done")
    //@RequiresPermissions("ware:purchase:save")
    public R done(@RequestBody PuchaseDoneVO puchaseDoneVO){

        purchaseService.done(puchaseDoneVO);

        return R.ok();
    }

    /**
     * 领取采购单
     */
    @RequestMapping("/received")
    //@RequiresPermissions("ware:purchase:save")
    public R received(@RequestBody List<Long> purchaseIds){

        purchaseService.received(purchaseIds);

        return R.ok();
    }

    /**
     * 合并需求单并分配采购单
     */
    @RequestMapping("/merge")
    //@RequiresPermissions("ware:purchase:save")
    public R mergePurchase(@RequestBody PurchaseVO purchaseVO){
        purchaseService.mergePurchase(purchaseVO);

        return R.ok();
    }


    /**
     * 未领取的采购单
     */
    @RequestMapping("/unreceive/list")
    //@RequiresPermissions("ware:purchase:list")
    public R unreceiveList(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPageUnreceivePage(params);

        return R.ok().put("page", page);
    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:purchase:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:purchase:info")
    public R info(@PathVariable("id") Long id){
		PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:purchase:save")
    public R save(@RequestBody PurchaseEntity purchase){

        purchase.setCreateTime(new Date());
        purchase.setUpdateTime(new Date());
		purchaseService.save(purchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:purchase:update")
    public R update(@RequestBody PurchaseEntity purchase){
		purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:purchase:delete")
    public R delete(@RequestBody Long[] ids){
		purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
