package com.ysy.tmall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ysy.tmall.common.utils.PageUtils;
import com.ysy.tmall.ware.entity.PurchaseEntity;
import com.ysy.tmall.ware.vo.PurchaseVO;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author SilenceIronMan
 * @email yinshiyu_2008@126.com
 * @date 2020-06-27 21:43:42
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnreceivePage(Map<String, Object> params);

    void mergePurchase(PurchaseVO purchaseVO);

    void received(List<Long> purchaseIds);
}

