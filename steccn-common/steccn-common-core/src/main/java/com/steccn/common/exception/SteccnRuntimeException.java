package com.steccn.common.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author yequan
 * @desc 上海凯盛节能工程技术有限公司
 * @date 2023/8/15
 */
@NoArgsConstructor
public class SteccnRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    @Getter
    @Setter
    private int code = 0;

    public SteccnRuntimeException(String message) {
        super(message);
    }

    public SteccnRuntimeException(int code, String message) {
        super(message);
        this.code = code;
    }

    public SteccnRuntimeException(Throwable cause) {
        super(cause);
    }

    public SteccnRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public SteccnRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
