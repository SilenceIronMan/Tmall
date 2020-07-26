package com.ysy.tmall.cart.controller;

import com.ysy.tmall.cart.interceptor.CartInterceptor;
import com.ysy.tmall.cart.to.UserInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @anthor silenceYin
 * @date 2020/7/26 - 15:40
 */
@Controller
@Slf4j
public class CartController {


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
}
