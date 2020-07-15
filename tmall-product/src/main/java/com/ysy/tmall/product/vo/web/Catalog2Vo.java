package com.ysy.tmall.product.vo.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 2 級分類vo
 * @anthor silenceYin
 * @date 2020/7/14 - 1:49
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Catalog2Vo {
    /**
     * 1級父分類Id
     */
    private String catalog1Id;

    /**
     * 三級子分類
     */
    private List<Catalog3Vo> catalog3List;

    /**
     * 當前分類id
     */
    private String id;

    /**
     * 當前分類名
     */
    private String name;


    /**
     * 三級分類vo
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Catalog3Vo {
        /**
         * 2級父分類Id
         */
        private String catalog2Id;
        /**
         * 當前分類id
         */
        private String id;

        /**
         * 當前分類名
         */
        private String name;
    }
}
