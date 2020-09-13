package com.ysy.tmall.ware;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.retry.backoff.Sleeper;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@Slf4j
class TmallWareApplicationTests {

    @Resource
    RabbitTemplate rabbitTemplate;


    @Test
    void contextLoads() throws InterruptedException {
        TimeUnit.SECONDS.sleep(10);
        try {

            rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", "1234");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("信息发送失败", e);
        }
    }

}
