package com.ysy.tmall.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * Feign 拦截器 添加请求头 cookie等信息
 * @anthor silenceYin
 * @date 2020/8/6 - 23:29
 */
@Configuration
public class MyFeignConfig {


    @Bean
    public RequestInterceptor requestInterceptor(){
        return requestTemplate -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if(attributes!=null){
                HttpServletRequest request = attributes.getRequest();
                if(request!=null){
                    //同步请求头数据，Cookie
                    String cookie = request.getHeader("Cookie");
                    requestTemplate.header("Cookie",cookie);
                }
            }
        };
    }
}
