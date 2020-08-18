package com.ysy.tmall.ware.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @anthor silenceYin
 * @date 2020/7/30 - 1:37
 */
@Configuration
public class MyRabbitConfig {

//    @RabbitListener(queues = "order.release.order.queue")
//    public void listener(Message message) {
//        System.out.println(new String(message.getBody()));
//    }

    @Autowired
    CachingConnectionFactory cachingConnectionFactory;

    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(cachingConnectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        //设置确认回调
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {

            /**
             * 只要消息抵达服务器  ack就为true
             * @param correlationData   当前消息的唯一关联数据（消息唯一id）
             * @param ack    Rabbit是否成功收到消息
             * @param s     失败原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String s) {
                /**
                 * 1.做好消息确认机制（发送方，消费方【手动ack】）
                 * 2.每一个发送的消息都在数据库做好日志记录（mq_message表）.定期将失败的消息再次发送
                 */
                //服务器收到了
                //修改消息状态
                System.out.println("confirm..");
            }
        });

        //设置消息抵达确认回调
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {

            /**
             * 只要消息没有投递给指定队列，就触发这个失败回调
             * @param message   投递失败的消息详细信息
             * @param replyCode 回复状态码
             * @param replyText 回复的文本内容
             * @param exchange  当时这个消息发给哪个交换机
             * @param routingKey 当时这个消息用哪个路由键
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                //报错了，修改数据库当前消息的状态 -》错误。
            }
        });
        return rabbitTemplate;
    }


    /**
     * 配置消息类型转换器
     * 使用json序列化，进行消息转换
     *
     * @return
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 库存交换机
     * @return
     */
    @Bean
    public Exchange stockEventExchange() {
        // String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        return new TopicExchange("stock-event-exchange", true, false);
    }

    /**
     * 消息库存解锁队列
     * @return
     */
    @Bean
    public Queue stockReleaseStockQueue() {
        // String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        return new Queue("stock.release.stock.queue", true, false, false);
    }

    /**
     * 消息库存死信队列
     * @return
     */
    @Bean
    public Queue stockDelayQueue() {
        // String name, boolean durable, boolean autoDelete, Map<String, Object> arguments

        Map<String, Object> arguments = new HashMap<>();
        // x-dead-letter-exchange x-dead-letter-routing-key x-message-ttl
        //死信路由
        arguments.put("x-dead-letter-exchange", "stock-event-exchange");
        //死信路由键
        arguments.put("x-dead-letter-routing-key", "stock.release");
        //过期时间
        arguments.put("x-message-ttl", 120000);
        Queue queue = new Queue("stock.delay.queue", true, false, false, arguments);
        return queue;
    }

    /**
     * 库存锁定binding
     * @return
     */
    @Bean
    public Binding stockLockedBinding() {

        return new Binding("stock.delay.queue", Binding.DestinationType.QUEUE, "stock-event-exchange", "stock.locked", null);
    }

    /**
     * 库存解锁binding
     * @return
     */
    @Bean
    public Binding stockReleaseBinding() {

        return new Binding("stock.release.stock.queue", Binding.DestinationType.QUEUE, "stock-event-exchange", "stock.release.#", null);
     }
     /**
     * 定制RabbitTemplate
     * 1.服务收到消息就回调
     *      1.spring.rabbitmq.publisher-confirms=true
     *      2.设置确认回调ConfirmCallback
     * 2.消息正确抵达队列回调
     *      1. spring.rabbitmq.publisher-returns=true
     *         spring.rabbitmq.template.mandatory=true
     *      2.设置消息抵达确认回调 ReturnCallback
     *
     * 3. 消费端确认（保证每个消息被正确消费，此时才可以broker删除这个消息）
     *      手动签收模式： pring.rabbitmq.listener.simple.acknowledge-mode=manual
     *     1、默认是自动确认，只要消息接收到，客户端会自动确认，服务端就会移除这个消息
     *          问题：
     *              收到很多消息，自动回复给服务器ack,只有一个息消处理成功，宕机了，发送消息丢失。
     *
     *          手动确认模式。只要没有明确告诉mq，没有Ack，消息就一直是unacked状态。
     *                 即使Consumer宕机，消息不会丢失，会重新变为Ready,下一次有新的Consumer连接进来就发给它。
     *
     *      2.如何签收
     *          channel.basicAck(deliveryTag,false);签收          业务成功完成，签收
     *          channel.basicNack(deliveryTag,false,true);拒签    业务失败，拒签
     *
     */
    //@PostConstruct  //MyRabbitConfig 对象创建完成以后执行
   /* public void initRabbitTemplate(){
        //设置确认回调
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback(){
            *//**
     * 只要消息抵达服务器  ack就为true
     * @param correlationData   当前消息的唯一关联数据（消息唯一id）
     * @param ack    Rabbit是否成功收到消息
     * @param s     失败原因
     *//*
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String s) {
                *//**
     * 1.做好消息确认机制（发送方，消费方【手动ack】）
     * 2.每一个发送的消息都在数据库做好日志记录（mq_message表）.定期将失败的消息再次发送
     *//*
                //服务器收到了
                //修改消息状态
                System.out.println("confirm..");
            }
        });
        //设置消息抵达确认回调
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            *//**
     * 只要消息没有投递给指定队列，就触发这个失败回调
     * @param message   投递失败的消息详细信息
     * @param replyCode 回复状态码
     * @param replyText 回复的文本内容
     * @param exchange  当时这个消息发给哪个交换机
     * @param routingKey 当时这个消息用哪个路由键
     *//*
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                //报错了，修改数据库当前消息的状态 -》错误。
            }
        });
    }*/


}
