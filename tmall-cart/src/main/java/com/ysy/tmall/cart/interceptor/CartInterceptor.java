package com.ysy.tmall.cart.interceptor;

import com.ysy.tmall.cart.to.UserInfoTo;
import com.ysy.tmall.common.constant.AuthServerConstant;
import com.ysy.tmall.common.constant.CartConstant;
import com.ysy.tmall.common.vo.MemberResponseVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Objects;
import java.util.UUID;

/**
 * 购物车拦截器
 * @anthor silenceYin
 * @date 2020/7/26 - 16:07
 */
public class CartInterceptor implements HandlerInterceptor {

    public static ThreadLocal <UserInfoTo> userInfoToThreadLocal = new ThreadLocal<>();


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfoTo userInfoTo = new UserInfoTo();
        HttpSession session = request.getSession();
        MemberResponseVO member = (MemberResponseVO)session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (Objects.nonNull(member)) {
            // 用户已登录
            userInfoTo.setUserId(member.getId());

        }

        // 判断cookie中是否有临时用户信息
        Cookie[] cookies = request.getCookies();
        if (Objects.nonNull(cookies) && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                // 如果cookie中有 user-key信息
                if (CartConstant.TEMP_USER_COOKIE_NAME.equals(name)) {
                    String userKey = cookie.getValue();
                    // 把临时用户id 存入 To对象中
                    userInfoTo.setUserKey(userKey);
                    userInfoTo.setTempUser(true);
                }
            }
        }

        if (StringUtils.isEmpty(userInfoTo.getUserKey())) {
            // 如果没有 自动生成一个随机的用户key
            String userKey = UUID.randomUUID().toString();
            userInfoTo.setUserKey(userKey);

        }

        // 目标方法执行之前
        userInfoToThreadLocal.set(userInfoTo);

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfoTo userInfoTo = userInfoToThreadLocal.get();

        // 如果已存在就不刷新cookie了 不存在才刷新
        if (!userInfoTo.getTempUser()) {
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey());
            cookie.setDomain("ysymall.com");
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }

    }
}
