package com.ysy.tmall.product.vo.web;

import com.ysy.tmall.product.vo.spu.Attr;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @anthor silenceYin
 * @date 2020/7/20 - 6:41
 */
@ToString
@Data
public class SpuItemAttrGroupVo {
    private String groupName;
    private List<Attr> attrs;
}
