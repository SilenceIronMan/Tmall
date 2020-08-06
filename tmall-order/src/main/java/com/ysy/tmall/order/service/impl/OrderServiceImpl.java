package com.ysy.tmall.order.service.impl;

import com.ysy.tmall.common.vo.MemberResponseVO;
import com.ysy.tmall.order.feign.CartFeignService;
import com.ysy.tmall.order.feign.MemberFeignService;
import com.ysy.tmall.order.interceptor.LoginUserInterceptor;
import com.ysy.tmall.order.vo.MemberAddressVo;
import com.ysy.tmall.order.vo.OrderConfirmVo;
import com.ysy.tmall.order.vo.OrderItemVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ysy.tmall.common.utils.PageUtils;
import com.ysy.tmall.common.utils.Query;

import com.ysy.tmall.order.dao.OrderDao;
import com.ysy.tmall.order.entity.OrderEntity;
import com.ysy.tmall.order.service.OrderService;

import javax.annotation.Resource;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Resource
    private MemberFeignService memberFeignService;

    @Resource
    private CartFeignService cartFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() {
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        // 这边肯定有值了不然拦截器都过不了
        MemberResponseVO memberResponseVO = LoginUserInterceptor.loginUser.get();
        // 获取会员id
        Long memberId = memberResponseVO.getId();
        List<MemberAddressVo> memberAddressVos = memberFeignService.listAddress(memberId);
        orderConfirmVo.setAddress(memberAddressVos);

        // 获取购物车选中的信息
        List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
        orderConfirmVo.setItems(currentUserCartItems);

        // 獲取積分信息
        Integer integration = memberResponseVO.getIntegration();
        orderConfirmVo.setIntegration(integration);


        return orderConfirmVo;
    }

}
