package com.ysy.tmall.ware.listener;

import com.rabbitmq.client.Channel;
import com.ysy.tmall.common.to.mq.OrderTo;
import com.ysy.tmall.common.to.mq.StockLockedTo;
import com.ysy.tmall.ware.service.WareSkuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 监听解锁库存消息队列
 * @anthor silenceYin
 * @date 2020/8/22 - 18:24
 */
@RabbitListener(queues = "stock.release.stock.queue")
@Component
@Slf4j
public class StockReleaseListener {

    @Autowired
    WareSkuService wareSkuService;

    /**
     * 监听解锁库存消息
     * 只要解锁库存的消息失败，一定要告诉服务解锁失败
     * @param to
     * @param message
     */
    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo to, Message message, Channel channel) throws IOException {
        log.info("收到解锁库存的消息");
        try {
            //当前消息是否被第二次及以后（重新）派发过来的
//            Boolean redelivered = message.getMessageProperties().getRedelivered();
            wareSkuService.unlockStock(to);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            //消息拒绝以后重新放到队列中，让别人继续消费解锁
            log.info("库存解锁消息处理失败", e);
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }

    /**
     * 监听关闭订单传来的消息
     * @param message
     * @param channel
     * @param orderTo
     * @throws IOException
     */
    @RabbitHandler
    public void handleOrderCloseRelease(Message message, Channel channel, OrderTo orderTo) throws IOException {
        log.info("订单关闭，准备解锁库存....");
        try {
            wareSkuService.unlockStock(orderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {

            log.info("订单关闭, 库存解锁消息处理失败", e);
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
