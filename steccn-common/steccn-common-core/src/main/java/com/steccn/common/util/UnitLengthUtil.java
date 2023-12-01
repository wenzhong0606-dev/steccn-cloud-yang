package com.steccn.common.util;

/**
 * @author yequan
 * @desc 单位长度限制
 * @date 2023/9/7
 */
public interface UnitLengthUtil {

    /**
     * 吨保留一位小数
     */
    int UNIT_TON_LENGTH = 1;

    /**
     * 平方米保留整数
     */
    int UNIT_SQUARE_METER_LENGTH = 0;

    /**
     * 片保留整数
     */
    int UNIT_PIECE_LENGTH = 0;

    /**
     * 百分比保留2位小数
     */
    int UNIT_PERCENT_LENGTH = 2;

    /**
     * 立方米保留整数
     */
    int UNIT_CUBIC_METER_LENGTH = 0;

}
