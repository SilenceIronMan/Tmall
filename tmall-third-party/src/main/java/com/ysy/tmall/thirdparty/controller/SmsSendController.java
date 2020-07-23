package com.ysy.tmall.thirdparty.controller;

import com.ysy.tmall.common.utils.R;
import com.ysy.tmall.thirdparty.component.SmsComponent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @anthor silenceYin
 * @date 2020/7/22 - 23:07
 */
@RestController
@RequestMapping("/sms")
public class SmsSendController {

    @Resource
    private SmsComponent smsComponent;
    /**
     * 供其他服务调用
     * @param phone
     * @param code
     * @return
     */
    @GetMapping("/sendCode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code) {
        // 有点贫穷 这边就模拟验证码发送了
        //smsComponent.sendMessage(phone, code);

        return R.ok();
    }
}
