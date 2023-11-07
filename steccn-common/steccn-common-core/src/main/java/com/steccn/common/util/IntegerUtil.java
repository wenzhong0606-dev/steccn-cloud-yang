package com.steccn.common.util;


import cn.hutool.core.collection.CollUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
        Integer res = val1 + val2;
        return res;
    }


    /**
     * 整数求和
     *
     * @param valueList
     * @return
     */
    public static Integer sum(List<Integer> valueList) {
        if (CollUtil.isEmpty(valueList)) {
            return null;
        }
        List<Integer> collect = valueList.stream().filter(ele -> Objects.nonNull(ele)).collect(Collectors.toList());
        if (CollUtil.isEmpty(collect)) {
            return null;
        }
        int sum = 0;
        for (Integer ele : collect) {
            sum += ele;
        }
        return sum;
    }

    /**
     * 格式化元素
     * @param ele 元素
     * @return
     */
    public static String format(Integer ele){
        if(Objects.nonNull(ele)){
            return ele.toString();
        }
        return null;
    }

}
