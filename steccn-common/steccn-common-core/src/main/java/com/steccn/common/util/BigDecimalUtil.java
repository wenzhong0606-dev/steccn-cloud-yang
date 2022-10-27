package com.steccn.common.util;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * @author yequan
 * @desc 精确数据类型
 */
public class BigDecimalUtil {

    /**
     * 格式化val 指定scale
     *
     * @param val
     * @param scale
     * @return
     */
    public static BigDecimal setScale(BigDecimal val, Integer scale) {
        if (Objects.isNull(val)) {
            return null;
        }
        BigDecimal res = val.setScale(scale, RoundingMode.HALF_UP);
        return res;
    }
}
