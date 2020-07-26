package com.ysy.tmall.cart.service;

import com.ysy.tmall.cart.vo.CartItem;

import java.util.concurrent.ExecutionException;

/**
 * 购物车
 * @anthor silenceYin
 * @date 2020/7/26 - 15:33
 */
public interface CartService {
    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    CartItem getCartItem(Long skuId, Integer num);
}
