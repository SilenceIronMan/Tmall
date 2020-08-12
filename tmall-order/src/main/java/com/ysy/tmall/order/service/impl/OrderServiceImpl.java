package com.ysy.tmall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.ysy.tmall.common.to.producttocoupon.SkuHasStockVo;
import com.ysy.tmall.common.utils.R;
import com.ysy.tmall.common.vo.MemberResponseVO;
import com.ysy.tmall.order.constant.OrderConstant;
import com.ysy.tmall.order.feign.CartFeignService;
import com.ysy.tmall.order.feign.MemberFeignService;
import com.ysy.tmall.order.feign.WareFeignService;
import com.ysy.tmall.order.interceptor.LoginUserInterceptor;
import com.ysy.tmall.order.vo.*;
import io.netty.util.concurrent.CompleteFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ysy.tmall.common.utils.PageUtils;
import com.ysy.tmall.common.utils.Query;

import com.ysy.tmall.order.dao.OrderDao;
import com.ysy.tmall.order.entity.OrderEntity;
import com.ysy.tmall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.annotation.Resource;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Resource
    private MemberFeignService memberFeignService;

    @Resource
    private CartFeignService cartFeignService;

    @Resource
    private WareFeignService wareFeignService;

    @Resource
    private ThreadPoolExecutor executor;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        // 这边肯定有值了不然拦截器都过不了
        MemberResponseVO memberResponseVO = LoginUserInterceptor.loginUser.get();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();


        CompletableFuture<Void> memberFuture = CompletableFuture.runAsync(() -> {
            // 异步情况下 线程不一致 导致获取不到request上下文 所以给当前线程设置 service方法线程下的 request上下文
            RequestContextHolder.setRequestAttributes(requestAttributes);

            // 获取会员id
            Long memberId = memberResponseVO.getId();
            List<MemberAddressVo> memberAddressVos = memberFeignService.listAddress(memberId);
            orderConfirmVo.setAddress(memberAddressVos);
        }, executor);


        CompletableFuture<Void> cartFuture = CompletableFuture.supplyAsync(() -> {
            // 异步情况下 线程不一致 导致获取不到request上下文 所以给当前线程设置 service方法线程下的 request上下文
            RequestContextHolder.setRequestAttributes(requestAttributes);

            // 获取购物车选中的信息
            List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
            orderConfirmVo.setItems(currentUserCartItems);
            return currentUserCartItems;
        }, executor).thenAcceptAsync((items) -> {
            // 所有的skuIdlist
            List<Long> skuIds = items.stream().map(item -> item.getSkuId()).collect(Collectors.toList());
            // 查询sku是否有货
            R skuHasStock = wareFeignService.getSkuHasStock(skuIds);
            List<SkuHasStockVo> stockData = skuHasStock.getData(new TypeReference<List<SkuHasStockVo>>() {
            });
            if (stockData != null) {
                Map<Long, Boolean> stockMap = stockData.stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock, (v1, v2) -> v1));
                orderConfirmVo.setStocks(stockMap);
            }

        });
        // 獲取積分信息
        Integer integration = memberResponseVO.getIntegration();
        orderConfirmVo.setIntegration(integration);


        CompletableFuture.allOf(memberFuture, cartFuture).get();

        String token = UUID.randomUUID().toString().replace("-", "");
        orderConfirmVo.setOrderToken(token);
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVO.getId(), token, 30, TimeUnit.MINUTES);

        return orderConfirmVo;
    }

    @Override
    @Transactional
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        SubmitOrderResponseVo response = new SubmitOrderResponseVo();


        MemberResponseVO memberResponseVO = LoginUserInterceptor.loginUser.get();

        String orderToken = vo.getOrderToken();
        //1.验证令牌【令牌的对比和删除必须保证原子性】
        //0令牌失败   1删除成功
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        //原子验证令牌和删除令牌
        Long execute = redisTemplate
                .execute(new DefaultRedisScript<>(script, Long.class),
                        Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVO.getId()),
                        orderToken);
//        String redisToken = redisTemplate.opsForValue().get(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVO.getId());
//        if (orderToken != null && orderToken.equals(redisToken)) {
//            // 通过验证
//            redisTemplate.delete(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVO.getId()));
//        } else {
//
//        }

        if (execute == 0L) {
            //令牌验证失败
            response.setCode(1);
            return response;
        } else {
            // 创建订单 验证令牌 验证价格 锁库存
        }
        return null;
    }

}
