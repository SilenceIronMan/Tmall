package com.ysy.tmall.order.listener;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.ysy.tmall.order.config.AlipayTemplate;
import com.ysy.tmall.order.entity.OrderEntity;
import com.ysy.tmall.order.enume.OrderStatusEnum;
import com.ysy.tmall.order.service.OrderService;
import com.ysy.tmall.order.vo.PayAsyncVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 支付宝异步监听器
 *
 * @anthor silenceYin
 * @date 2020/8/30 - 22:51
 */
@RestController
public class OrderPayedListener {


    @Resource
    private OrderService orderService;

    @Resource
    private AlipayTemplate alipayTemplate;

    @PostMapping("/payed/notify")
    public String handleAlipayed(PayAsyncVo vo, HttpServletRequest request) throws AlipayApiException {
        //只要收到了支付宝给我们的异步通知，告诉我们订单支付成功，返回success，支付宝就不再通知
        //验签
        Map<String,String> params = new HashMap<>();
        Map<String,String[]> requestParams = request.getParameterMap();

        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name =  iter.next();
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用 (因为post请求 提交有乱码过滤器 所以不需要)
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }

        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayTemplate.getAlipay_public_key(), alipayTemplate.getCharset(), alipayTemplate.getSign_type()); //调用SDK验证签名

        /* 实际验证过程建议商户务必添加以下校验：
        1、需要验证该通知数据中的out_trade_no是否为商户系统中创建的订单号，
        2、判断total_amount是否确实为该订单的实际金额（即商户订单创建时的金额），
        3、校验通知中的seller_id（或者seller_email) 是否为out_trade_no这笔单据的对应的操作方（有的时候，一个商户可能有多个seller_id/seller_email）
        4、验证app_id是否为该商户本身。
        */
        if(signVerified) {//验证成功
            System.out.println("验签成功");
//
//            String appId=vo.getApp_id();//支付宝分配给开发者的应用Id
//            String totalAmount=vo.getTotal_amount();//订单金额:本次交易支付的订单金额，单位为人民币（元）
//
//            OrderEntity orderEntity = orderService.getOrderByOrderSn(vo.getOut_trade_no());
//            if(orderEntity != null && totalAmount.equals(orderEntity.getTotalAmount().toString())
//                    && alipayTemplate.getApp_id().equals(appId)) {
            String result= orderService.handlePayResult(vo);
//                return result;
//            }else {
//                return "error";
//            }
            return result;
        }else {//验证失败
            System.out.println("验签失败");
            return "error";
        }
    }
}
