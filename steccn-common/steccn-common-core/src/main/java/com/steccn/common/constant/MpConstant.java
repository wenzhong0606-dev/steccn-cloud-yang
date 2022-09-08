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
}
