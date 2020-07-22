package com.ysy.tmall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * @anthor silenceYin
 * @date 2020/7/19 - 21:12
 */
@Data
public  class AttrVo {
    private Long attrId;

    private String attrName;

    private List<String> attrValue;
}
