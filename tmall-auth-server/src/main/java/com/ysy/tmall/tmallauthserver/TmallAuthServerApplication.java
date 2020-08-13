package com.ysy.tmall.tmallauthserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableRedisHttpSession // 整合redis 处理session问题
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class TmallAuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TmallAuthServerApplication.class, args);
    }

}
