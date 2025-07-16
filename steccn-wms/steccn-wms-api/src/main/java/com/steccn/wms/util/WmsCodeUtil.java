package com.steccn.wms.util;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.steccn.common.dto.ParentTupleDTO;
import com.steccn.common.vo.EpTreeGraphDataVO;
import com.steccn.common.vo.EpTreeGraphLinkVO;
import com.steccn.common.vo.EpTreeGraphVO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
     * 箱号前缀
     */
    public final static String PREFIX_CASE_NO="PG";



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
     * 数字位数-12
     */
    public final static Integer NUM_BIT_10 = 10;

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

    /**
     * 获取erp批号
     * @param root
     * @return
     */
    public static String getErpFlotCode(String materialName,String workShopCode,String packNum) {
        if(StrUtil.isBlank(materialName) || StrUtil.isBlank(workShopCode) || StrUtil.isBlank(packNum)){
            return "0";
        }
        StringBuilder sb = new StringBuilder();
        if(materialName.contains("原片")){
            sb.append("2Y");
        }else {
            if(workShopCode.equals("STEC002")){
                sb.append("1");
            }else if(workShopCode.equals("STEC003")){
                sb.append("2");
            } else if (workShopCode.equals("STEC015")) {
                sb.append("3");
            }
            sb.append("-CT.");
            sb.append(packNum);
        }
        return sb.toString();
    }

    /**
     * 切割PG后的箱号
     * @param
     * @return
     */
    public static String extractBetweenPGAndComma(String input) {
        int pgIndex = input.indexOf("PG:");
        if (pgIndex == -1) return ""; // 没有PG:

        int start = pgIndex + 3; // "PG:" 长度是3
        int end = input.indexOf(",", start);
        if (end == -1) return input.substring(start); // 没有逗号，则取到结尾

        return input.substring(start, end);
    }

}
