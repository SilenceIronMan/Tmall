package com.ysy.tmall.product.service.impl;

import com.ysy.tmall.product.vo.web.SkuItemSaleAttrVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ysy.tmall.common.utils.PageUtils;
import com.ysy.tmall.common.utils.Query;

import com.ysy.tmall.product.dao.SkuSaleAttrValueDao;
import com.ysy.tmall.product.entity.SkuSaleAttrValueEntity;
import com.ysy.tmall.product.service.SkuSaleAttrValueService;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuItemSaleAttrVo> getSaleAttrsBySpuId(Long spuId) {

        return this.baseMapper.getSaleAttrsBySpuId(spuId);

    }

    @Override
    public List<String> getSkuSaleAttrValues(Long skuId) {
        List<SkuSaleAttrValueEntity> skuIdEntities = this.baseMapper.selectList(new QueryWrapper<SkuSaleAttrValueEntity>().eq("sku_id", skuId).select("attr_value"));
        List<String> list = skuIdEntities.stream().map(s -> s.getAttrValue()).collect(Collectors.toList());
        return list;
    }

}
