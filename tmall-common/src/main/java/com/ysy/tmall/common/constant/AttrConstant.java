package com.ysy.tmall.common.constant;

/**
 * @anthor silenceYin
 * @date 2020/7/12 - 21:13
 */
public class AttrConstant {
    public enum AttrSearchEnum {
        ATTR_SEARCH_UNNEED(0, "不需要"), ATTR_SEARCH_NEED(1, "需要");

        private int code;
        private String message;

        AttrSearchEnum(int code, String message) {
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

}
