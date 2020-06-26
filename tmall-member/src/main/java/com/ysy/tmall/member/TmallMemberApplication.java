package com.ysy.tmall.member;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class TmallMemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(TmallMemberApplication.class, args);
    }

}
