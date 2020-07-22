package com.ysy.tmall.tmallauthserver.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * 注冊信息
 * @anthor silenceYin
 * @date 2020/7/23 - 0:55
 */
@Data
public class RegisterVo {

    @NotEmpty(message = "用戶名必須提交")
    @Length(min = 6, max = 18, message = "用戶名必須是6-18位字符")
    private String userName;

    @NotEmpty(message = "密碼必須填寫")
    @Length(min = 6, max = 18, message = "密碼必須是6-18位字符")
    private String password;

    @NotEmpty(message = "手機號必須填寫")
    @Pattern(regexp ="^[1]([3-9])[0-9]{9}$", message = "手機號格式不正確")
    private String phone;

    @NotEmpty(message = "驗證碼必須填寫")
    private String code;
}
