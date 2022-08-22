package com.steccn.common.constant;

/**
 * 类说明
 *
 * @author Jinglihui
 * @date 2022/7/15
 */
public enum ResultCode implements StatusCode {
    SUCCESS(Constant.SUCCESS, "访问成功"),
    FAILURE(Constant.FAILURE, "访问失败"),

    VALIDATE_ERROR(Constant.VALIDATE_ERROR, "参数校验错误"),

    RESPONSE_PACK_ERROR(Constant.RESPONSE_PACK_ERROR, "Response包装失败"),

    RUNTIME_EXCEPTION(Constant.RUNTIME_EXCEPTION, "运行时异常"),

    AUTHENTICATION_FAILURE(Constant.AUTHENTICATION_FAILURE, "认证失败");

    private int code;
    private String msg;

    @Override
    public int getCode() {
        return this.code;
    }

    @Override
    public String getMsg() {
        return this.msg;
    }

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
