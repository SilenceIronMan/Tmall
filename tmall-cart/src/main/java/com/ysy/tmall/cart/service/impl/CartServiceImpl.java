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
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
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

        String redisCartItem = (String) cartOps.get(skuId.toString());

        if (StringUtils.isEmpty(redisCartItem)) {
            // 將商品添加到購物車
            CartItem cartItem = new CartItem();
            CompletableFuture<Void> skuInfoFuture = CompletableFuture.runAsync(() -> {
                // 先遠程獲取sku的信息
                R info = productFeignService.info(skuId);
                SkuInfoVo skuInfo = info.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });
                if (Objects.nonNull(skuInfo)) {
                    cartItem.setSkuId(skuId);
                    cartItem.setCount(num);
                    cartItem.setImage(skuInfo.getSkuDefaultImg());
                    cartItem.setPrice(skuInfo.getPrice());
                    cartItem.setTitle(skuInfo.getSkuTitle());
                }

            }, executor);

            CompletableFuture<Void> skuSaleAttrsFuture = CompletableFuture.runAsync(() -> {
                List<String> skuSaleAttrValues = productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttr(skuSaleAttrValues);
            }, executor);

            CompletableFuture.allOf(skuInfoFuture, skuSaleAttrsFuture).get();

            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));

            return cartItem;
        }  else {
            CartItem cartItem = JSON.parseObject(redisCartItem, CartItem.class);
            Integer count = cartItem.getCount();
            // 商品数量叠加
            cartItem.setCount(count + num);
            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));

            // 显示的应该是你 实际添加的商品数量 而不是redis 中应该存储的数量
            cartItem.setCount(num);
            return cartItem;
        }

    }

    @Override
    public CartItem getCartItem(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String cartItemRedis =(String) cartOps.get(skuId.toString());

        if (StringUtils.isNotEmpty(cartItemRedis)) {
            CartItem cartItem = JSON.parseObject(cartItemRedis, CartItem.class);
            // 显示的应该是实际添加的商品数量 而不是购物车总数
            cartItem.setCount(num);
            return cartItem;
        } else {
            // 概率极低吧 刚添加完 人就没了
            // 某人改了url 的skuId 得预防一下
            CartItem cartItem = new CartItem();
            CompletableFuture<Void> skuInfoFuture = CompletableFuture.runAsync(() -> {
                // 先遠程獲取sku的信息
                R info = productFeignService.info(skuId);
                SkuInfoVo skuInfo = info.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });
                if (Objects.nonNull(skuInfo)) {
                    cartItem.setCount(num);
                    cartItem.setImage(skuInfo.getSkuDefaultImg());
                    cartItem.setPrice(skuInfo.getPrice());
                    cartItem.setSkuId(skuId);
                    cartItem.setTitle(skuInfo.getSkuTitle());
                }
            }, executor);

            CompletableFuture<Void> skuSaleAttrsFuture = CompletableFuture.runAsync(() -> {
                List<String> skuSaleAttrValues = productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttr(skuSaleAttrValues);
            }, executor);

            CompletableFuture.allOf(skuInfoFuture, skuSaleAttrsFuture).get();

            return cartItem;

        }

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
