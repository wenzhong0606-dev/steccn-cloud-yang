package com.steccn.common.constant;

/**
 * @author yequan
 * @desc 缓存字符串
 */
public interface CacheConstant {

    /**
     * 随机比例
     */
    Integer RANDOM_RATE_10 = 10;
    Integer PERCENT_100 = 100;

    /**
     * # , 用于配置自定义过期时间
     */
    String STR_POUND_SIGN = "#";

    /**
     * 过期时间5分钟 = 300s
     */
    String EXPIRE_5_MINUTES = STR_POUND_SIGN + 300;

    /**
     * 系统配置-客户端凭证
     */
    String SYS_CLIENT_ENCODE_PASSWORD = "sys_client_encode_password_v";

    /**
     * 数据字典
     */
    String SYS_DICT = "sys_dict_list";
    String  SYS_DICT_FILTER = "sys_dict_list_filter";

}
