package com.ysy.tmall.search.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * @anthor silenceYin
 * @date 2020/7/25 - 15:28
 */
@Configuration
public class MySessionConfig {
    /**
     * 自定义cookie序列化
     * @return
     */
    @Bean
    public CookieSerializer cookieSerializer(){
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();

        cookieSerializer.setDomainName("ysymall.com");//指定作用域  扩大作用域到父域
        cookieSerializer.setCookieName("yyySESSION");//指定名字
        return cookieSerializer;
    }

    /**
     * session存储到redis序列化 使用json
     * @return
     */
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }
}
