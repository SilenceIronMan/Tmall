package com.ysy.tmall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@MapperScan("com.ysy.tmall.product.dao")
@SpringBootApplication
public class TmallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(TmallProductApplication.class, args);
    }

}
