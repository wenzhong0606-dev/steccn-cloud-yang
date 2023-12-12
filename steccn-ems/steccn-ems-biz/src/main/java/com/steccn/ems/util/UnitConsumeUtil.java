package com.steccn.ems.util;

import com.steccn.common.util.BigDecimalUtil;

import java.math.BigDecimal;

/**
 * @author yequan
 * @desc 单耗计算公式
 * @date 2023/12/12
 */
public class UnitConsumeUtil {

    /**
     * 单耗的计算方式
     *
     * @param gas       气
     * @param oil       油
     * @param pullForce 拉引量
     * @param bit       保留小数
     * @return
     */
    public static BigDecimal unitConsume(BigDecimal gas, BigDecimal oil, BigDecimal pullForce, int bit) {
        BigDecimal gasValueMu = BigDecimalUtil.multiply(gas, new BigDecimal("7800"), bit);
        BigDecimal oilValueMu = BigDecimalUtil.multiply(oil, new BigDecimal("9300000"), bit);
        BigDecimal multiply = BigDecimalUtil.add(gasValueMu, oilValueMu, bit);
        BigDecimal pullForceValueMu = BigDecimalUtil.multiply(pullForce, new BigDecimal("1000"), bit);
        BigDecimal result = BigDecimalUtil.divide(multiply, pullForceValueMu, bit);
        return result;
    }
}
