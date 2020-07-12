package com.ysy.tmall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ysy.tmall.common.utils.PageUtils;
import com.ysy.tmall.product.entity.SpuInfoEntity;
import com.ysy.tmall.product.vo.spu.SpuSaveVo;

import java.util.Map;

/**
 * spu信息
 *
 * @author SilenceIronMan
 * @email yinshiyu_2008@126.com
 * @date 2020-06-27 21:47:45
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfo(SpuSaveVo spuSaveVo);

    PageUtils listSpuInfo(Map<String, Object> params);

    /**
     * 商品上架
     * @param spuId spuid.
     */
    void upSpu(Long spuId);
}

