package com.ysy.tmall.order;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableRabbit
@EnableRedisHttpSession
@SpringBootApplication
@EnableDiscoveryClient
public class TmallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(TmallOrderApplication.class, args);
    }

}
