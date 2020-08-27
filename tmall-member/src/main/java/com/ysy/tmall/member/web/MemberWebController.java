package com.ysy.tmall.member.web;

import com.ysy.tmall.common.vo.MemberResponseVO;
import com.ysy.tmall.member.interceptor.LoginUserInterceptor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @anthor silenceYin
 * @date 2020/8/28 - 2:16
 */
@Controller
public class MemberWebController {


    /**
     * 当前登录用户订单列表
     * @return
     */
    @GetMapping("/memberOrder.html")
    public String memberOrder() {
        ThreadLocal<MemberResponseVO> loginUser = LoginUserInterceptor.loginUser;

        return "orderList";
    }
}
