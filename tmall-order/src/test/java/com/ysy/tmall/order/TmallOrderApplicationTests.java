package com.ysy.tmall.order;

import com.ysy.tmall.order.entity.OrderEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
@Slf4j
class TmallOrderApplicationTests {

    @Autowired
    AmqpAdmin amqpAdmin;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    void createExchange() {
        DirectExchange directSrpringExchange = new DirectExchange("directsrpringexchange",true, false);
        amqpAdmin.declareExchange(directSrpringExchange);
        log.info("创建{}交换机", directSrpringExchange.getType());

    }

    @Test
    void createQueue() {
        Queue queue = new Queue("springQueue", true);
        String s = amqpAdmin.declareQueue(queue);
        log.info("创建{}队列", s);

    }

    @Test
    void createBinding () {
        Binding binding = new Binding("springQueue", Binding.DestinationType.QUEUE, "directsrpringexchange", "springQueue222", null);
        amqpAdmin.declareBinding(binding);
        log.info("创建{}绑定", binding);

    }

    @Test
    void sendmessage() {
        for (int i = 0; i < 99; i++) {
            OrderEntity orderEntity = new OrderEntity();
            orderEntity.setId(123123L);
            // 如果发送的消息是对象 对象需要实现 Serializable
            rabbitTemplate.convertAndSend("directsrpringexchange", "springQueue222", orderEntity);
            //rabbitTemplate.receiveAndConvert()
        }

    }

    @Test
    @RabbitListener(queues = "springQueue")
    void getMessage(Message message, OrderEntity orderEntity) {
        log.info("===============message" + message + orderEntity);

    }

}
