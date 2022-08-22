package com.steccn.common.constant;

/**
 * @Description
 * @Author
 * @Date
 */
public interface SecurityConstants {
    /**
     * 刷新
     */
    String REFRESH_TOKEN = "refresh_token";
    /**
     * 验证码有效期
     */
    int CODE_TIME = 60;
    /**
     * 验证码长度
     */
    String CODE_SIZE = "4";
    /**
     * 角色前缀
     */
    String ROLE = "ROLE_";
    /**
     * 前缀
     */
    String JINGTAI_PREFIX = "steccn_";

    /**
     * oauth 缓存前缀
     */
    String OAUTH_ACCESS = "oauth:access:";

    /**
     * oauth 客户端信息
     */
    String CLIENT_DETAILS_KEY = "oauth:client:details";

    /**
     * oauth 相关前缀
     */
    String OAUTH_PREFIX = "oauth:";
    /**
     * 项目的license
     */
    String JINGTAI_LICENSE = "made by jingtai";

    /**
     * 内部
     */
    String FROM_IN = "Y";

    /**
     * 标志
     */
    String FROM = "from";

    /**
     * OAUTH URL
     */
    String OAUTH_TOKEN_URL = "/oauth/token";

    /**
     * 手机号登录URL
     */
    String SMS_TOKEN_URL = "/mobile/token/sms";

    /**
     * app短信登录URL
     */
    String SMS_APP_TOKEN_URL = "/mobile/token/app/sms";

    /**
     * 社交登录URL
     */
    String SOCIAL_TOKEN_URL = "/mobile/token/social";

    /**
     * w2登录URL
     */
    String W2_TOKEN_URL = "/mobile/token/w2";

    /**
     * 自定义登录URL
     */
    String MOBILE_TOKEN_URL = "/mobile/token/**";

    /**
     * 微信获取OPENID
     */
    String WX_AUTHORIZATION_CODE_URL = "https://api.weixin.qq.com/sns/oauth2/access_token" +
            "?appid=%s&secret=%s&code=%s&grant_type=authorization_code";

    /**
     * {bcrypt} 加密的特征码
     */
    String BCRYPT = "{bcrypt}";
    /**
     * sys_oauth_client_details 表的字段，不包括client_id、client_secret
     */
    String CLIENT_FIELDS = "client_id, CONCAT('{noop}',client_secret) as client_secret, resource_ids, scope, "
            + "authorized_grant_types, web_server_redirect_uri, authorities, access_token_validity, "
            + "refresh_token_validity, additional_information, autoapprove";

    /**
     * JdbcClientDetailsService 查询语句
     */
    String BASE_FIND_STATEMENT = "select " + CLIENT_FIELDS
            + " from sys_oauth_client_details";

    /**
     * 默认的查询语句
     */
    String DEFAULT_FIND_STATEMENT = BASE_FIND_STATEMENT + " order by client_id";

    /**
     * 按条件client_id 查询
     */
    String DEFAULT_SELECT_STATEMENT = BASE_FIND_STATEMENT + " where client_id = ?";

    /**
     * 资源服务器默认bean名称
     */
    String RESOURCE_SERVER_CONFIGURER = "resourceServerConfigurerAdapter";

    /**
     * 客户端模式
     */
    String CLIENT_CREDENTIALS = "client_credentials";

    /**
     * 用户ID字段
     */
    String DETAILS_USER_ID = "user_id";

    /**
     * 用户名字段
     */
    String DETAILS_USERNAME = "user_name";
    /**
     * uuid
     */
    String DETAILS_NAME = "name";

    /**
     * openid
     */
    String DETAILS_OPENID = "openid";
    /**
     * openid
     */
    String DETAILS_APPID = "appid";
    /**
     * phone
     */
    String DETAILS_PHONE = "phone";


    /**
     * 租户ID 字段
     */
    String DETAILS_TENANT_ID = "tenant_id";

    /**
     * 协议字段
     */
    String DETAILS_LICENSE = "license";

    /**
     *
     */
    String DEFAULT_GATEWAY_PREFIX = "";
    /**
     * 正式环境 active
     */
    String ACTIVE_PROD = "prd";
    /**
     * uat环境 uat
     */
    String ACTIVE_UAT = "uat";
}
