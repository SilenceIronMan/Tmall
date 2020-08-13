package com.ysy.tmall.member.exception;

/**
 * @anthor silenceYin
 * @date 2020/7/24 - 0:23
 */
public class UserNameExistExcetpion extends RuntimeException{
    public UserNameExistExcetpion() {
        super("用户名已存在");
    }
}
