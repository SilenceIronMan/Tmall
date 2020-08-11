package com.ysy.tmall.order.web;

import com.ysy.tmall.order.service.OrderService;
import com.ysy.tmall.order.vo.OrderConfirmVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.concurrent.ExecutionException;

/**
 * @anthor silenceYin
 * @date 2020/8/6 - 23:14
 */
@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;

    //跳转 结算页面
    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {

        OrderConfirmVo confirmVo = orderService.confirmOrder();

        //展示订单确认的数据
        model.addAttribute("orderConfirmData", confirmVo);
        return "confirm";
    }
}
