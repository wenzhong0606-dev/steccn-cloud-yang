package com.steccn.common.util;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Objects;

/**
 * @author yequan
 * @desc 精确数据类型
 */
public class BigDecimalUtil {

    /**
     * 默认精度
     */
    public static Integer DEFAULT_SCALE = 3;

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

    /**
     * 将val1 和 val2 相加
     *
     * @param val1 val1
     * @param val2 val2
     * @return 和
     */
    public static BigDecimal add(BigDecimal val1, BigDecimal val2) {
        if (Objects.isNull(val1)) {
            val1 = BigDecimal.ZERO;
        }
        if (Objects.isNull(val2)) {
            val2 = BigDecimal.ZERO;
        }
        BigDecimal res = val1.add(val2);
        res = res.setScale(DEFAULT_SCALE, RoundingMode.HALF_UP);
        return res;
    }

    /**
     * 除法val1 / val2保留3位小数
     *
     * @param val1 值1
     * @param val2 值2
     * @return 数值，四舍五入模式下保留3位小数
     */
    public static BigDecimal divide(BigDecimal val1, BigDecimal val2) {
        if (Objects.isNull(val1)) {
            val1 = BigDecimal.ZERO;
        }
        if (Objects.isNull(val2)) {
            val2 = BigDecimal.ZERO;
        }
        if (BigDecimal.ZERO.compareTo(val2) != 0) {
            return val1.divide(val2, DEFAULT_SCALE, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }


    /**
     * map同一个key数据累计
     *
     * @param map
     * @param key
     * @param value
     * @param <K>
     */
    public static <K> void accumulate(Map<K, BigDecimal> map, K key, BigDecimal value) {
        //如果value为空，则默认为0
        if (Objects.isNull(value)) {
            value = BigDecimal.ZERO;
        }
        BigDecimal finalValue = value;
        map.compute(key, (p, q) -> {
            if (Objects.isNull(q)) {
                q = BigDecimal.ZERO;
            }
            q = q.add(finalValue);
            return q;
        });
    }

}
