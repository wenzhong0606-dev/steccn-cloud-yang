package com.steccn.authserver.exception;

import cn.hutool.core.util.StrUtil;
import com.steccn.common.vo.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Objects;

/**
 * @desc 全局异常类捕获
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.steccn")
public class SteccnExceptionControllerAdvice {

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.OK)
    public R bindException(BindException ex) {
        try {
            // 拿到@NotNull,@NotBlank和 @NotEmpty等注解上的message值
            String msg = Objects.requireNonNull(ex.getBindingResult().getFieldError()).getDefaultMessage();
            if (StrUtil.isNotEmpty(msg)) {
                // 自定义状态返回
                return R.failure(msg);
            }
        } catch (Exception ignored) {
        }
        // 参数类型不匹配检验
        StringBuilder msg = new StringBuilder();
        List<FieldError> fieldErrors = ex.getFieldErrors();
        fieldErrors.forEach((oe) ->
                msg.append("参数:[").append(oe.getObjectName())
                        .append(".").append(oe.getField())
                        .append("]的传入值:[").append(oe.getRejectedValue()).append("]与预期的字段类型不匹配.")
        );
        return R.failure(msg);
    }

    /**
     * 业务异常捕获
     *
     * @param businessException
     * @return
     */
    @ExceptionHandler(value = BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public R handleBusinessException(BusinessException businessException) {
        log.error("错误：", businessException);
        return R.failure(businessException.getLocalizedMessage());
    }

    /**
     * 图表异常捕获
     *
     * @param chartException 图表异常
     * @return
     */
    @ExceptionHandler(value = SteccnChartException.class)
    @ResponseStatus(HttpStatus.OK)
    public R handleSteccnChartException(SteccnChartException chartException) {
        log.error("错误：", chartException);
        R success = R.success();
        success.setMsg(chartException.getLocalizedMessage());
        return success;
    }

    /**
     * 异常捕获
     *
     * @param accessDeniedException
     * @return
     */
    @ExceptionHandler(value = AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public R handleAccessDeniedException(AccessDeniedException accessDeniedException) {
        log.error("错误：", accessDeniedException);
        return R.failure(accessDeniedException.getLocalizedMessage());
    }

    /**
     * 异常捕获
     *
     * @param runtimeException
     * @return
     */
    @ExceptionHandler(value = RuntimeException.class)
    @ResponseStatus(HttpStatus.OK)
    public R handleRuntimeException(RuntimeException runtimeException) {
        log.error("错误：", runtimeException);
        return R.failure(runtimeException.getLocalizedMessage());
    }

    /**
     * 异常捕获
     *
     * @param throwable
     * @return
     */
    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable throwable) {
        log.error("错误：", throwable);
        return R.failure(throwable.getLocalizedMessage());
    }
}
