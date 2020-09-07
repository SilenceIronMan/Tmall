package com.ysy.tmall.seckill;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @anthor silenceYin
 * @date 2020/9/1 - 0:38
 */
@Component
public class Test {



    @Async
    //@Scheduled(cron = "* * * ? * *")
    public void test () {

        String name = Thread.currentThread().getName();
        System.out.println(name);
    }
}
