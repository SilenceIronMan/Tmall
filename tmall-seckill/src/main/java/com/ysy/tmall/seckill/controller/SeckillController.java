package com.ysy.tmall.seckill.controller;

import com.ysy.tmall.common.utils.R;
import com.ysy.tmall.seckill.service.SeckillService;
import com.ysy.tmall.seckill.to.SeckillSkuRedisTo;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @anthor silenceYin
 * @date 2020/9/11 - 0:29
 */
@RestController
public class SeckillController {

    @Resource
    private SeckillService seckillService;


    /**
     * 返回当前时间可以参与秒杀的商品
     * @return
     */
    @GetMapping("/currentSeckillSkus")
    public R getCurrentSeckillSkus() {
        List<SeckillSkuRedisTo> vos =  seckillService.getCurrentSeckillSkus();
        return R.ok().setData(vos);
    }

    /**
     * 获取当前时间秒杀商品信息
     * @return
     */
    @GetMapping("/sku/seckill/{skuId}")
    public R getSkuSeckillInfo(@PathVariable("skuId")Long skuId) {
        SeckillSkuRedisTo to = seckillService.getSkuSeckillInfo(skuId);
        return R.ok().setData(to);
    }

    /**
     * 获取当前时间秒杀商品信息
     * @return
     */
    @GetMapping("/kill")
    public R secKill(@RequestParam("killId") String killId,
                     @RequestParam("key") String key,
                     @RequestParam("num") Integer num) {
        // 1.登陆拦截 拦截器 判断了
        String orderSn = seckillService.kill(killId, key, num);
        return R.ok().setData(orderSn);
    }

}
