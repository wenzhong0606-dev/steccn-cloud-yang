package com.steccn.common.constant;


/**
 * 类说明
 *
 * @author Jinglihui
 * @date 2022/7/7
 */
public class Constant {
    public static final String ISSUER = "www.steccn";

    //TOKEN有效期：10分钟
    public static final long TOKEN_EXPIRATION_TIME = 1000 * 60 * 10;

    public static final Integer SUCCESS = 1;
    public static final Integer FAILURE = 0;

    public static final Integer VALIDATE_ERROR = 1002;

    public static final Integer RESPONSE_PACK_ERROR = 1003;

    public static final Integer RUNTIME_EXCEPTION = 1004;

    public static final Integer AUTHENTICATION_FAILURE = 1005;

    /**
     * OA成功的code
     */
    public static final Integer OA_SUCCESS_CODE = 0;



}
