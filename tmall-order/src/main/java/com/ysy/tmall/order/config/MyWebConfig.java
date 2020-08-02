package com.ysy.tmall.order.config;

import com.ysy.tmall.order.interceptor.LoginUserInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * @anthor silenceYin
 * @date 2020/8/1 - 12:33
 */
@Configuration
public class MyWebConfig implements WebMvcConfigurer {

    @Resource
    private LoginUserInterceptor loginUserInterceptor;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/pay.html").setViewName("pay");
        registry.addViewController("/detail.html").setViewName("detail");
        registry.addViewController("/list.html").setViewName("list");
        registry.addViewController("/confirm.html").setViewName("confirm");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginUserInterceptor).addPathPatterns("/**");
    }
}
