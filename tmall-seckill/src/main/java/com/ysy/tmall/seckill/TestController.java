package com.ysy.tmall.seckill;

import cn.hutool.http.HttpUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

/**
 * @anthor silenceYin
 * @date 2020/9/7 - 22:26
 */
@RestController
public class TestController {

    @RequestMapping("hello")
    public String hello(HttpServletRequest request) {
        System.out.println(request.getParameter("hello"));
        System.out.println(request.getParameter("world"));
        return null;
    }

    public static void main(String[] args) {
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("hello", "北京");
        paramMap.put("world", "北京2");

        String result= HttpUtil.post("http://localhost:8046/hello", paramMap);
    }
}
