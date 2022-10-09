package com.steccn.pms.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/9/8
 */
public class PmsCodeUtil {


    /**
     * 前缀-物料清单
     */
    public final static String PREFIX_B = "B-";

    /**
     * 前缀-工艺路线
     */
    public final static String PREFIX_R = "R-";
    /**
     * 数字位数
     */
    public final static Integer NUM_BIT_8 = 8;

    /**
     * 编码前缀-生产订单 PP
     */
    public final static String PREFIX_PMS_PRODUCE_ORDER = "PP";

    /**
     * 数字位数
     */
    public final static Integer NUM_BIT_4 = 4;

    public final static Integer DEFAULT_PADDING = 0 ;


    /**
     * @param prefix     前缀
     * @param numBit     数字位数【0填充】
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

    /**
     * 生产订单编码生成
     * 规则：（PP-20220824-0001，时间取创建日期）
     *
     * @param prefix     前缀
     * @param numBit     数字位数【0填充】
     * @param currentNum 当前数字
     * @return 下一个code
     */
    public static String getProduceOrderCode(String prefix, Integer numBit, Long currentNum) {
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
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        String s = format.format(date).replaceAll("-", "");
        return prefix + s + finalValStr;

    }


}
