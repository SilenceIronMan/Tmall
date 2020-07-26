package com.ysy.tmall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.ysy.tmall.cart.feign.ProductFeignService;
import com.ysy.tmall.cart.interceptor.CartInterceptor;
import com.ysy.tmall.cart.service.CartService;
import com.ysy.tmall.cart.to.UserInfoTo;
import com.ysy.tmall.cart.vo.CartItem;
import com.ysy.tmall.cart.vo.SkuInfoVo;
import com.ysy.tmall.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @anthor silenceYin
 * @date 2020/7/26 - 15:34
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Resource
    private ProductFeignService productFeignService;

    @Resource
    private ThreadPoolExecutor executor;

    private final String CART_PREFIX = "ysymall:cart:";

    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        // 將商品添加到購物車
        CartItem cartItem = new CartItem();
        CompletableFuture<Void> skuInfoFuture = CompletableFuture.runAsync(() -> {
            // 先遠程獲取sku的信息
            R info = productFeignService.info(skuId);
            SkuInfoVo skuInfo = info.getData("skuInfo", new TypeReference<SkuInfoVo>() {
            });
            cartItem.setCount(num);
            cartItem.setImage(skuInfo.getSkuDefaultImg());
            cartItem.setPrice(skuInfo.getPrice());
            cartItem.setSkuId(skuId);
            cartItem.setTitle(skuInfo.getSkuTitle());

        }, executor);

        CompletableFuture<Void> skuSaleAttrsFuture = CompletableFuture.runAsync(() -> {
            List<String> skuSaleAttrValues = productFeignService.getSkuSaleAttrValues(skuId);
            cartItem.setSkuAttr(skuSaleAttrValues);
        }, executor);

        CompletableFuture.allOf(skuInfoFuture, skuSaleAttrsFuture).get();

        cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));

        return cartItem;
    }

    /**
     * 獲取對redis中購物車的操作
     * @return
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.userInfoToThreadLocal.get();
        String cartKey = "";
        // 1 登錄用戶
        if (userInfoTo.getUserId() != null) {
            cartKey = CART_PREFIX + userInfoTo.getUserId();

        } else {
            // 臨時用戶
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }

        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);
        return operations;
    }
}
