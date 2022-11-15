package com.steccn.qms.util;

import java.math.BigDecimal;
/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2022/11/11
 */
@FunctionalInterface
public interface ToBigDecimalFunction<T> {
    BigDecimal applyAsBigDecimal(T value);
}

