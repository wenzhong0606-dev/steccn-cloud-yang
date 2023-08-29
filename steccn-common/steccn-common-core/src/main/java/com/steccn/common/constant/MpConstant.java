package com.steccn.common.constant;

/**
 * @desc mybatis-plus的常量
 */
public interface MpConstant {

    /**
     * 只取一条
     */
    String STR_LIMIT_1 = " limit 1 ";

    /**
     * 删除标识  正常为0
     */
    Integer DELETE_FLAG_NORMAL = 0;

    /**
     * 状态-停用
     */
    Integer STATUS_STOP = 0;

    /**
     * 状态-启用
     */
    Integer STATUS_START = 1;

    /**
     * 是否当前版本 1是 0否
     */
    Integer CURRENT_VERSION_YES = 1;

    /**
     * 是否当前版本 1是 0否
     */
    Integer CURRENT_VERSION_NOT = 0;

    /**
     * 是否
     */
    Integer YES = 1;
    Integer NO = 0;

    /**
     * app下载前缀
     */
    String DOWNLOAD_APP_PREFIX = "tongcheng.cnbmmom.com";

    /**
     * app下载源地址
     */
    String ORIGINAL_APP_PREFIX = "192.168.16.252";

    /**
     * 状态-启用
     */
    String STATUS_200 = "200";
}
