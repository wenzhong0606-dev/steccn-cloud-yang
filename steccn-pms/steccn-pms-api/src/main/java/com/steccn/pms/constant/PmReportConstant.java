package com.steccn.pms.constant;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.compress.utils.Sets;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author yequan
 * @desc 生产报表的常量接口
 */
public interface PmReportConstant {

    /**
     * 水电油气 bar标识定义
     */
    String ELECTRIC = "electric";
    String WATER = "water";
    String OIL = "oil";
    String GAS = "gas";
    String STR_BAR = "bar";

    /**
     * type类型定义
     */
    Integer TYPE_0 = 0;
    Integer TYPE_1 = 1;
    Integer TYPE_2 = 2;
    Integer TYPE_3 = 3;
    Integer TYPE_4 = 4;

    String date_01 = "01";

    /**
     * 早班 晚班
     */
    String SYS_SHIFT_0 = "0";
    String SYS_SHIFT_1 = "1";

    /**
     * 时间范围 本日，本周，本月
     */
    @Getter
    @AllArgsConstructor
    enum DateRangeTypeEnum {
        TODAY(0, "本日"), THIS_WEEK(1, "本周"), THIS_MONTH(2, "本月");

        private final Integer code;

        private final String name;

        /**
         * 所有的类型
         *
         * @return
         */
        public static Set<Integer> all() {
            Set<Integer> all = Sets.newHashSet();
            for (DateRangeTypeEnum value : DateRangeTypeEnum.values()) {
                all.add(value.getCode());
            }
            return all;
        }
    }

    /**
     * 数据统计维度 班组，产线
     */
    @Getter
    @AllArgsConstructor
    enum DataGaugeTypeEnum {
        CLASS_GROUP(0, "班组"), PRODUCT_LINE(1, "产线");

        private final Integer code;

        private final String name;

        /**
         * 所有的类型
         *
         * @return
         */
        public static Set<Integer> all() {
            Set<Integer> all = Sets.newHashSet();
            for (DataGaugeTypeEnum value : DataGaugeTypeEnum.values()) {
                all.add(value.getCode());
            }
            return all;
        }
    }

    /**
     * 0耗电量 1耗水量 2耗油量 3耗气量
     */
    @Getter
    @AllArgsConstructor
    enum ReportWorkConsumeEnum {
        ELECTRIC(0, "耗电量(Kwh)"),
        WATER(1, "耗水量(T)"),
        OIL(2, "耗油量(Kg)"),
        GAS(3, "耗气量(m³)");

        private final Integer code;
        private final String name;
    }

    @Getter
    @AllArgsConstructor
    enum SubstrateReportConsumeEnum {
        PULL_FORCE(0, "拉引量(T)"),
        PASS_RATE(1, "产量合格率(%)"),
        IN_PASS_RATE(7, "入库合格率(%)"),
        ELECTRIC(2, "耗电量(Kwh)"),
        WATER(3, "耗水量(T)"),
        OIL(4, "耗油量(Kg)"),
        GAS(5, "耗气量(m³)"),
        Kcal(6, "单耗(kcal/Kg玻璃液)");

        private final Integer code;
        private final String name;
    }
}
