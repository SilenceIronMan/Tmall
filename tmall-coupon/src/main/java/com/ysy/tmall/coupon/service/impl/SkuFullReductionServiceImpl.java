package com.ysy.tmall.coupon.service.impl;

import com.ysy.tmall.common.to.MemberPrice;
import com.ysy.tmall.common.to.SkuReductionTO;
import com.ysy.tmall.coupon.entity.MemberPriceEntity;
import com.ysy.tmall.coupon.entity.SkuLadderEntity;
import com.ysy.tmall.coupon.service.MemberPriceService;
import com.ysy.tmall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ysy.tmall.common.utils.PageUtils;
import com.ysy.tmall.common.utils.Query;

import com.ysy.tmall.coupon.dao.SkuFullReductionDao;
import com.ysy.tmall.coupon.entity.SkuFullReductionEntity;
import com.ysy.tmall.coupon.service.SkuFullReductionService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Resource
    private SkuLadderService skuLadderService;

    @Resource
    private MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * sku 满减, 打折, 以及会员价格
     *
     * @param skuReductionTO
     */
    @Override
    @Transactional
    public void saveSkuReduction(SkuReductionTO skuReductionTO) {
        // sku 打折

        if (skuReductionTO.getFullCount() > 0
                && skuReductionTO.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
            SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
            BeanUtils.copyProperties(skuReductionTO, skuLadderEntity);
            skuLadderEntity.setAddOther(skuReductionTO.getCountStatus());
                   /*
         可以现在就计算价格,也可以下单再计算价格 因为sku价格会变 所以折后价也会变
         */
            // skuLadderEntity.setPrice(null);
            skuLadderService.save(skuLadderEntity);
        }


        // sku 满减
        if (skuReductionTO.getFullPrice().compareTo(BigDecimal.ZERO) > 0
                && skuReductionTO.getReducePrice().compareTo(BigDecimal.ZERO) > 0) {
            SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
            BeanUtils.copyProperties(skuReductionTO, skuFullReductionEntity);
            skuFullReductionEntity.setAddOther(skuReductionTO.getPriceStatus());
            this.save(skuFullReductionEntity);
        }


        // sku会员价格
        Long skuId = skuReductionTO.getSkuId();
        List<MemberPrice> memberPrice = skuReductionTO.getMemberPrice();
        List<MemberPriceEntity> memberPriceEntities = memberPrice.stream()
                .filter(price -> price.getPrice().compareTo(BigDecimal.ZERO) > 0)
                .map(price -> {
                    MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
                    memberPriceEntity.setMemberLevelId(price.getId()); //会员Id
                    memberPriceEntity.setMemberLevelName(price.getName()); //会员等級
                    memberPriceEntity.setMemberPrice(price.getPrice());//會員價格
                    memberPriceEntity.setAddOther(1);
                    memberPriceEntity.setSkuId(skuId);
                    return memberPriceEntity;
                }).collect(Collectors.toList());
        memberPriceService.saveBatch(memberPriceEntities);
    }

}
