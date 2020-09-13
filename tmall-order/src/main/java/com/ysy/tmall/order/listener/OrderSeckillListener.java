package com.ysy.tmall.order.listener;

import com.rabbitmq.client.Channel;
import com.ysy.tmall.common.to.mq.SeckillOrderTo;
import com.ysy.tmall.order.entity.OrderEntity;
import com.ysy.tmall.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @anthor silenceYin
 * @date 2020/8/22 - 21:10
 */
@RabbitListener(queues = "order.seckill.order.queue")
@Component
@Slf4j
public class OrderSeckillListener {

    @Resource
    private OrderService orderService;

    @RabbitHandler
    public void listener(SeckillOrderTo entity, Channel channel, Message message) throws IOException {
        log.info("准备创建秒杀单的详细信息:" + entity.getOrderSn());
        try {
            orderService.createSeckillOrder(entity);
            // TODO 手动调用支付宝收单功能

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            log.error("创建秒杀单订单信息接收异常", e);

            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }


}
