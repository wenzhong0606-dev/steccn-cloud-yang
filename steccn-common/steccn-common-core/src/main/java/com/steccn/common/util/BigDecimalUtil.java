package com.steccn.common.util;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import io.swagger.models.auth.In;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @desc 精确数据类型
 */
public class BigDecimalUtil {

    /**
     * 精度
     */
    public static final Integer DEFAULT_SCALE = 3;
    public static final Integer SCALE_0 = 0;
    public static final Integer SCALE_2 = 2;
    public static final Integer SCALE_4 = 4;
    public static final Integer SCALE_6 = 6;
    public static final Integer SCALE_8 = 8;


    public static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    public static final BigDecimal THOUSAND = BigDecimal.valueOf(1000);
    public static final BigDecimal MINUS_ONE = new BigDecimal("-1");

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
    public static BigDecimal add(BigDecimal val1, BigDecimal val2, int scale) {
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
        BigDecimal res = val1.add(val2);
        res = res.setScale(scale, RoundingMode.HALF_UP);
        return res;
    }

    /**
     * 将val1 + val2
     *
     * @param val1
     * @param val2
     * @return 和
     */
    public static BigDecimal add(BigDecimal val1, BigDecimal val2) {
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
     * val1 > val2 ? true : false
     *
     * @param val1
     * @param val2
     * @return
     */
    public static boolean gt(BigDecimal val1, BigDecimal val2) {
        return greaterMeta(val1, val2, Boolean.FALSE);
    }

    /**
     * val1 >= val2 ? true : false
     *
     * @param val1
     * @param val2
     * @return
     */
    public static boolean ge(BigDecimal val1, BigDecimal val2) {
        return greaterMeta(val1, val2, Boolean.TRUE);
    }

    /**
     * val1 < val2 ? true : false
     *
     * @param val1
     * @param val2
     * @return
     */
    public static boolean lt(BigDecimal val1, BigDecimal val2) {
        return greaterMeta(val2, val1, Boolean.FALSE);
    }

    /**
     * val1 <= val2 ? true : false
     *
     * @param val1
     * @param val2
     * @return
     */
    public static boolean le(BigDecimal val1, BigDecimal val2) {
        return greaterMeta(val2, val1, Boolean.TRUE);
    }

    /**
     * 如果equalsFlag == false left > right
     * 如果equalsFlag == true  left >= right
     *
     * @param left
     * @param right
     * @param equalsFlag
     * @return
     */
    private static Boolean greaterMeta(BigDecimal left, BigDecimal right, boolean equalsFlag) {
        Assert.notNull(left, "值不能为空");
        Assert.notNull(right, "值不能为空");
        return left.compareTo(right) > 0 || (equalsFlag && left.compareTo(right) == 0);
    }

    /**
     * 将val1 - val2
     * null -  null = null
     * x    -  null = x
     * null -  y = -y
     * x    -  y = z
     *
     * @param val1 val1
     * @param val2 val2
     * @return 和
     */
    public static BigDecimal subtract(BigDecimal val1, BigDecimal val2, int scale) {
        //如果2个值为null，则返回null
        if (Objects.isNull(val1) && Objects.isNull(val2)) {
            return null;
        }
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
        if (Objects.isNull(val1) || Objects.isNull(val2)) {
            return null;
        }
        if (BigDecimal.ZERO.compareTo(val2) != 0) {
            return val1.divide(val2, scale, RoundingMode.HALF_UP);
        }
        //除数为0 返回空
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
        map.compute(key, (p, q) -> add(q, value));
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
        map.compute(k1, (p, q) -> {
            if (Objects.isNull(q)) {
                q = Maps.newHashMap();
                q.put(k2, value);
            } else {
                q.put(k2, add(q.get(k2), value));
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
            return null;
        }
        return value.setScale(DEFAULT_SCALE, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    /**
     * 格式化输出
     *
     * @param value 值
     * @param scale 位数
     * @return
     */
    public static String format(BigDecimal value, Integer scale) {
        if (Objects.isNull(value)) {
            return null;
        }
        return value.setScale(scale, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    /**
     * val1 x val2
     *
     * @param val1
     * @param val2
     * @return
     */
    public static BigDecimal multiply(BigDecimal val1, BigDecimal val2) {
        //乘法 有一个为null，则为null
        return multiply(val1, val2, DEFAULT_SCALE);
    }

    /**
     * val1 x val2
     *
     * @param val1  value1
     * @param val2  value2
     * @param scale 精度
     * @return
     */
    public static BigDecimal multiply(BigDecimal val1, BigDecimal val2, Integer scale) {
        //乘法 有一个为null，则为null
        if (Objects.isNull(val1) || Objects.isNull(val2)) {
            return null;
        }
        BigDecimal res = val1.multiply(val2);
        res = res.setScale(scale, RoundingMode.HALF_UP);
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

    /**
     * 求和
     *
     * @param valueList 值集合
     * @return
     */
    public static BigDecimal sum(Collection<BigDecimal> valueList) {
        //集合为空，则返回null
        if (CollUtil.isEmpty(valueList)) {
            return null;
        }
        //如果全为空值，则返回null
        List<BigDecimal> collect = valueList.stream().filter(v -> Objects.nonNull(v)).collect(Collectors.toList());
        if (CollUtil.isEmpty(collect)) {
            return null;
        }
        //求和，返回结果
        BigDecimal sumValue = collect.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sumValue;
    }

    /**
     * value1 / value2 * 100 %
     *
     * @param value1 值1
     * @param value2 值2
     * @param scale  保留几位小数
     * @return
     */
    public static BigDecimal percent(BigDecimal value1, BigDecimal value2, int scale) {
        if (Objects.isNull(value1) || Objects.isNull(value2) || equals(value2, BigDecimal.ZERO)) {
            return null;
        }
        BigDecimal percent = value1.divide(value2, scale + 2, RoundingMode.HALF_UP).multiply(HUNDRED);
        return percent.setScale(scale, RoundingMode.HALF_UP);
    }

    /**
     * value1 / value2 * 100 %
     *
     * @param value1 值1
     * @param value2 值2
     * @return
     */
    public static BigDecimal percent(BigDecimal value1, BigDecimal value2) {
        return percent(value1, value2, UnitLengthUtil.UNIT_PERCENT_LENGTH);
    }

    /**
     * 字符串转换成BigDecimal
     * @param value 值
     * @return
     */
    public static BigDecimal valueOf(String value) {
        if (StrUtil.isNotBlank(value)) {
            return new BigDecimal(value);
        }
        return null;
    }

    /**
     * 整数转换成BigDecimal
     * @param value 值
     * @return
     */
    public static BigDecimal valueOf(Integer value) {
        if (Objects.nonNull(value)) {
            return new BigDecimal(value);
        }
        return null;
    }

    /**
     * 百分比导出需要加百分号
     * @param percent
     * @return
     */
    public static String jointPercent(BigDecimal percent) {
        if (Objects.nonNull(percent)) {
            return format(percent, SCALE_2) + "%";
        }
        return null;
    }
}
