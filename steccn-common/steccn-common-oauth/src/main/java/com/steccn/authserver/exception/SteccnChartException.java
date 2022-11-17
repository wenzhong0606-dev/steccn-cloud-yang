package com.steccn.authserver.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * @desc 图表异常
 * @author yequan
 */
public class SteccnChartException extends RuntimeException{
    private static final long serialVersionUID = 1L;
    @Getter
    @Setter
    private int code = 0;

    public SteccnChartException(String message) {
        super(message);
    }
    public SteccnChartException(int code, String message){
        super(message);
        this.code = code;
    }

    public SteccnChartException(Throwable cause) {
        super(cause);
    }

    public SteccnChartException(String message, Throwable cause) {
        super(message, cause);
    }

    public SteccnChartException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
