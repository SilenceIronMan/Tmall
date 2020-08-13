package com.ysy.tmall.cart.service;

import com.ysy.tmall.cart.vo.Cart;
import com.ysy.tmall.cart.vo.CartItem;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 购物车
 * @anthor silenceYin
 * @date 2020/7/26 - 15:33
 */
public interface CartService {
    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    CartItem getCartItem(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    Cart getCart() throws ExecutionException, InterruptedException;

    /**
     * 清空redis购物车
     * @param cartKey 购物车key
     */
    void clearCart(String cartKey);

    void checkItem(Long skuId, Integer check);

    void changeItemCount(Long skuId, Integer num);

    void deleteItem(Long skuId);

    List<CartItem> getUserCartItems();
}
