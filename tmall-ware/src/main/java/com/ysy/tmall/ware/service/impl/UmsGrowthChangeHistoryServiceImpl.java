package com.ysy.tmall.ware.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ysy.tmall.common.utils.PageUtils;
import com.ysy.tmall.common.utils.Query;

import com.ysy.tmall.ware.dao.UmsGrowthChangeHistoryDao;
import com.ysy.tmall.ware.entity.UmsGrowthChangeHistoryEntity;
import com.ysy.tmall.ware.service.UmsGrowthChangeHistoryService;


@Service("umsGrowthChangeHistoryService")
public class UmsGrowthChangeHistoryServiceImpl extends ServiceImpl<UmsGrowthChangeHistoryDao, UmsGrowthChangeHistoryEntity> implements UmsGrowthChangeHistoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<UmsGrowthChangeHistoryEntity> page = this.page(
                new Query<UmsGrowthChangeHistoryEntity>().getPage(params),
                new QueryWrapper<UmsGrowthChangeHistoryEntity>()
        );

        return new PageUtils(page);
    }

}