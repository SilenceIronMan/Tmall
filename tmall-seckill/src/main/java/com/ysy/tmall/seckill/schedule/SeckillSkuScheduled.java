package com.ysy.tmall.seckill.schedule;

import com.ysy.tmall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 商品秒杀定时上架
 * 例如  每天晚上三点; 上架最近需要秒杀的商品
 *       当天00:00:00 - 23:59:59
 *       明天00:00:00 - 23:59:59
 *       后天00:00:00 - 23:59:59
 * @anthor silenceYin
 * @date 2020/9/1 - 0:50
 */
@Slf4j
@Service
public class SeckillSkuScheduled {

    @Resource
    private SeckillService seckillService;

    @Resource
    private RedissonClient redissonClient;

    private final String UPLOAD_LOCK = "seckill:upload:lock";

    //@Scheduled(cron = "0 0 3 * * ?")
    @Scheduled(cron = "0 * * * * ?")
    public void uploadSeckillSkuLatest3Days() {
        // 1. 重复上架无需处理
        log.info("上架三天内商品");

        RLock lock = redissonClient.getLock(UPLOAD_LOCK);
        lock.lock(10, TimeUnit.SECONDS);

        // 分布式锁 防止多个定时任务一起执行
        try {
            seckillService.uploadSeckillSkuLatest3Days();
        } finally {
            lock.unlock();
        }

    }

}
