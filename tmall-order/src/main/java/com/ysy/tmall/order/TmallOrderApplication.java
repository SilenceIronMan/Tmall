package com.ysy.tmall.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class TmallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(TmallOrderApplication.class, args);
    }

}
