package com.ysy.tmall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ysy.tmall.common.utils.PageUtils;
import com.ysy.tmall.common.utils.Query;
import com.ysy.tmall.ware.dao.PurchaseDao;
import com.ysy.tmall.ware.entity.PurchaseDetailEntity;
import com.ysy.tmall.ware.entity.PurchaseEntity;
import com.ysy.tmall.ware.service.PurchaseDetailService;
import com.ysy.tmall.ware.service.PurchaseService;
import com.ysy.tmall.ware.vo.PurchaseVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Resource
    private PurchaseDetailService purchaseDetailService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceivePage(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> wrapper = new QueryWrapper<>();
        // 采购单新建和已分配
        wrapper.eq("status", 0).or().eq("status", 1);
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);

    }

    @Override
    @Transactional
    public void mergePurchase(PurchaseVO purchaseVO) {

        Long purchaseId = purchaseVO.getPurchaseId();
        List<Long> items = purchaseVO.getItems();

        // TODO 采购需求必须是 0 采购清单必须是 0,1
        if (Objects.isNull(purchaseId)) { //合并没有选择采购单id 则新建采购单
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            purchaseEntity.setStatus(0); //新建
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        } else {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setId(purchaseId);
            purchaseEntity.setUpdateTime(new Date());
            purchaseEntity.setStatus(1); //已分配
            this.updateById(purchaseEntity);
        }
        Long purchaseFinalId = purchaseId;
        List<PurchaseDetailEntity> purchaseDetailEntities = items.stream().map(i -> {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setId(i);
            purchaseDetailEntity.setPurchaseId(purchaseFinalId);
            purchaseDetailEntity.setStatus(1);
            return purchaseDetailEntity;
        }).collect(Collectors.toList());
        purchaseDetailService.updateBatchById(purchaseDetailEntities);
    }

    @Override
    @Transactional
    public void received(List<Long> purchaseIds) {

        // 状态小于领取状态的
        List<PurchaseEntity> purchaseEntities = purchaseIds.stream().map(p -> {
            PurchaseEntity byId = this.getById(p);
            return byId;
        }).filter(e -> (e.getStatus() == 0 || e.getStatus() == 1)
                //     FIXME     && e.getAssigneeName().equals("admin") 当前用户  只能领取自己的采购单
        ).map(e -> {
                    e.setStatus(2);  //更改状态为领取
                    e.setUpdateTime(new Date());
                    return e;
                }
        ).collect(Collectors.toList());

        this.updateBatchById(purchaseEntities);

        // 更新采购需求的状态(因为采购单更新了)
        purchaseIds.stream().forEach(p -> {
                    List<PurchaseDetailEntity> entities = purchaseDetailService.list(new QueryWrapper<PurchaseDetailEntity>().eq("purchase_id", p));
                    List<PurchaseDetailEntity> purchaseDetailEntities = entities.stream().map(e -> {
                        PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                        purchaseDetailEntity.setId(e.getId());
                        purchaseDetailEntity.setStatus(2);
                        return purchaseDetailEntity;
                    }).collect(Collectors.toList());

                    purchaseDetailService.updateBatchById(purchaseDetailEntities);
                }

        );

    }

}
