package com.steccn.common.util;


import com.google.common.collect.Maps;
import io.swagger.models.auth.In;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collector;

/**
 * @author yequan
 * @desc 精确数据类型
 */
public class BigDecimalUtil {

    /**
     * 精度
     */
    public static Integer DEFAULT_SCALE = 3;
    public static Integer SCALE_2 = 2;
    public static Integer SCALE_4 = 4;

    public static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    public static final BigDecimal THOUSAND = BigDecimal.valueOf(1000);

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
    public static BigDecimal add(BigDecimal val1, BigDecimal val2,int scale) {
        if (Objects.isNull(val1)) {
            val1 = BigDecimal.ZERO;
        }
        if (Objects.isNull(val2)) {
            val2 = BigDecimal.ZERO;
        }
        BigDecimal res = val1.add(val2);
        res = res.setScale(scale, RoundingMode.HALF_UP);
        return res;
    }

    /**
     * 将val1 + val2
     * @param val1
     * @param val2
     * @return 和
     */
    public static BigDecimal add(BigDecimal val1, BigDecimal val2){
        return add(val1, val2, DEFAULT_SCALE);
    }

    /**
     * 将val1 - val2
     *
     * @param val1 val1
     * @param val2 val2
     * @return 和
     */
    public static BigDecimal subtract(BigDecimal val1, BigDecimal val2) {
        return subtract(val1, val2, DEFAULT_SCALE);
    }

    /**
     * 将val1 - val2
     *
     * @param val1 val1
     * @param val2 val2
     * @return 和
     */
    public static BigDecimal subtract(BigDecimal val1, BigDecimal val2, int scale) {
        if (Objects.isNull(val1)) {
            val1 = BigDecimal.ZERO;
        }
        if (Objects.isNull(val2)) {
            val2 = BigDecimal.ZERO;
        }
        BigDecimal res = val1.subtract(val2);
        res = res.setScale(scale, RoundingMode.HALF_UP);
        return res;
    }


    /**
     * 除法val1 / val2保留scale位小数
     *
     * @param val1  值1
     * @param val2  值2
     * @param scale 保留几位小数
     * @return 数值，四舍五入模式下保留scale位小数
     */
    public static BigDecimal divide(BigDecimal val1, BigDecimal val2, int scale) {
        if (Objects.isNull(val1)) {
            val1 = BigDecimal.ZERO;
        }
        if (Objects.isNull(val2)) {
            val2 = BigDecimal.ZERO;
        }
        if (BigDecimal.ZERO.compareTo(val2) != 0) {
            return val1.divide(val2, scale, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    /**
     * 除法val1 / val2保留3位小数
     *
     * @param val1 值1
     * @param val2 值2
     * @return 数值，四舍五入模式下保留3位小数
     */
    public static BigDecimal divide(BigDecimal val1, BigDecimal val2) {
        return divide(val1, val2, DEFAULT_SCALE);
    }


    /**
     * Map<K1,BigDecimal>
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

    /**
     * Map<K1,Map<K2,BigDecimal>>
     * <p>
     * map同一个key数据累计
     *
     * @param map
     * @param k1
     * @param k2
     * @param value
     * @param <K1,K2>
     */
    public static <K1, K2> void biAccumulate(Map<K1, Map<K2, BigDecimal>> map, K1 k1, K2 k2, BigDecimal value) {
        //如果value为空，则默认为0
        if (Objects.isNull(value)) {
            value = BigDecimal.ZERO;
        }
        BigDecimal finalValue = value;
        map.compute(k1, (p, q) -> {
            if (Objects.isNull(q)) {
                q = Maps.newHashMap();
                q.put(k2, finalValue);
            } else {
                q.put(k2, add(q.get(k2), finalValue));
            }
            return q;
        });
    }

    /**
     * 格式化decimal数据
     *
     * @param value 数据
     * @return
     */
    public static String format(BigDecimal value) {
        if (Objects.isNull(value)) {
            value = BigDecimal.ZERO;
        }
        return value.setScale(DEFAULT_SCALE, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    /**
     * val1 x val2
     *
     * @param val1
     * @param val2
     * @return
     */
    public static BigDecimal multiply(BigDecimal val1, BigDecimal val2) {
        if (Objects.isNull(val1)) {
            val1 = BigDecimal.ZERO;
        }
        if (Objects.isNull(val2)) {
            val2 = BigDecimal.ZERO;
        }
        BigDecimal res = val1.multiply(val2);
        res = res.setScale(DEFAULT_SCALE, RoundingMode.HALF_UP);
        return res;
    }

    /**
     * 判断val1 == val2
     *
     * @param val1 val1
     * @param val2 val2
     * @return true or false
     */
    public static Boolean equals(BigDecimal val1, BigDecimal val2) {
        if (Objects.isNull(val1) && Objects.isNull(val2)) {
            return Boolean.TRUE;
        } else if (Objects.nonNull(val1) && Objects.nonNull(val2)) {
            return val1.compareTo(val2) == 0;
        }
        return Boolean.FALSE;
    }

}
