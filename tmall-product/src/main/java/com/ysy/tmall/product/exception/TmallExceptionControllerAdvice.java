package com.ysy.tmall.product.exception;

import com.ysy.tmall.common.exception.BizCodeEnum;
import com.ysy.tmall.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;

/**
 * 全局异常处理类
 * @anthor silenceYin
 * @date 2020/7/1 - 22:44
 */
@RestControllerAdvice(basePackages = "com.ysy.tmall.product.controller")
@Slf4j
public class TmallExceptionControllerAdvice {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e){
        BindingResult result = e.getBindingResult();
        HashMap<String, String> errorMap = new HashMap<>();
        result.getFieldErrors().forEach(error -> {

            String field = error.getField();
            String message = error.getDefaultMessage();
            errorMap.put(field, message);
        });

        log.error("数据校验出现问题{}, 异常类型: {}", e.getMessage(), e.getClass());
        return R.error(BizCodeEnum.VALID_EXCEPTION.getCode(), BizCodeEnum.VALID_EXCEPTION.getMessage()).put("data", errorMap);
    }


    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable e){

        log.error("程序出现异常{}, 异常类型: {}", e.getMessage(), e.getClass());
        return R.error(BizCodeEnum.UNKNOWN_EXCEPTION.getCode(), BizCodeEnum.UNKNOWN_EXCEPTION.getMessage());
    }

}
