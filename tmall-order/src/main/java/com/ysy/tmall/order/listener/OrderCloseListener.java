package com.ysy.tmall.order.listener;

import com.rabbitmq.client.Channel;
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
@RabbitListener(queues = "order.release.order.queue")
@Component
@Slf4j
public class OrderCloseListener {

    @Resource
    private OrderService orderService;

    @RabbitHandler
    public void listener(OrderEntity entity, Channel channel, Message message) throws IOException {
        log.info("收到过期的订单信息: 准备关闭订单" + entity.getOrderSn());
        try {
            orderService.closeOrder(entity);
            // TODO 手动调用支付宝收单功能

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            log.error("关闭订单信息接收异常", e);

            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }


}
