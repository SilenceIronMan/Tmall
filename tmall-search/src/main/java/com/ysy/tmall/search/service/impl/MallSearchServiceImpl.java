package com.ysy.tmall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.ysy.tmall.common.to.producttocoupon.to.SkuEsModel;
import com.ysy.tmall.search.config.ElasticSearchConfig;
import com.ysy.tmall.search.constant.EsConstant;
import com.ysy.tmall.search.service.MallSearchService;
import com.ysy.tmall.search.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @anthor silenceYin
 * @date 2020/7/18 - 21:21
 */
@Service
@Slf4j
public class MallSearchServiceImpl implements MallSearchService {

    @Resource
    RestHighLevelClient esRestClient;

    @Override
    public SearchResult search(SearchParam searchParam){
        SearchResult searchResult = null;
        SearchRequest searchRequest = buildSearchRequest(searchParam);
        try {
            // 执行检索
            SearchResponse response = esRestClient.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);

            searchResult = buildSearchResult(searchParam, response);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return searchResult;
    }


    /**
     * 检索结果参数处理
     * @param response
     * @return
     */
    private SearchResult buildSearchResult(SearchParam searchParam, SearchResponse response) {
        SearchResult result = new SearchResult();
        SearchHits hits = response.getHits();

        SearchHit[] subHits = hits.getHits();
        List<SkuEsModel> skuEsModels=null;
        if(Objects.nonNull(subHits) && subHits.length > 0){

            skuEsModels = Arrays.asList(subHits).stream().map(subHit -> {
                String sourceAsString = subHit.getSourceAsString();
                SkuEsModel skuEsModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                if (StringUtils.isNotEmpty(searchParam.getKeyword())) {
                    HighlightField skuTitle = subHit.getHighlightFields().get("skuTitle");
                    String skuTitleHighLight = skuTitle.getFragments()[0].string();
                    skuEsModel.setSkuTitle(skuTitleHighLight);
                }
                return skuEsModel;
            }).collect(Collectors.toList());

        }

        //1.返回所查询到的所有商品
        result.setProducts(skuEsModels);


        //2.当前所有商品所涉及到的所有属性信息
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        List<AttrVo> attrVos = attr_id_agg.getBuckets().stream().map(item -> {
            AttrVo attrVo = new AttrVo();
            //1.获取属性的id
            long attrId = item.getKeyAsNumber().longValue();

            //2.获取属性名
            String attrName = ((ParsedStringTerms) item.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();
            //3.获取属性的所有值
            List<String> attrValues = ((ParsedStringTerms) item.getAggregations().get("attr_value_agg")).getBuckets().stream().map(bucket -> {
                return bucket.getKeyAsString();
            }).collect(Collectors.toList());

            attrVo.setAttrId(attrId);
            attrVo.setAttrName(attrName);
            attrVo.setAttrValue(attrValues);


            return attrVo;
        }).collect(Collectors.toList());

        result.setAttrs(attrVos);


        //3.当前所有商品所涉及到的所有品牌信息
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        List<BrandVo> brandVos = brand_agg.getBuckets().stream().map(item -> {
            BrandVo brandVo = new BrandVo();
            //1.获取id
            long brandId = item.getKeyAsNumber().longValue();
            //2.获取品牌名
            String brandName = ((ParsedStringTerms) item.getAggregations().get("brand_name_agg")).getBuckets().get(0).getKeyAsString();

            //3.获取品牌图片
            String brandImag = ((ParsedStringTerms) item.getAggregations().get("brand_img_agg")).getBuckets().get(0).getKeyAsString();

            brandVo.setBrandId(brandId);
            brandVo.setBrandName(brandName);
            brandVo.setBrandImg(brandImag);
            return brandVo;
        }).collect(Collectors.toList());

        result.setBrands(brandVos);

        //4.当前所有商品所涉及到的所有分类信息
        ParsedLongTerms catelog_agg = response.getAggregations().get("catalog_agg");
        List<CatalogVo> catalogVos = catelog_agg.getBuckets().stream().map(item -> {
            CatalogVo catalogVo = new CatalogVo();
            //获取分类ID
            String catelogId = item.getKeyAsString();
            catalogVo.setCatalogId(Long.parseLong(catelogId));

            //获取分类名
            ParsedStringTerms catelog_name_agg = item.getAggregations().get("catalog_name_agg");
            String catelogName = catelog_name_agg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catelogName);
            return catalogVo;
        }).collect(Collectors.toList());

        result.setCatalogs(catalogVos);

        //=========以上从聚合信息中获取===========

        //5.分页信息-总记录数
        long total = hits.getTotalHits().value;
        result.setTotal(total);
        //5.分页信息-总页码
        boolean flag=total % EsConstant.PRODUCT_PAGESIZE == 0;
        int totalPage=flag ? (int)total / EsConstant.PRODUCT_PAGESIZE
                : ((int)total / EsConstant.PRODUCT_PAGESIZE) + 1;
        result.setTotalPages(totalPage);
        //5.分页信息-页码
        Integer pageNum = searchParam.getPageNum();
        // 如果超过了总页数 取最大页数
        if (pageNum > totalPage) {
            pageNum  = totalPage;
        }
        result.setPageNum(pageNum);



        return result;
    }


