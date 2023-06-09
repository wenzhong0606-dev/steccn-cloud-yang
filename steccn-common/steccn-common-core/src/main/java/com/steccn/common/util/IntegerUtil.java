package com.steccn.common.util;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Integer类型工具类
 */
public class IntegerUtil {

    /**
     * 将val1 和 val2 相加
     *
     * @param val1 val1
     * @param val2 val2
     * @return 和
     */
    public static Integer add(Integer val1, Integer val2) {
        //如果2个都为null则返回null
        if (Objects.isNull(val1) && Objects.isNull(val2)) {
            return null;
        }
        if (Objects.isNull(val1)) {
            return val2;
        }
        if (Objects.isNull(val2)) {
            return val1;
        }
        Integer res = val1+val2;
        return res;
    }
}
