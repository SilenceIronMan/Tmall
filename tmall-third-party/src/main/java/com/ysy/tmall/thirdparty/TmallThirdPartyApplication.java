package com.ysy.tmall.thirdparty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class TmallThirdPartyApplication {

    public static void main(String[] args) {
        SpringApplication.run(TmallThirdPartyApplication.class, args);
    }

}
