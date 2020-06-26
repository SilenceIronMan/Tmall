package com.ysy.tmall.member;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class TmallMemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(TmallMemberApplication.class, args);
    }

}
