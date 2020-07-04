package com.ysy.tmall.product.vo;

import lombok.Data;

/**
 * 属性 response视图对象
 * @anthor silenceYin
 * @date 2020/7/4 - 15:42
 */
@Data
public class AttrRespVo extends AttrVo{
    /**
     * 所属分类名字
     *
     */
    private String catelogName;

    /**
     * 所属分组名字
     */
    private String groupName;
}
