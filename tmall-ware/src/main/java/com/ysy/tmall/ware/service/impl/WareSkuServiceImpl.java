package com.ysy.tmall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ysy.tmall.common.exception.NoStockException;
import com.ysy.tmall.common.to.producttocoupon.SkuHasStockVo;
import com.ysy.tmall.common.utils.PageUtils;
import com.ysy.tmall.common.utils.Query;
import com.ysy.tmall.ware.dao.WareSkuDao;
import com.ysy.tmall.ware.entity.WareSkuEntity;
import com.ysy.tmall.ware.service.WareSkuService;
import com.ysy.tmall.ware.vo.OrderItemVo;
import com.ysy.tmall.ware.vo.WareSkuLockVo;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();

        String wareId = (String) params.get("wareId");
        if (StringUtils.isNotEmpty(wareId)) {
            wrapper.eq("ware_id", wareId);

        }

        String skuId = (String) params.get("skuId");
        if (StringUtils.isNotEmpty(skuId)) {
            wrapper.eq("sku_id", skuId);

        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void addStocks(Long skuId, Long wareId, Integer stock) {

        WareSkuEntity wareSkuEntity = this.baseMapper
                .selectOne(new QueryWrapper<WareSkuEntity>()
                        .eq("sku_id", skuId).eq("ware_id", wareId));
        if (Objects.isNull(wareSkuEntity)) {
            WareSkuEntity newWare = new WareSkuEntity();
            newWare.setSkuId(skuId);
            newWare.setWareId(wareId);
            newWare.setSkuName("");
            newWare.setStock(stock);
            this.save(newWare);
        } else {

            wareSkuEntity.setStock(wareSkuEntity.getStock() + stock);
            this.updateById(wareSkuEntity);

        }

    }

    @Override
    public List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds) {

        List<SkuHasStockVo> skuHasStockVos = skuIds.stream().map(skuId -> {
            SkuHasStockVo skuHasStockVo = new SkuHasStockVo();

            Long count = baseMapper.getSkuStock(skuId);
            skuHasStockVo.setSkuId(skuId);

            if (count > 0) {
                skuHasStockVo.setHasStock(true);
            } else {
                skuHasStockVo.setHasStock(false);
            }

            return skuHasStockVo;
        }).collect(Collectors.toList());
        return skuHasStockVos;
    }

    @Override
    @Transactional
    public Boolean orderLockStock(WareSkuLockVo wareSkuLockVo) {

        // 1.根据下单地址 找一个就近的仓库 锁定库存

        // 1.找到每个商品在哪个仓库都有库存
        List<OrderItemVo> locks = wareSkuLockVo.getLocks();
        List<SkuWareHasStock> skuWareHasStockList = locks.stream().map(item -> {
            SkuWareHasStock skuWareHasStock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            skuWareHasStock.setSkuId(skuId);
            skuWareHasStock.setNum(item.getCount());

            // 查询该skuId商品所在仓库
            List<Long> wareId = this.listWareIdHasSkuStock(skuId);
            skuWareHasStock.setWareId(wareId);
            return skuWareHasStock;
        }).collect(Collectors.toList());

        // 锁定库存
        for (SkuWareHasStock skuWareHasStock : skuWareHasStockList) {



            // 有一个仓库有库存就设置为true
            Boolean skuLock = false;
            Long skuId = skuWareHasStock.getSkuId();
            List<Long> wareIds = skuWareHasStock.getWareId();
            if (wareIds == null && wareIds.size() > 0) {
                // 直接抛出无库存异常
                throw new NoStockException(skuId);

            }
            Integer num = skuWareHasStock.getNum();
            for (Long wareId : wareIds) {
                // 成功就返回1, 失败返回 0
                Long count = this.lockSkuStock(skuId, wareId, num);
                if (count == 1) {
                    skuLock = true;
                    // 成功跳出当前循环
                    break;
                }
                // 当前仓库锁定失败 锁下一个


            }
            if (!skuLock) {
                // 如果该商品所有仓库都没有办法锁定库存
                throw new NoStockException(skuId);
            }

        }
        // 到这说明 无功

        return true;
    }

    @Override
    public List<Long> listWareIdHasSkuStock(Long skuId) {
        return this.baseMapper.listWareIdHasSkuStock(skuId);
    }

    @Override
    public Long lockSkuStock(Long skuId, Long wareId, Integer num) {
        return this.baseMapper.lockSkuStock(skuId, wareId, num);
    }

    @Data
    class SkuWareHasStock {
        /**
         * skuId
         */
        private Long skuId;

        /**
         * 商品数量(需要扣减的库存数量)
         */
        private Integer num;

        /**
         * sku商品所在仓库列表
         */
        private List<Long> wareId;
    }
}
