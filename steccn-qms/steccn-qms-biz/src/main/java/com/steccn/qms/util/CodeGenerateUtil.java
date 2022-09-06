package com.steccn.qms.util;

import java.util.Objects;

public class CodeGenerateUtil {

    /**
     * 前缀-检验项
     */
    public final static String PREFIX_JYX = "JYX";

    /**
     * 前缀-缺陷项
     */
    public final static String PREFIX_QXX = "QXX";

    /**
     * 数字位数
     */
    public final static Integer num_bit_6 = 6;


    /**
     * @param prefix 前缀
     * @param numBit 数字位数【0填充】
     * @param currentNum 当前数字
     * @return 下一个code
     */
    public static String getNextCode(String prefix, Integer numBit, Long currentNum) {
        Long next = currentNum;
        if (Objects.isNull(currentNum)) {
            next = 0L;
        }
        next++;
        String finalValStr = String.valueOf(next);
        int length = finalValStr.length();
        while (length++ < numBit) {
            finalValStr = 0 + finalValStr;
        }
        return prefix + finalValStr;
    }
}
