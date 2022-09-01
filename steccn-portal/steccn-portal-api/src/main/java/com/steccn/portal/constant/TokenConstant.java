package com.steccn.portal.constant;

import io.swagger.annotations.ApiModelProperty;

/**
 * @desc token的字符串常量
 */
public interface TokenConstant {


    String GRANT_TYPE_PASSWORD = "password";

    String CLIENT_ID = "admin";

    String SCOPE = "all";


    /**
     * oauth token正常返回的5个字段
     */
    String STR_ACCESS_TOKEN = "access_token";

    String STR_TOKEN_TYPE = "token_type";

    String STR_REFRESH_TOKEN = "refresh_token";

    String STR_EXPIRES_IN = "expires_in";

    String STR_SCOPE = "scope";


}
