package com.steccn.pms.constant;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.compress.utils.Sets;

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
}
