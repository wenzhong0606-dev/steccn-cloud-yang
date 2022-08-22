package com.steccn.common.constant;

import lombok.Getter;

@Getter
public enum BizCode implements StatusCode {

    APP_ERROR(2000, "业务异常"),

    ;

    BizCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    private int code;
    private String msg;

    @Override
    public int getCode() {
        return 0;
    }

    @Override
    public String getMsg() {
        return null;
    }
}
