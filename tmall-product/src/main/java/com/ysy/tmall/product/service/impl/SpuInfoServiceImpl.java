package com.ysy.tmall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ysy.tmall.common.to.producttocoupon.SkuReductionTO;
import com.ysy.tmall.common.to.producttocoupon.SpuBoundsTO;
import com.ysy.tmall.common.utils.PageUtils;
import com.ysy.tmall.common.utils.Query;
import com.ysy.tmall.common.utils.R;
import com.ysy.tmall.product.dao.SpuInfoDao;
import com.ysy.tmall.product.entity.*;
import com.ysy.tmall.product.feign.CouponFeignService;
import com.ysy.tmall.product.service.*;
import com.ysy.tmall.product.vo.spu.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Service("spuInfoService")
@Slf4j
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Resource
    private SpuInfoDescService spuInfoDescService;

    @Resource
    private SpuImagesService spuImagesService;

    @Resource
    private ProductAttrValueService productAttrValueService;

    @Resource
    private AttrService attrService;

    @Resource
    private CouponFeignService couponFeignService;

    @Resource
    private SkuInfoService skuInfoService;

    @Resource
    private SkuImagesService skuImagesService;

    @Resource
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void saveSpuInfo(SpuSaveVo vo) {

        // 1. 保存spu基本信息 pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.save(spuInfoEntity);
        Long spuId = spuInfoEntity.getId();

        // 2.保存spu的描述图片信息 pms_spu_info_desc
        List<String> decriptList = vo.getDecript();
        String decript = String.join(",", decriptList);
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuId);
        spuInfoDescEntity.setDecript(decript);
        spuInfoDescService.save(spuInfoDescEntity);

        // 3.保存spu的图片集 pms_spu_images
        List<String> images = vo.getImages();
        if (Objects.nonNull(images) && images.size() > 0) {
            List<SpuImagesEntity> SpuImages = images.stream().filter(spuImage -> StringUtils.isNotEmpty(spuImage)).map(image -> {
                SpuImagesEntity spuImagesEntity = new SpuImagesEntity();
                spuImagesEntity.setImgUrl(image);
                return spuImagesEntity;
            }).collect(Collectors.toList());
            spuImagesService.saveBatch(SpuImages);
        }


        // 4.保存spu的规格参数 pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> productAttrValueEntities = baseAttrs.stream().map(baseAttr -> {
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
            Long attrId = baseAttr.getAttrId();
            productAttrValueEntity.setAttrId(attrId);
            AttrEntity attrEntity = attrService.getById(attrId);
            productAttrValueEntity.setAttrName(attrEntity.getAttrName());
            productAttrValueEntity.setAttrName("");
            productAttrValueEntity.setAttrValue(baseAttr.getAttrValues());
            return productAttrValueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveBatch(productAttrValueEntities);


        // 5.保存spu的积分信息  tmall_sms(sms_spu_bounds) feign远程服务调用
        Bounds bounds = vo.getBounds();
        SpuBoundsTO spuBoundsTO = new SpuBoundsTO();
        BeanUtils.copyProperties(bounds, spuBoundsTO);
        spuBoundsTO.setSpuId(spuId);
        // 客户端 后期可以做服务降级 处理 feign 的fallback
        R boundSave = couponFeignService.saveSpuBounds(spuBoundsTO);
        if (boundSave.getCode() == 0) {
            log.info("保存优惠信息成功--------");
        } else {
            log.error("保存优惠信息失败--------");
        }

        // 6.保存当前spu对应的所有sku信息
        // 6.1 sku的基本信息 pms_sku_info
        List<Skus> skus = vo.getSkus();
        if (Objects.nonNull(skus) && skus.size() > 0) {
            skus.stream().forEach(sku -> {

                String defaultImg = "";
                List<Images> skuImage = sku.getImages();
                for (Images img : skuImage) {
                    if (img.getDefaultImg() == 1) {
                        defaultImg = img.getImgUrl();
                        break;
                    }
                }
                /**
                 *  "skuName":"华为 HUAWEI Mate 30  超亮绿 8G + 256",
                 *   "price":"4599",
                 *    "skuTitle":"华为 HUAWEI Mate 30  超亮绿 8G + 256  麒麟990旗舰芯片4000万超感光徕卡影像屏内指纹",
                 *    "skuSubtitle":"【直降400，到手价3599】
                 */
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(sku, skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuId);
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                skuInfoService.save(skuInfoEntity);
                // 数据库自增的skuId 以后可以用雪花算法
                Long skuId = skuInfoEntity.getSkuId();

                // 6.2 sku的图片信息 pms_sku_images
                List<SkuImagesEntity> skuImagesEntities = skuImage
                        .stream().filter(image -> StringUtils.isNotEmpty(image.getImgUrl()))
                        .map(image -> {
                            SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                            BeanUtils.copyProperties(image, skuImagesEntity);
                            skuImagesEntity.setSkuId(skuId);
                            return skuImagesEntity;
                        }).collect(Collectors.toList());
                skuImagesService.saveBatch(skuImagesEntities);

                // 6.3 sku的销售属性信息 pms_sku_sale_attr_value
                List<Attr> saleAttrs = sku.getAttr();
                List<SkuSaleAttrValueEntity> saleAttrValueEntities = saleAttrs.stream().map(attr -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();

                    BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);

                    skuSaleAttrValueEntity.setSkuId(skuId);
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(saleAttrValueEntities);

                // 6.4 sku的优惠.满减等信息 tmall_sms
                //  sms_sku_full_reduction 满减 / sms_sku_ladder 折扣/sms_member_price 会员价格
                // 6.4.1 sms_sku_full_reduction 满减
                // 6.4.2 sms_sku_ladder 折扣
                //couponFeignService.saveSkuLadder(null);
                // 6.4.3 sms_member_price 满减
                SkuReductionTO skuReductionTO = new SkuReductionTO();
                BeanUtils.copyProperties(sku, skuReductionTO);
                skuReductionTO.setSkuId(skuId);
                R skuReduction = couponFeignService.saveSkuReduction(skuReductionTO);
                if (skuReduction.getCode() == 0) {
                    log.info("保存满减 打折 会员优惠信息成功--------");
                } else {
                    log.error("保存满减 打折 会员优惠信息失败--------");
                }
            });
        }


    }

    @Override
    public PageUtils listSpuInfo(Map<String, Object> params) {
        /**
         * {
         *    page: 1,//当前页码
         *    limit: 10,//每页记录数
         *    sidx: 'id',//排序字段
         *    order: 'asc/desc',//排序方式
         *    key: '华为',//检索关键字
         *    catelogId: 6,//三级分类id
         *    brandId: 1,//品牌id
         *    status: 0,//商品状态
         * }
         */
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        //三级分类id
        String catelogId = (String) params.get("catelogId");
        if (StringUtils.isNotEmpty(catelogId) && !"0".equals(catelogId)) {
            wrapper.eq("catalog_id", catelogId);
        }
        //品牌id
        String brandId = (String) params.get("brandId");
        if (StringUtils.isNotEmpty(brandId) && !"0".equals(brandId  )) {
            wrapper.eq("brand_Id", brandId);
        }
        //商品状态
        String status = (String) params.get("status");
        if (StringUtils.isNotEmpty(status)) {
            wrapper.eq("publish_status", status);
        }
        // 检索关键字

        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            wrapper.and(w -> w.eq("id", key).or().like("spu_name", key));
        }

        IPage<SpuInfoEntity> page = this.page(new Query<SpuInfoEntity>().getPage(params),
                wrapper);

        return new PageUtils(page);
    }

}
