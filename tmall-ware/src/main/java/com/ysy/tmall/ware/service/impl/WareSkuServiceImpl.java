package com.ysy.tmall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ysy.tmall.common.utils.PageUtils;
import com.ysy.tmall.common.utils.Query;
import com.ysy.tmall.ware.dao.WareSkuDao;
import com.ysy.tmall.ware.entity.WareSkuEntity;
import com.ysy.tmall.ware.service.WareSkuService;
import com.ysy.tmall.common.to.producttocoupon.SkuHasStockVo;
import com.ysy.tmall.ware.vo.LockStockResult;
import com.ysy.tmall.ware.vo.WareSkuLockVo;
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
    public List<LockStockResult> orderLockStock(WareSkuLockVo wareSkuLockVo) {
        return null;
    }


}
