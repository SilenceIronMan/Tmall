package com.ysy.tmall.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class TmallGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(TmallGatewayApplication.class, args);
    }

}
