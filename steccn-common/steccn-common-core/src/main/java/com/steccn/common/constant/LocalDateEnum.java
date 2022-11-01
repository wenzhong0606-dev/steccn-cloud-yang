package com.steccn.common.constant;

/**
 * @author chenhuan
 * @date 2022/11/1 16:21
 */
public enum LocalDateEnum {

    /**
     * 日
     */
    DAY(0),
    /**
     * 周
     */
    WEEK(1),
    /**
     * 月
     */
    MONTH(2);

    private final int code;

    LocalDateEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }


}
