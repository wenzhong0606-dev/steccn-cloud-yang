package com.steccn.authserver.exception;

import com.steccn.common.vo.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @desc 全局异常类捕获
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.steccn")
public class SteccnExceptionControllerAdvice {

    /**
     * 异常捕获
     * @param throwable
     * @return
     */
    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable throwable) {
        log.error("错误：", throwable);
        return R.failure(throwable.getLocalizedMessage());
    }
}
