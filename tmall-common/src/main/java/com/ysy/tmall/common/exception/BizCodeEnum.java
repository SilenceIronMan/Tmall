package com.ysy.tmall.common.exception;

/**
 * @anthor silenceYin
 * @date 2020/7/1 - 23:06
 */
public enum BizCodeEnum {
    UNKNOWN_EXCEPTION(10000, "系统未知异常"),
    VALID_EXCEPTION(10001, "参数格式校验失败"),
    SMS_CODE_EXCEPTION(10002, "短信验证码获取频率太高, 请稍后再试"),
    PRODUCT_UP_EXCEPTION(11000, "商品上架异常");
    private int code;
    private String message;

    BizCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
