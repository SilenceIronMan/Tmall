package com.ysy.tmall.product.vo;

import com.ysy.tmall.product.entity.AttrEntity;
import lombok.Data;

import java.util.List;

/**
 * @anthor silenceYin
 * @date 2020/7/5 - 17:03
 */
@Data
public class AttrGroupWithAttrRespVo {
    /**
     * 分组id
     */
    private Long attrGroupId;

    /**
     * 组名
     */
    private String attrGroupName;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 描述
     */
    private String descript;
    /**
     * 组图标
     */
    private String icon;
    /**
     * 所属分类id
     */
    private Long catelogId;

    /**
     * 屬性列表
     */
    private List<AttrEntity> attrs;
}
