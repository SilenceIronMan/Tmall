package com.ysy.tmall.seckill.interceptor;

import com.ysy.tmall.common.constant.AuthServerConstant;
import com.ysy.tmall.common.vo.MemberResponseVO;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * @anthor silenceYin
 * @date 2020/8/1 - 22:31
 */
@Component
public class LoginUserInterceptor implements HandlerInterceptor {
    public static ThreadLocal<MemberResponseVO> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 调用feign  放行
        // URL 是带主机地址的 url 是主机地址之后的部分
        String requestURI = request.getRequestURI();
        boolean match = new AntPathMatcher().match("/kill", requestURI);

        if (match) {
            MemberResponseVO attribute = (MemberResponseVO)request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
            if (Objects.nonNull(attribute)) {
                loginUser.set(attribute);
                return true;
            } else {
                // 未登录用户无法访问订单模块
                request.getSession().setAttribute("msg", "请先进行登陆");

                response.sendRedirect("http://auth.ysymall.com/login.html");
                return false;
            }
        }

            return true;

    }
}
