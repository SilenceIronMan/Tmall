package com.ysy.tmall.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;



@SpringBootApplication
@EnableDiscoveryClient
public class TmallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(TmallProductApplication.class, args);
    }

}
