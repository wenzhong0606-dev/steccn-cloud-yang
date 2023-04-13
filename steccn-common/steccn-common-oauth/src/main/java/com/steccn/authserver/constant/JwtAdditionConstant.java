package com.steccn.authserver.constant;

/**
 * @desc jwt附加常量信息
 */
public interface JwtAdditionConstant {

    String JWT_ADDITION_USERID = "userId";

    String JWT_ADDITION_NAME = "name";

    /**
     * jwt的权限字符串
     */
    String JWT_ADDITION_PW_STR ="pwStr";

    /**
     * jwt自己包含的权限字符串，临时过度使用
     */
    String JWT_CONTAIN = "jwt_contain";
}
