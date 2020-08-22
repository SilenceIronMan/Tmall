package com.ysy.tmall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.ysy.tmall.common.to.to.SkuEsModel;
import com.ysy.tmall.search.config.ElasticSearchConfig;
import com.ysy.tmall.search.constant.EsConstant;
import com.ysy.tmall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @anthor silenceYin
 * @date 2020/7/13 - 1:32
 */
@Service
@Slf4j
public class ProductSaveServiceImpl implements ProductSaveService {

    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Override
    public boolean prductStatusUp(List<SkuEsModel> skuEsModelList) throws IOException {
        // 1.建立index(索引)
        // 2.存doc(数据)
        //BulkRequest bulkRequest, RequestOptions options
        BulkRequest bulkRequest = new BulkRequest();
        skuEsModelList.stream().forEach(skuEsModel -> {
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            indexRequest.id(skuEsModel.getSkuId().toString());
            String skuStr = JSON.toJSONString(skuEsModel);
            indexRequest.source(skuStr, XContentType.JSON);
            bulkRequest.add(indexRequest);
        });
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, ElasticSearchConfig.COMMON_OPTIONS);

        // TODO 1.如果批量错误
        boolean b = bulk.hasFailures();
        List<String> collect = Arrays.stream(bulk.getItems())
                .map(item -> item.getId())
                .collect(Collectors.toList());

        if (b) {
            log.error("商品上架失败 {}", collect);
        } else {
            log.error("商品上架成功 {}", collect);
        }


        return b;
    }
}
