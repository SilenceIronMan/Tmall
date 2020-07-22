package com.ysy.tmall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ysy.tmall.common.utils.PageUtils;
import com.ysy.tmall.common.utils.Query;
import com.ysy.tmall.product.dao.SkuInfoDao;
import com.ysy.tmall.product.entity.SkuImagesEntity;
import com.ysy.tmall.product.entity.SkuInfoEntity;
import com.ysy.tmall.product.entity.SpuInfoDescEntity;
import com.ysy.tmall.product.entity.SpuInfoEntity;
import com.ysy.tmall.product.service.*;
import com.ysy.tmall.product.vo.web.SkuItemSaleAttrVo;
import com.ysy.tmall.product.vo.web.SkuItemVo;
import com.ysy.tmall.product.vo.web.SpuItemAttrGroupVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 商品sku详细信息
 */
@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Resource
    private SkuImagesService skuImagesService;

    @Resource
    private SpuInfoDescService spuInfoDescService;

    @Resource
    private AttrGroupService attrGroupService;

    @Resource
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Resource
    private ThreadPoolExecutor executor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils listSkuInfo(Map<String, Object> params) {

        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();
        //品牌id
        String brandId = (String) params.get("brandId");
        if (StringUtils.isNotEmpty(brandId) && !"0".equals(brandId)) {
            wrapper.eq("brand_Id", brandId);
        }

        //三级分类id
        String catelogId = (String) params.get("catelogId");
        if (StringUtils.isNotEmpty(catelogId) && !"0".equals(catelogId)) {
            wrapper.eq("catalog_id", catelogId);
        }

        //min max
        String min = (String) params.get("min");
        if (StringUtils.isNotEmpty(min) && BigDecimal.ZERO.compareTo(new BigDecimal(min)) < 0) {
            wrapper.ge("price", min);
        }
        String max = (String) params.get("max");
        if (StringUtils.isNotEmpty(max) && BigDecimal.ZERO.compareTo(new BigDecimal(max)) < 0) {
            wrapper.le("price", max);
        }
        // 检索关键字

        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            wrapper.and(w -> w.eq("id", key).or().like("spu_name", key));
        }

        IPage<SkuInfoEntity> page = this.page(new Query<SkuInfoEntity>().getPage(params),
                wrapper);

        return new PageUtils(page);

    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        List<SkuInfoEntity> skus = list(new QueryWrapper<SkuInfoEntity>()
                .eq("spu_id", spuId));

        return skus;
    }

    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = new SkuItemVo();

        /**
         * 异步编排
         */
        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            // 1. sku基本信息获取 pms_sku_info
            SkuInfoEntity skuInfo = getById(skuId);
            skuItemVo.setInfo(skuInfo);
            return skuInfo;
        }, executor);

        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            //3. 获取SPU销售属性组合 pms_product_attr_value
            List<SkuItemSaleAttrVo> skuItemSaleAttrVos = skuSaleAttrValueService.getSaleAttrsBySpuId(res.getSpuId());
            skuItemVo.setSaleAttr(skuItemSaleAttrVos);
        }, executor);

        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync((res) -> {
            // 4. 获取spu的介绍
            SpuInfoDescEntity desp = spuInfoDescService.getById(res.getSpuId());
            skuItemVo.setDesp(desp);
        }, executor);

        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            // 2. sku的图片信息 pms_sku_images
            List<SkuImagesEntity> images = skuImagesService.getImagesBySkuId(skuId);

            skuItemVo.setImages(images);
        }, executor);


        CompletableFuture<Void> baseAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            // 5. 获取spu的规格参数
            List<SpuItemAttrGroupVo> groupAttrs = attrGroupService.getAttrGroupWithAttrsBySpuId(res.getSpuId(), res.getCatalogId());
            skuItemVo.setGroupAttrs(groupAttrs);
        }, executor);


        //等待所有任务都完成
        CompletableFuture.allOf(saleAttrFuture, descFuture, baseAttrFuture, imageFuture).get();


        return skuItemVo;
    }

}
