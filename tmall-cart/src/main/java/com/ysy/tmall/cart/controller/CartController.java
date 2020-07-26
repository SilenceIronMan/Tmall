package com.ysy.tmall.cart.controller;

import com.ysy.tmall.cart.interceptor.CartInterceptor;
import com.ysy.tmall.cart.service.CartService;
import com.ysy.tmall.cart.to.UserInfoTo;
import com.ysy.tmall.cart.vo.CartItem;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;

/**
 * @anthor silenceYin
 * @date 2020/7/26 - 15:40
 */
@Controller
@Slf4j
public class CartController {

    @Resource
    private CartService cartService;


    /**
     * 浏览器有一个cookie; user-key: 标识用户身份,一个月后过期;
     * 如果第一次使用jd的购物车功能,都会给一个临时的用户身份;
     * 浏览器以后保存,每次访问都会带上这个cookie;
     *
     * 登陆: session有
     * 没登录: 按照cookie里面带来user-key来说.
     * 第一次: 如果没有临时用户,帮忙创建一个临时用户
     * @return
     */
    @GetMapping("/cart.html")
    public String cartListPage() {

        // 1 得到用户信息 用户id + 临时用户 id
        UserInfoTo userInfoTo = CartInterceptor.userInfoToThreadLocal.get();
        log.info(userInfoTo.toString());

        return "cartList";
    }

    /**
     * 添加购物车执行过程,重定向到成功页面
     * @return
     */
    @GetMapping("/addToCart")
    public String addToCart(@Param("skuId") Long skuId,
                            @Param("num") Integer num,
                            Model model) throws ExecutionException, InterruptedException {

        //CartItem cartItem = cartService.addToCart(skuId, num);
        //model.addAttribute("item", cartItem);
        // 采用重定向 防止接口重刷 多次添加购物车
        cartService.addToCart(skuId, num);
        return "redirect:http://cart.ysymall.com/addToCart.html?skuId="+skuId +"&num=" + num;
    }


    /**
     * 添加购物车成功页面
     * @return
     */
    @GetMapping("/addToCart.html")
    public String addToCartSuccessPage(@Param("skuId") Long skuId,
                            @Param("num") Integer num,
                            Model model) {

        //CartItem cartItem = cartService.addToCart(skuId, num);
        //model.addAttribute("item", cartItem);
        // 采用重定向 防止接口重刷 多次添加购物车
        CartItem cartItem = cartService.getCartItem(skuId, num);
        model.addAttribute("item", cartItem);
        return "success";
    }



}