    /**
     * 检索查询条件构造
     * @param searchParam
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam searchParam) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); //构建DSL
        // 模糊匹配 过滤(按照属性 分类 品牌 价格区间 库存等)
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        // 1 must
        // 关键字 模糊匹配
        String keyword = searchParam.getKeyword();
        if (StringUtils.isNotEmpty(keyword)) {

            MatchQueryBuilder skuTitleMatch = QueryBuilders.matchQuery("skuTitle", keyword);
            boolQueryBuilder.must(skuTitleMatch);
        }
        // 过滤
        // 属性
        List<String> attrs = searchParam.getAttrs();
        if (Objects.nonNull(attrs) && attrs.size() > 0) {


            for (String attr : attrs) {
                BoolQueryBuilder filterBoolQueryBuilder = QueryBuilders.boolQuery();
                // 1_3G:4G:5G
                String[] s = attr.split("_");
                String attrId = s[0];
                String[] attrValues = s[1].split(":");
                // 属性id
                filterBoolQueryBuilder.must(QueryBuilders.termQuery("attrs.attrId",attrId));
                // 属性名
                filterBoolQueryBuilder.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));

                // 多个nested查询拼接
                NestedQueryBuilder attrsFilter = QueryBuilders.nestedQuery("attrs", filterBoolQueryBuilder, ScoreMode.None);

                boolQueryBuilder.filter(attrsFilter);
            }






        }

        // 分类
        Long category3Id = searchParam.getCategory3Id();
        if (Objects.nonNull(category3Id)) {
            TermQueryBuilder catalogIdFilter = QueryBuilders.termQuery("catalogId", category3Id);
            boolQueryBuilder.filter(catalogIdFilter);
        }

        // 品牌
        List<Long> brandId = searchParam.getBrandId();
        if (Objects.nonNull(brandId) && brandId.size() > 0) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", brandId));
        }



        // 价格区间
        String skuPrice = searchParam.getSkuPrice();
        if (StringUtils.isNotEmpty(skuPrice)) {
            RangeQueryBuilder skuPriceRange = QueryBuilders.rangeQuery("skuPrice");
            // 1_550 _550 1_
            String[] s = skuPrice.split("_");
            if (s.length == 2) {
                skuPriceRange.gte(s[0]).lte(s[1]);
            } else {

                if (!"_".equals(skuPrice)) {
                    boolean startsWith = skuPrice.startsWith("_");
                    if (startsWith) {
                        // 一般情况价格都是大于等于0的
                        skuPriceRange.gte(0).lte(s[0]);
                    }

                    boolean endsWith = skuPrice.endsWith("_");
                    if (endsWith) {
                        skuPriceRange.gte(s[0]);
                    }
                }

            }


            boolQueryBuilder.filter(skuPriceRange);
        }



        // 库存
        boolQueryBuilder.filter(QueryBuilders.termsQuery("hasStock", searchParam.getHasStock() == 1));


        searchSourceBuilder.query(boolQueryBuilder);


        // 排序 分页 高亮
        //排序
        //形式为sort=hotScore_asc/desc
        if(!StringUtils.isEmpty(searchParam.getSort())){
            String sort = searchParam.getSort();
            String[] sortFileds = sort.split("_");

            SortOrder sortOrder="asc".equalsIgnoreCase(sortFileds[1]) ? SortOrder.ASC:SortOrder.DESC;

            searchSourceBuilder.sort(sortFileds[0], sortOrder);
        }

        searchSourceBuilder.from((searchParam.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        searchSourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);


        if (StringUtils.isNotEmpty(keyword)) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            searchSourceBuilder.highlighter(highlightBuilder);
        }

        // 聚合分析
        // 1 品牌聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId").size(50);

        // 品牌聚合 子聚合
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));

        searchSourceBuilder.aggregation(brand_agg);

        // 2 分類聚合
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(10);
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        searchSourceBuilder.aggregation(catalog_agg);


        // 3 屬性聚合
        //2. 按照属性信息进行聚合
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        //2.1 按照属性ID进行聚合
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        attr_agg.subAggregation(attr_id_agg);
        //2.1.1 在每个属性ID下，按照属性名进行聚合
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        //2.1.1 在每个属性ID下，按照属性值进行聚合
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        searchSourceBuilder.aggregation(attr_agg);

        log.debug("构建的DSL语句 {}",searchSourceBuilder.toString());

        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, searchSourceBuilder);

        return searchRequest;
    }
}
