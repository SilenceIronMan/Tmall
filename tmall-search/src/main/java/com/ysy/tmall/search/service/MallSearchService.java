package com.ysy.tmall.search.service;

import com.ysy.tmall.search.vo.SearchParam;
import com.ysy.tmall.search.vo.SearchResult;


/**
 * @anthor silenceYin
 * @date 2020/7/18 - 21:21
 */
public interface MallSearchService {
    /**
     *
     * @param searchParam 检索参数
     * @return 检索结果
     */
    SearchResult search(SearchParam searchParam);
}
