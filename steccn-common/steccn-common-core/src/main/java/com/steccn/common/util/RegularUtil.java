package com.steccn.common.util;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yequan
 * @desc 正则表达式
 */
@Slf4j
public class RegularUtil {

    /**
     * 从字符串中依照正则抽取数值
     * 正则表达式异常，则抛异常
     * 转换异常，返回默认值
     *
     * @param rawStr       原始字符串
     * @param defaultValue 默认值
     * @param function     返回值函数式接口
     * @param <T>          泛型
     * @return 返回
     */
    public static <T> T extractValue(String rawStr, T defaultValue, Function<String, T> function) {
        T resValue = defaultValue;
        if (StrUtil.isNotBlank(rawStr)) {
            Matcher matcher;
            String trim = null;
            try {
                Pattern pattern = Pattern.compile("(\\d+\\.\\d+)|(\\d+)");
                matcher = pattern.matcher(rawStr);
                matcher.find();
            } catch (Exception e) {
                throw new RuntimeException("正则表达式错误");
            }
            try {
                trim = matcher.group();
                resValue = function.apply(trim);
            } catch (Exception e) {
                log.error("字符串抽取数值异常 -> {}", trim);
            }
        }
        return resValue;
    }
}
