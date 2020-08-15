package com.ysy.tmall.order.web;

import com.ysy.tmall.order.service.OrderService;
import com.ysy.tmall.order.vo.OrderConfirmVo;
import com.ysy.tmall.order.vo.OrderSubmitVo;
import com.ysy.tmall.order.vo.SubmitOrderResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

/**
 * @anthor silenceYin
 * @date 2020/8/6 - 23:14
 */
@Controller
@Slf4j
public class OrderWebController {

    @Autowired
    OrderService orderService;

    /**
     * 跳转 结算页面
     * @param model
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {

        OrderConfirmVo confirmVo = orderService.confirmOrder();

        //展示订单确认的数据
        model.addAttribute("orderConfirmData", confirmVo);
        return "confirm";
    }

    /**
     * 提交订单
     * @param vo
     * @param model
     * @return
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo, Model model, RedirectAttributes redirectAttributes) {
        log.info("下订单传递数据" + vo);
        SubmitOrderResponseVo responseVo = orderService.submitOrder(vo);
        // 创建订单 验证令牌 验证价格 锁库存
        // 下单成功 到支付页面
        // 下单失败 重新确认订单信息
        if (responseVo.getCode() == 0) {
            model.addAttribute("submitOrderResp", responseVo);
            return "pay";
        } else {
            String msg = "下单失败；";
            switch (responseVo.getCode()) {
                case 1:
                    msg += "订单信息过期，请刷新再次提交";
                    break;
                case 2:
                    msg += "订单商品价格发生变化，请确认后再次提交";
                    break;
                case 3:
                    msg += "库存锁定失败，商品库存不足";
                    break;
            }
            redirectAttributes.addFlashAttribute("msg", msg);
            return "redirect:http://order.ysymall.com/toTrade";
        }

    }

}
