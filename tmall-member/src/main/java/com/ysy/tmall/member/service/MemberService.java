package com.ysy.tmall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ysy.tmall.common.utils.PageUtils;
import com.ysy.tmall.member.entity.MemberEntity;
import com.ysy.tmall.member.exception.PhoneExistExcetpion;
import com.ysy.tmall.member.exception.UserNameExistExcetpion;
import com.ysy.tmall.member.vo.MemberLoginVo;
import com.ysy.tmall.member.vo.MemberRegistVo;
import com.ysy.tmall.member.vo.SocialUser;

import java.util.Map;

/**
 * 会员
 *
 * @author SilenceIronMan
 * @email yinshiyu_2008@126.com
 * @date 2020-06-26 21:45:12
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void regist(MemberRegistVo vo);

    void checkPhoneUnique(String phone) throws PhoneExistExcetpion;

    void checkUserNameUnique(String userName) throws UserNameExistExcetpion;

    MemberEntity login(MemberLoginVo vo);

    MemberEntity login(SocialUser socialUser)  throws Exception ;
}

