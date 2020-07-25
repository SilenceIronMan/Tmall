package com.ysy.tmall.member.exception;

/**
 * @anthor silenceYin
 * @date 2020/7/24 - 0:22
 */
public class PhoneExistExcetpion extends RuntimeException{

    public PhoneExistExcetpion() {
        super("手机号已存在");
    }
}
