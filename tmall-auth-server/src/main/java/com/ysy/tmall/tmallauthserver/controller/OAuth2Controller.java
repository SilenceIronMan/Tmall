package com.ysy.tmall.tmallauthserver.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.ysy.tmall.common.utils.HttpUtils;
import com.ysy.tmall.common.utils.R;
import com.ysy.tmall.common.vo.MemberResponseVO;
import com.ysy.tmall.tmallauthserver.constant.AuthServerConstant;
import com.ysy.tmall.tmallauthserver.feign.MemberFeignService;
import com.ysy.tmall.tmallauthserver.vo.SocialUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @anthor silenceYin
 * @date 2020/7/24 - 21:58
 */
@Controller
@Slf4j
public class OAuth2Controller {

    @Resource
    MemberFeignService memberFeignService;

    @GetMapping("/oauth2.0weibo/success")
    public String weibo(@RequestParam("code") String code, HttpSession session) throws Exception {
        Map<String,String> map=new HashMap<>();
        map.put("client_id","2360232986");
        map.put("client_secret","979a4c02ad9d19db32d83852839ce146");
        map.put("grant_type","authorization_code");
        map.put("redirect_uri","http://auth.ysymall.com/oauth2.0weibo/success");
        map.put("code",code);
        //根据code 换取accessToken
        HttpResponse response = HttpUtils.doPost("https://api.weibo.com",
                "/oauth2/access_token", "post",
                new HashMap<>(), map, new HashMap<>());


        if (response.getStatusLine().getStatusCode() == 200) {
            String json = EntityUtils.toString(response.getEntity());
            SocialUser socialUser = JSON.parseObject(json, SocialUser.class);
            // 获取用户的登录平台，然后判断用户是否该注册到系统中
            R r = memberFeignService.oauthLogin(socialUser);
            if (r.getCode() == 0) {

                MemberResponseVO loginUser = r.getData(new TypeReference<MemberResponseVO>() {});
                log.info("登陆成功,用户：{}",loginUser);
                // session 子域共享问题
                // TODO 1 .默认发的令牌.session = xxxxx . 作用域:当前域 ; (解决子域session共享问题)
                // TODO 2. 使用json的序列化方式来序列化对象数据到redis中(默认是JDK序列化 需要实现序列化 implements Serializable)
                // TODO 以上两点 详情见配置类
                session.setAttribute(AuthServerConstant.LOGIN_USER, loginUser);

                //成功 跳转首页
                return "redirect:http://glmall.com";
            } else {
                return "redirect:http://auth.glmall.com/login.html ";
            }
        }else {
            return "redirect:http://auth.glmall.com/login.html ";
        }
    }

}
