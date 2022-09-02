package com.steccn.authserver.exception;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @desc 200 业务异常处理
 */
@NoArgsConstructor
public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    @Getter
    @Setter
    private int code = 0;

    public BusinessException(String message) {
        super(message);
    }
    public BusinessException(int code, String message){
        super(message);
        this.code = code;
    }

    public BusinessException(Throwable cause) {
        super(cause);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusinessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
