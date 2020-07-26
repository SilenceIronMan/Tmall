package com.ysy.tmall.cart.to;

import lombok.Data;

/**
 * @anthor silenceYin
 * @date 2020/7/26 - 16:26
 */
@Data
public class UserInfoTo {
    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 临时用户
     */
    private String userKey;

    /**
     * 是否有user-key
     */
    private Boolean tempUser=false;
}
