package com.ysy.tmall.common.exception;

/**
 * 无库存异常
 * @anthor silenceYin
 * @date 2020/8/15 - 13:46
 */
public class NoStockException extends RuntimeException{
    /**
     * skuId 商品Id
     */
    private Long skuId;

    public NoStockException(Long skuId) {
        super("商品:" + skuId + ";没有足够的库存!");
        this.skuId = skuId;
    }

    public Long getSkuId() {
        return skuId;
    }

}
