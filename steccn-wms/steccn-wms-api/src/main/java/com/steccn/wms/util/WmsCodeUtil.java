package com.steccn.wms.util;

import java.time.LocalDate;
import java.util.Objects;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/9/8
 */
public class WmsCodeUtil {

    /**
     * 短线
     */
    public final static String SHORT_LINE = "-";

    /**
     * 前缀
     */
    public final static String PREFIX_PMS_BATCH = "SP";

    /**
     * 调拨单前缀
     */
    public final static String PREFIX_WMS_TRANSFER_SLIP = "SM";


    /**
     * 入库单前缀
     */
    public final static String PREFIX_WMS_STORAGE_RECEIPT ="SI";


    /**
     * 出库单前缀
     */
    public final static String PREFIX_WMS_DELIVERY_RECEIPT ="SO";

    /**
     * 物流单前缀
     */
    public final static String PREFIX_WMS_LOGISTICS_ORDER = "DO";

    /**
     * 数字位数-4
     */
    public final static Integer NUM_BIT_4 = 4;

    /**
     * 数字位数-4
     */
    public final static Integer NUM_BIT_15 = 15;

    /**
     * 数字位数-12
     */
    public final static Integer NUM_BIT_12 = 12;
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
     * 盘点单编码生成
     * 规则：（SP-20220824-0001，时间取创建日期）
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
