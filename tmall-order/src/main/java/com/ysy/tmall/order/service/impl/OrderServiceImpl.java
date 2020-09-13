package com.ysy.tmall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ysy.tmall.common.to.SkuHasStockVo;
import com.ysy.tmall.common.to.mq.OrderTo;
import com.ysy.tmall.common.to.mq.SeckillOrderTo;
import com.ysy.tmall.common.utils.PageUtils;
import com.ysy.tmall.common.utils.Query;
import com.ysy.tmall.common.utils.R;
import com.ysy.tmall.common.vo.MemberResponseVO;
import com.ysy.tmall.order.constant.OrderConstant;
import com.ysy.tmall.order.dao.OrderDao;
import com.ysy.tmall.order.entity.OrderEntity;
import com.ysy.tmall.order.entity.OrderItemEntity;
import com.ysy.tmall.order.entity.PaymentInfoEntity;
import com.ysy.tmall.order.enume.OrderStatusEnum;
import com.ysy.tmall.order.feign.CartFeignService;
import com.ysy.tmall.order.feign.MemberFeignService;
import com.ysy.tmall.order.feign.ProductFeignService;
import com.ysy.tmall.order.feign.WareFeignService;
import com.ysy.tmall.order.interceptor.LoginUserInterceptor;
import com.ysy.tmall.order.service.OrderItemService;
import com.ysy.tmall.order.service.OrderService;
import com.ysy.tmall.order.service.PaymentInfoService;
import com.ysy.tmall.order.vo.*;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private ThreadLocal<OrderSubmitVo> orderSubmitVoThreadLocal = new ThreadLocal<>();

    @Resource
    private MemberFeignService memberFeignService;

    @Resource
    private CartFeignService cartFeignService;

    @Resource
    private WareFeignService wareFeignService;

    @Resource
    private ProductFeignService productFeignService;

    @Resource
    private ThreadPoolExecutor executor;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Resource
    private OrderItemService orderItemService;

    @Resource
    private PaymentInfoService paymentInfoService;

    @Resource
    private RabbitTemplate rabbitTemplate;



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
    // @GlobalTransactional SEATA AT模式 不适合高并发
    @Transactional
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        orderSubmitVoThreadLocal.set(vo);
        SubmitOrderResponseVo response = new SubmitOrderResponseVo();
        // 成功
        response.setCode(0);

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
            // 令牌验证失败
            response.setCode(1);
            return response;
        } else {
            // 创建订单 验证令牌 验证价格 锁库存
            // 1.创建订单
            OrderCreateTo order = createOrder();

            // 2.验证价格
            // 最新價格
            BigDecimal payAmount = order.getOrder().getPayAmount();
            // 畫面應付價格
            BigDecimal payPrice = vo.getPayPrice();
            // 誤差值允許 0.01 應爲實際價格減去優惠價格后(打折等) 會出現2位以上
            // 我們應該認爲(12.4222 等於 12.4299 這種)
            double abs = Math.abs(payAmount.subtract(payPrice).doubleValue());
            if (abs < 0.01) {
                // 金額對比成功
                // 3.保存訂單數據
                saveOrder(order);
                // 4.鎖定庫存，只要有異常就回滾

                WareSkuLockVo wareSkuLockVo = new WareSkuLockVo();
                wareSkuLockVo.setOrderSn(order.getOrder().getOrderSn());
                List<OrderItemVo> lockItems = order.getItems().stream().map(item -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    // 只需要skuid 和 quality 数量
                    orderItemVo.setCount(item.getSkuQuantity());
                    orderItemVo.setSkuId(item.getSkuId());
                    // orderItemVo.setTitle(item.getSkuName());
                    return orderItemVo;
                }).collect(Collectors.toList());
                wareSkuLockVo.setLocks(lockItems);

                // 远程锁库存
                R r = wareFeignService.orderLockStock(wareSkuLockVo);

                // TODO 测试订单异常状态
                // int i = 10/0;

                // 库存成功了, 网络原因超时 订单回滚 库存未回滚
                // 为了保证高并发.库存 服务自己回滚. 可以发消息给库存
                if (r.getCode() == 0) {
                    OrderEntity orderInfo = order.getOrder();
                    response.setOrder(orderInfo);

                    // 订单创建成功 发送消息
                    rabbitTemplate.convertAndSend("order-event-exchange","order.create.order", orderInfo);

                    return response;

                } else {
                    // 失败

                    response.setCode(3);
                    return response;
                }



            } else {

                // 金額對比失敗
                response.setCode(2);
                return response;
            }

        }
    }

    @Override
    public OrderEntity getOrderStatus(String orderSn) {
        OrderEntity order = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        return order;
    }

    /**
     * 获取订单信息
     * @param orderSn
     * @return
     */
    @Override
    public PayVo getOrderSn(String orderSn) {

        OrderEntity orderEntity = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        PayVo payVo = new PayVo();
        payVo.setOut_trade_no(orderSn);
        BigDecimal payAmount = orderEntity.getPayAmount();
        payAmount.setScale(2, BigDecimal.ROUND_HALF_UP);
        payVo.setTotal_amount(payAmount.toString());

        List<OrderItemEntity> orderItems = orderItemService
                .list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn));
        // 去第一个商品名作为支付宝的订单名字
        OrderItemEntity orderItemEntity = orderItems.get(0);
        String skuName = orderItemEntity.getSkuName();
        payVo.setSubject(skuName);

        payVo.setBody(orderItemEntity.getSkuAttrsVals());
        return payVo;
    }

    @Override
    public void closeOrder(OrderEntity entity) {
        // 关单前先查询订单的状态
        OrderEntity orderEntity = this.getById(entity.getId());

        // 未付款订单可以关闭
        if (orderEntity.getStatus() == OrderStatusEnum.CREATE_NEW.getCode()) {
            OrderEntity order = new OrderEntity();
            order.setId(orderEntity.getId());
            order.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(order);
            /* 订单关闭成功 通知 库存解锁 (虽然库存解锁队列的时间正常情况一定在订单关闭之后)
             但是有可能由于网络等原因导致了库存解锁队列的消息先被获取了
             所以这边有必要 再去提醒一遍 双重保险
            */

            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity, orderTo);
            try {
                //TODO 保证消息一定会发送出去，，每一个消息都可以做好日志记录，（给数据库保存每一个消息的详细信息）
                //TODO 定期扫描数据库将失败的消息再发送一遍
                //发送订单关单消息给mq  库存服务（其他服务）监听消息 执行解锁库存
                rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
            } catch (AmqpException e) {
                //TODO 将没发送成功的消息进行重试发送
                e.printStackTrace();
            }

        }

    }

    /**
     * 查询当前登录用户所有订单
     * @param params
     * @return
     */
    @Override
    public PageUtils listWithItem(Map<String, Object> params) {
        MemberResponseVO memberResponseVO = LoginUserInterceptor.loginUser.get();
        Long memberId = memberResponseVO.getId();
        IPage<OrderEntity> page = this.page(new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>().eq("member_id", memberId).orderByDesc("id"));

        List<OrderEntityVO> orderEntityVOS = page.getRecords().stream().map(o -> {
            OrderEntityVO orderEntityVO = new OrderEntityVO();
            BeanUtils.copyProperties(o, orderEntityVO);

            List<OrderItemEntity> orderItemEntities = orderItemService.list(new QueryWrapper<OrderItemEntity>()
                    .eq("order_sn", orderEntityVO.getOrderSn()));

            orderEntityVO.setItemEntities(orderItemEntities);
            return orderEntityVO;
        }).collect(Collectors.toList());

        PageUtils pageUtils = new PageUtils(page);
        pageUtils.setList(orderEntityVOS);

        return pageUtils;
    }

    @Override
    @Transactional
    public String handlePayResult(PayAsyncVo vo) {

        //1.保存交易流水
        PaymentInfoEntity paymentInfoEntity = new PaymentInfoEntity();
        paymentInfoEntity.setAlipayTradeNo(vo.getTrade_no());
        paymentInfoEntity.setOrderSn(vo.getOut_trade_no());
        paymentInfoEntity.setPaymentStatus(vo.getTrade_status());
        paymentInfoEntity.setCallbackTime(vo.getNotify_time());

        paymentInfoService.save(paymentInfoEntity);

        String trade_status = vo.getTrade_status();
        //2.修改订单状态信息
        if("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)){
            //支付成功
            String outTradeNo = vo.getOut_trade_no();
            baseMapper.updateOrderStatus(outTradeNo, OrderStatusEnum.PAYED.getCode());
        }

        return "success";
    }

    @Override
    public void createSeckillOrder(SeckillOrderTo seckillOrderTo) {
        //TODO 保存秒杀订单
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(seckillOrderTo.getOrderSn());
        orderEntity.setMemberId(seckillOrderTo.getMemberId());
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        BigDecimal multiply = seckillOrderTo.getSeckillPrice().multiply(new BigDecimal("" + seckillOrderTo.getNum()));
        orderEntity.setPayAmount(multiply);
        this.save(orderEntity);

        //保存订单项信息
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setOrderSn(seckillOrderTo.getOrderSn());
        orderItemEntity.setRealAmount(multiply);
        orderItemEntity.setSkuQuantity(seckillOrderTo.getNum());
        //TODO 获取当前sku的详细信息进行设置          productFeignService。getSpuInfoBySkuId()
        orderItemService.save(orderItemEntity);
    }

    /**
     * 创建订单
     *
     * @return
     */
    private OrderCreateTo createOrder() {
        OrderCreateTo orderCreateTo = new OrderCreateTo();
        // 1.生成订单号
        String orderSn = IdWorker.getTimeId();
        //构建订单信息
        OrderEntity orderEntity = buildOrder(orderSn);

        //2.获取所有订单项
        List<OrderItemEntity> itemEntities = buildOrderItems(orderSn);

        //3.计算价格,积分相关
        computePrice(orderEntity, itemEntities);

        orderCreateTo.setOrder(orderEntity);
        orderCreateTo.setItems(itemEntities);
        return orderCreateTo;
    }

    /**
     * 验证价格
     * @param orderEntity
     * @param itemEntities
     */
    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> itemEntities) {

        BigDecimal total = new BigDecimal("0.0");
        BigDecimal totalPromotionAmount = new BigDecimal("0.0");
        BigDecimal totalCouponAmount = new BigDecimal("0.0");
        BigDecimal totalIntegrationAmount = new BigDecimal("0.0");
        Integer totalGiftGrowth = 0;
        Integer totlGiftIntegration = 0;
        for (OrderItemEntity itemEntity : itemEntities) {
            BigDecimal realAmount = itemEntity.getRealAmount();
            total = total.add(realAmount);

            // 优惠
            BigDecimal promotionAmount = itemEntity.getPromotionAmount();
            BigDecimal couponAmount = itemEntity.getCouponAmount();
            BigDecimal integrationAmount = itemEntity.getIntegrationAmount();
            totalPromotionAmount = totalPromotionAmount.add(promotionAmount);
            totalCouponAmount = totalCouponAmount.add(couponAmount);
            totalIntegrationAmount = totalIntegrationAmount.add(integrationAmount);

            // 积分 + 成长值
            Integer giftGrowth = itemEntity.getGiftGrowth();
            Integer giftIntegration = itemEntity.getGiftIntegration();
            totalGiftGrowth = totalGiftGrowth + giftGrowth;
            totlGiftIntegration = totlGiftIntegration + giftIntegration;
        }
        // 减去优惠信息的总额度
        orderEntity.setTotalAmount(total);
        // 应付总额
        BigDecimal payAmount = total.add(orderEntity.getFreightAmount());
        orderEntity.setPayAmount(payAmount);

        // 各项优惠总额(整个订单的优惠总额 即各个商品的优惠总额之和)
        orderEntity.setPromotionAmount(totalPromotionAmount);
        orderEntity.setCouponAmount(totalCouponAmount);
        orderEntity.setIntegrationAmount(totalIntegrationAmount);

        // 订单状态
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        // 自动确认时间 理论上所有订单的时间是一样的 这个7可以提出去变为全局变量
        orderEntity.setAutoConfirmDay(7);

        // 积分 + 成长值
        orderEntity.setGrowth(totalGiftGrowth);
        orderEntity.setIntegration(totlGiftIntegration);

        // 订单删除状态 (未删除)
        orderEntity.setDeleteStatus(0);

    }

    /**
     * 获取订单项
     * @param orderSn
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        // 最后确定每个购物项的价格（這邊調用的遠程接口 會取最新價格 所以不用擔心）
        List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
        if (currentUserCartItems != null && currentUserCartItems.size() > 0) {
            List<OrderItemEntity> orderItemEntities = currentUserCartItems.stream().map(cartItem -> {
                OrderItemEntity orderItemEntity = buildOrderItems(cartItem, orderSn);
                return orderItemEntity;
            }).collect(Collectors.toList());
            return orderItemEntities;
        }

        return null;
    }

    /**
     * 获取订单商品列表
     * @param cartItem
     * @param orderSn
     * @return
     */
    private OrderItemEntity buildOrderItems(OrderItemVo cartItem, String orderSn) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        // 1. 订单信息 订单号
        orderItemEntity.setOrderSn(orderSn);
        // 2. 商品的spu信息
        Long skuId = cartItem.getSkuId();
        R spuInfoBySkuId = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo spuInfo = spuInfoBySkuId.getData("spuInfo", new TypeReference<SpuInfoVo>() {
        });
        orderItemEntity.setSpuId(spuInfo.getId());
        orderItemEntity.setSpuBrand(spuInfo.getBrandId().toString());
        orderItemEntity.setSpuName(spuInfo.getSpuName());
        orderItemEntity.setCategoryId(spuInfo.getCatalogId());

        // 3. 商品的sku信息
        orderItemEntity.setSkuId(cartItem.getSkuId());
        orderItemEntity.setSkuName(cartItem.getTitle());
        orderItemEntity.setSkuPic(cartItem.getImage());
        BigDecimal price = cartItem.getPrice();
        orderItemEntity.setSkuPrice(price);
        List<String> skuAttr = cartItem.getSkuAttr();
        orderItemEntity.setSkuAttrsVals(StringUtils.collectionToDelimitedString(skuAttr, ";"));
        Integer count = cartItem.getCount();
        orderItemEntity.setSkuQuantity(count);
        // 4. 优惠信息[不做] 图省事
        // 5. 积分信息
        // 这边图省事就不从表 tmall-sms sms_spu_bounds中取了
        orderItemEntity.setGiftGrowth((price.multiply(new BigDecimal(count.toString()))).intValue());
        orderItemEntity.setGiftIntegration((price.multiply(new BigDecimal(count.toString()))).intValue());

        // 6.订单项价格信息 偷懒写死 应该远程查询
        orderItemEntity.setPromotionAmount(new BigDecimal("0.0") );
        orderItemEntity.setCouponAmount(new BigDecimal("0.0") );
        orderItemEntity.setIntegrationAmount(new BigDecimal("0.0") );
        // 实际金额
        BigDecimal originPrice = price.multiply(new BigDecimal(count.toString()));
        BigDecimal realPrice = originPrice.subtract(orderItemEntity.getPromotionAmount())
                .subtract(orderItemEntity.getCouponAmount())
                .subtract(orderItemEntity.getIntegrationAmount());
        orderItemEntity.setRealAmount(realPrice);

        return orderItemEntity;
    }

    /**
     * 生成订单
     * @param orderSn  订单号
     * @return
     */
    private OrderEntity buildOrder(String orderSn) {
        OrderEntity orderEntity = new OrderEntity();
        // 1. 設置订单号
        orderEntity.setOrderSn(orderSn);

        // 設置會員id
        MemberResponseVO memberResponseVO = LoginUserInterceptor.loginUser.get();
        orderEntity.setMemberId(memberResponseVO.getId());

        OrderSubmitVo orderSubmitVo = orderSubmitVoThreadLocal.get();
        // 2.生成远程地址对象
        R fare = wareFeignService.getFare(orderSubmitVo.getAddrId());
        FareVo fareVo = fare.getData(new TypeReference<FareVo>() {
        });
        MemberAddressVo address = fareVo.getAddress();
        // 运费金额
        orderEntity.setFreightAmount(fareVo.getFare());
        // 收货城市
        orderEntity.setReceiverCity(address.getCity());
        // 收货人姓名
        orderEntity.setReceiverName(address.getName());
        // 收货人电话
        orderEntity.setBillReceiverPhone(address.getPhone());
        // 收货人邮编
        orderEntity.setReceiverPostCode(address.getPostCode());
        // 收货人省份
        orderEntity.setReceiverProvince(address.getProvince());
        // 收货人区
        orderEntity.setReceiverRegion(address.getRegion());
        // 收货人详细地址
        orderEntity.setReceiverDetailAddress(address.getDetailAddress());
        // 订单备注
        orderEntity.setNote(orderSubmitVo.getNote());

        return orderEntity;
    }

    /**
     * 保存订单数据
     *
     * @param order
     */
    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        this.save(orderEntity);

        List<OrderItemEntity> orderItems = order.getItems();
        orderItemService.saveBatch(orderItems);
    }


}
