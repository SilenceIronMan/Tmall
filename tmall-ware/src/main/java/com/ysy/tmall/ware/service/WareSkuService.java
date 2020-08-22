package com.ysy.tmall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ysy.tmall.common.to.SkuHasStockVo;
import com.ysy.tmall.common.to.mq.OrderTo;
import com.ysy.tmall.common.to.mq.StockLockedTo;
import com.ysy.tmall.common.utils.PageUtils;
import com.ysy.tmall.ware.entity.WareSkuEntity;
import com.ysy.tmall.ware.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author SilenceIronMan
 * @email yinshiyu_2008@126.com
 * @date 2020-06-27 21:43:42
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 添加库存
     * @param skuId
     * @param wareId
     * @param stock
     */
    void addStocks(Long skuId, Long wareId, Integer stock);

    /**
     * 根据skuid获取是否有库存
     * @param skuIds
     * @return
     */
    List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds);

    /**
     * 锁定库存
     * @param wareSkuLockVo
     * @return
     */
    Boolean orderLockStock(WareSkuLockVo wareSkuLockVo);

    List<Long> listWareIdHasSkuStock(Long skuId);

    Long lockSkuStock(Long skuId, Long wareId, Integer num);

    /**
     * 根据库存工作单解锁
     * @param to
     */
    void unlockStock(StockLockedTo to);

    /**
     * 根据订单解锁
     * @param orderTo
     */
    void unlockStock(OrderTo orderTo);
}

