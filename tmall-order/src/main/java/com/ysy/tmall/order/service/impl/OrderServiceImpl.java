package com.ysy.tmall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.ysy.tmall.common.to.producttocoupon.SkuHasStockVo;
import com.ysy.tmall.common.utils.R;
import com.ysy.tmall.common.vo.MemberResponseVO;
import com.ysy.tmall.order.feign.CartFeignService;
import com.ysy.tmall.order.feign.MemberFeignService;
import com.ysy.tmall.order.feign.WareFeignService;
import com.ysy.tmall.order.interceptor.LoginUserInterceptor;
import com.ysy.tmall.order.vo.MemberAddressVo;
import com.ysy.tmall.order.vo.OrderConfirmVo;
import com.ysy.tmall.order.vo.OrderItemVo;
import io.netty.util.concurrent.CompleteFuture;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ysy.tmall.common.utils.PageUtils;
import com.ysy.tmall.common.utils.Query;

import com.ysy.tmall.order.dao.OrderDao;
import com.ysy.tmall.order.entity.OrderEntity;
import com.ysy.tmall.order.service.OrderService;
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


        CompletableFuture<Void> integrationFuture = CompletableFuture.runAsync(() -> {
            // 獲取積分信息
            Integer integration = memberResponseVO.getIntegration();
            orderConfirmVo.setIntegration(integration);
        }, executor);

        CompletableFuture.allOf(memberFuture, cartFuture, integrationFuture).get();

        return orderConfirmVo;
    }

}
