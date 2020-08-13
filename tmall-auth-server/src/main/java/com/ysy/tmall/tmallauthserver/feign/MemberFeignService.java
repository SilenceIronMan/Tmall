package com.ysy.tmall.tmallauthserver.feign;

import com.ysy.tmall.common.utils.R;
import com.ysy.tmall.tmallauthserver.vo.RegisterVo;
import com.ysy.tmall.tmallauthserver.vo.SocialUser;
import com.ysy.tmall.tmallauthserver.vo.UserLoginVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @anthor silenceYin
 * @date 2020/7/23 - 22:23
 */
@FeignClient(value = "tmall-member", path = "/member/member")
public interface MemberFeignService {

    @PostMapping("/regist")
    R regist(@RequestBody RegisterVo vo);

    @PostMapping("/login")
    R login(@RequestBody UserLoginVo vo);

    @PostMapping("/oauth2/login")
    R oauthLogin(@RequestBody SocialUser socialUser) throws Exception;

}
