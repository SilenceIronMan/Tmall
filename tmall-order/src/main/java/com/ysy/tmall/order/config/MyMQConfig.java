package com.ysy.tmall.order.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 延时队列  定时关单
 * @anthor silenceYin
 * @date 2020/8/17 - 2:01
 */
@Configuration
public class MyMQConfig {
    // @Bean Binding Queue Exchange
    //延时队列  死信队列

//    @RabbitListener(queues = "order.release.order.queue")
//    public void listener(Message message) {
//        System.out.println(new String(message.getBody()));
//    }

    /**
     * 容器中 Binding Queue Exchange  会自动在rabbitMq中创建(RabbitMq中不存在 该 Binding Queue Exchange 时)
     * @return
     */
    @Bean
    public Queue orderDelayQueue(){
        //Queue(String name, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments)

        Map<String, Object> arguments = new HashMap<>();
        //死信路由
        arguments.put("x-dead-letter-exchange", "order-event-exchange");
        //死信路由键
        arguments.put("x-dead-letter-routing-key", "order.release.order");
        //过期时间
        arguments.put("x-message-ttl", 60000);
        Queue queue = new Queue("order.delay.queue", true, false, false, arguments);
        return queue;
    }

    @Bean
    public Queue orderReleseOrderQueue(){
        Queue queue = new Queue("order.release.order.queue", true, false, false);
        return queue;
    }

    //交换机
    @Bean
    public Exchange orderEventExchange(){
        TopicExchange topicExchange = new TopicExchange("order-event-exchange", true, false);
        return topicExchange;
    }

    //订单的binding
    @Bean
    public Binding orderCreateOrderBinding() {
        Binding binding = new Binding("order.delay.queue", Binding.DestinationType.QUEUE, "order-event-exchange", "order.create.order", null);
        return binding;
    }

    //binding
    @Bean
    public Binding orderReleaseOrderBinding() {
        Binding binding = new Binding("order.release.order.queue", Binding.DestinationType.QUEUE, "order-event-exchange", "order.release.order", null);
        return binding;
    }

    /**
     * 取消订单后同事通知库存释放队列
     * @return
     */
    @Bean
    public Binding orderReleaseOtherBinding() {
        Binding binding = new Binding("stock.release.stock.queue", Binding.DestinationType.QUEUE, "order-event-exchange", "order.release.other.#", null);
        return binding;
    }


    /**
     * 削峰队列Binding
     * @return
     */
    @Bean
    public Binding orderSeckillOrderBinding() {
        Binding binding = new Binding("order.seckill.order.queue", Binding.DestinationType.QUEUE, "order-event-exchange", "order.seckill.order", null);
        return binding;
    }


    /**
     * 削峰队列
     * @return
     */
    @Bean
    public Queue orderSeckillOrderQueue(){
        Queue queue = new Queue("order.seckill.order.queue", true, false, false);
        return queue;
    }

}
