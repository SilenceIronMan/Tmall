package com.ysy.tmall.member.web;

import com.alibaba.fastjson.TypeReference;
import com.ysy.tmall.common.utils.R;
import com.ysy.tmall.common.vo.MemberResponseVO;
import com.ysy.tmall.member.feign.OrderFeignService;
import com.ysy.tmall.member.interceptor.LoginUserInterceptor;
import com.ysy.tmall.member.vo.OrderEntityVO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @anthor silenceYin
 * @date 2020/8/28 - 2:16
 */
@Controller
public class MemberWebController {

    @Resource
    private OrderFeignService orderFeignService;

    /**
     * 当前登录用户订单列表
     * @return
     */
    @GetMapping("/memberOrder.html")
    public String memberOrder(@RequestParam(value = "pageNum", defaultValue = "1") String pageNum,
                              @RequestParam(value = "pageSize", defaultValue = "10") String pageSize,
                              Model model) {

        HashMap<String, Object> map = new HashMap<>();
        map.put("page", pageNum);
        map.put("limit", pageSize);
        R r = orderFeignService.listWithItem(map);
        model.addAttribute("orders", r);
        return "orderList";
    }
}
