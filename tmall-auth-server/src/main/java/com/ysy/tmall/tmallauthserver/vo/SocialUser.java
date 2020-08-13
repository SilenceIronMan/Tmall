package com.ysy.tmall.tmallauthserver.vo;

import lombok.Data;

/**
 * @anthor silenceYin
 * @date 2020/7/23 - 22:20
 */
@Data
public class SocialUser {

    private String access_token;

    private String remind_in;

    private long expires_in;

    private String uid;

    private String isRealName;
}
