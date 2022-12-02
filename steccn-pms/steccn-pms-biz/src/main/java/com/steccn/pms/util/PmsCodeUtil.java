package com.steccn.pms.util;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
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
     * 前缀-入库单
     */
    public final static String PREFIX_SI = "SI-";

    /**
     * 前缀-出库单
     */
    public final static String PREFIX_SO = "SO-";

    /**
     * 短线
     */
    public final static String SHORT_LINE = "-";
    /**
     * 数字位数
     */
    public final static Integer NUM_BIT_8 = 8;

    /**
     * 数字位数-3
     */
    public final static Integer NUM_BIT_3 = 3;
    /**
     * 数字位数-3
     */
    public final static Integer NUM_BIT_2 = 2;

    /**
     * 数字位数-1
     */
    public final static Integer NUM_BIT_1 = 1;

    /**
     * 编码前缀-领料单 ML
     */
    public final static String PREFIX_PMS_MATERIAL_REQUISITION = "ML";

    /**
     * 编码前缀-生产订单 PP
     */
    public final static String PREFIX_PMS_PRODUCE_ORDER = "PP";

    /**
     * 前缀
     */
    public final static String PREFIX_PMS_BATCH = "PT-";

    /**
     * 数字位数-4
     */
    public final static Integer NUM_BIT_4 = 4;
    /**
     * 数字位数-0
     */
    public final static Integer DEFAULT_PADDING = 0;


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
        String s = LocalDate.now().toString().replaceAll(SHORT_LINE, "");
        return prefix + SHORT_LINE + s + SHORT_LINE + finalValStr;
    }

    /**
     * 批次订单编码生成
     * 规则：(PT-20220829-01-01)
     * 01 班次号 01流水号
     *
     * @param prefix     前缀
     * @param numBit     数字位数【0填充】
     * @param currentNum 当前数字
     * @return 下一个code
     */
    public static String getBatchCode(String prefix, Integer numBit, Long currentNum, String classes) {
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
        String s = LocalDate.now().toString().replaceAll(SHORT_LINE, "");
        return prefix + s + SHORT_LINE + classes + SHORT_LINE + finalValStr;
    }

}
