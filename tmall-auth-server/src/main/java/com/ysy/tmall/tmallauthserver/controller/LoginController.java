package com.ysy.tmall.tmallauthserver.controller;

import com.ysy.tmall.common.exception.BizCodeEnum;
import com.ysy.tmall.common.utils.R;
import com.ysy.tmall.tmallauthserver.constant.AuthServerConstant;
import com.ysy.tmall.tmallauthserver.feign.ThirdPartFeignService;
import com.ysy.tmall.tmallauthserver.vo.RegisterVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @anthor silenceYin
 * @date 2020/7/22 - 21:19
 */
@Controller
@Slf4j
public class LoginController {

    @Resource
    private ThirdPartFeignService thirdPartFeignService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    /**
     * 獲取短信驗證碼
     * @param phone 電話號碼
     * @return
     */
    @GetMapping("/sms/sendCode")
    @ResponseBody
    public R sendCode(@RequestParam("phone") String phone) {
        //1 TODO 接口防刷(因爲接口在前端暴露了 我猜可以用 sentinel限流？)

        //2 验证码校验
        String codeRedis = stringRedisTemplate.opsForValue().get(AuthServerConstant.CODE_SMS_CACHE_PREFIX + phone);
        if (StringUtils.isNotEmpty(codeRedis)) {
            String[] s = codeRedis.split("_");
            long saveTime = Long.parseLong(s[1]);
            long currentTime = System.currentTimeMillis();
            // 防止前端页面刷新后,未过60s再次发送请求
            if ((currentTime - saveTime) < 60000) {
                log.info("操作过于频繁,请稍后再试");
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_EXCEPTION.getMessage());
            }
        }
        // 防止前端页面刷新后,未过60s再次发送请求 (记录时间)
        String code = UUID.randomUUID().toString().substring(0,6) +"_" + System.currentTimeMillis();

        stringRedisTemplate.opsForValue().set(AuthServerConstant.CODE_SMS_CACHE_PREFIX + phone, code, 10, TimeUnit.MINUTES);
        log.info("验证码为-----------{}", code);
        R r = thirdPartFeignService.sendCode(phone, code);
        return r;

    }


    @PostMapping("/register")
    public String register(@Valid RegisterVo registerVo, BindingResult result, Model model) {
        if (result.hasErrors()) {
            List<FieldError> fieldErrors = result.getFieldErrors();
            Map<String, String> errorMap = fieldErrors.stream().collect(Collectors.toMap(e -> e.getField(), e -> e.getDefaultMessage(), (v1, v2) -> v2));

            model.addAttribute("errors", errorMap);
            // 校驗出錯 轉發會注冊
            return "reg";
        }


        return "redirect:/login.html";
    }



}
