package com.ysy.tmall.seckill.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @anthor silenceYin
 * @date 2020/9/6 - 23:03
 */
@Data
public class SeckillSessionsWithSkus {
    /**
     * id
     */
    private Long id;
    /**
     * 场次名称
     */
    private String name;
    /**
     * 每日开始时间
     */
    private Date startTime;
    /**
     * 每日结束时间
     */
    private Date endTime;
    /**
     * 启用状态
     */
    private Integer status;
    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 秒杀sku商品
     */
    private List<SeckillSkuVo> relationSkus;

}
