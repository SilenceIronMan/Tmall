package com.ysy.tmall.search.vo;

import com.ysy.tmall.common.to.producttocoupon.to.SkuEsModel;
import lombok.Data;

import java.util.List;

/**
 * @anthor silenceYin
 * @date 2020/7/19 - 1:09
 */
@Data
public class SearchResult {

    /**
     * 所有商品信息
     */
    private List<SkuEsModel> products;


    // 以下是分页信息
    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页码
     */
    private Integer totalPages;

    //导航页
    private List<Integer> pageNavs;

    //查询到的所有品牌信息
    private List<BrandVo> brands;


    //查询所涉及到的所有属性
    private List<AttrVo> attrs;



    //查询所涉及到的所有分类信息
    private List<CatalogVo> catalogs;



    //面包屑导航
    private List<NavVo> navs;

    @Data
    public static class NavVo{
        private String navName;
        private String navValue;
        private String link;
    }


}
