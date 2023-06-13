package com.steccn.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.*;
import java.time.temporal.TemporalAdjusters;

/**
 * @author yequan
 * @desc 日期选择器产量
 * @date 2023/6/13
 */
public interface DateSelectorConstant {


    @Getter
    @AllArgsConstructor
    enum DateSelectEnum {
        TODAY(1, "今天"),
        YESTERDAY(2, "昨天"),
        THIS_WEEK(3, "本周"),
        LAST_WEEK(4, "上周"),
        THIS_MONTH(5, "本月"),
        LAST_MONTH(6, "上月"),
        THIS_QUATER(7, "本季度"),
        LAST_QUATER(8, "上季度"),
        THIS_YEAR(9, "今年"),
        LAST_YEAR(10, "去年"),
        LAST_7_DAYS(11, "过去7天"),
        LAST_14_DAYS(12, "过去14天"),
        LAST_30_DAYS(13, "过去30天"),
        LAST_90_DAYS(14, "过去90天"),
        LAST_180_DAYS(15, "过去180天"),
        LAST_365_DAYS(16, "过去365天");

        private Integer code;
        private String name;

        /**
         * 基于当前日期获取动态时间范围-用于筛选数据
         * eg. current = 2023-06-13
         * [2023-06-13 00:00:00,2023-06-14 00:00:00) 今天
         * [2023-06-12 00:00:00,2023-06-13 00:00:00) 昨天
         * [2023-06-12 00:00:00,2023-06-19 00:00:00) 本周
         * [2023-06-05 00:00:00,2023-06-12 00:00:00) 上周
         * [2023-06-01 00:00:00,2023-07-01 00:00:00) 本月
         * [2023-05-01 00:00:00,2023-06-01 00:00:00) 上月
         * [2023-04-01 00:00:00,2023-07-01 00:00:00) 本季度
         * [2023-01-01 00:00:00,2023-04-01 00:00:00) 上季度
         * [2023-01-01 00:00:00,2024-01-01 00:00:00) 今年
         * [2022-01-01 00:00:00,2023-01-01 00:00:00) 去年
         * [2023-06-07 00:00:00,2023-06-14 00:00:00) 过去7天
         * [2023-05-31 00:00:00,2023-06-14 00:00:00) 过去14天
         * <p>
         *
         *
         * @param code    编码
         * @param current 当前日期
         * @return
         */
        public static LocalDateTime[] getDynamicDateTime(Integer code, LocalDate current) {
            LocalDate start = null;
            LocalDate end = null;
            switch (code) {
                case 1:
                    start = current;
                    end = start.plusDays(1);
                    break;
                case 2:
                    start = current.minusDays(1);
                    end = start.plusDays(1);
                    break;
                case 3:
                    start = current.with(DayOfWeek.MONDAY);
                    end = start.plusDays(7);
                    break;
                case 4:
                    current = current.minusDays(7);
                    start = current.with(DayOfWeek.MONDAY);
                    end = start.plusDays(7);
                    break;
                case 5:
                    start = current.with(TemporalAdjusters.firstDayOfMonth());
                    end = current.with(TemporalAdjusters.lastDayOfMonth()).plusDays(1);
                    break;
                case 6:
                    current = current.minusMonths(1);
                    start = current.with(TemporalAdjusters.firstDayOfMonth());
                    end = current.with(TemporalAdjusters.lastDayOfMonth()).plusDays(1);
                    break;
                case 7:
                    start = getQuaterStart(current);
                    end = start.plusMonths(2).with(TemporalAdjusters.lastDayOfMonth()).plusDays(1);
                    break;
                case 8:
                    current = current.minusMonths(3);
                    start = getQuaterStart(current);
                    end = start.plusMonths(2).with(TemporalAdjusters.lastDayOfMonth()).plusDays(1);
                    break;
                case 9:
                    start = current.with(TemporalAdjusters.firstDayOfYear());
                    end = current.with(TemporalAdjusters.lastDayOfYear()).plusDays(1);
                    break;
                case 10:
                    current = current.minusYears(1);
                    start = current.with(TemporalAdjusters.firstDayOfYear());
                    end = current.with(TemporalAdjusters.lastDayOfYear()).plusDays(1);
                    break;
                case 11:
                    start = current.minusDays(6);
                    end = current.plusDays(1);
                    break;
                case 12:
                    start = current.minusDays(13);
                    end = current.plusDays(1);
                    break;
                case 13:
                    start = current.minusDays(29);
                    end = current.plusDays(1);
                    break;
                case 14:
                    start = current.minusDays(89);
                    end = current.plusDays(1);
                    break;
                case 15:
                    start = current.minusDays(179);
                    end = current.plusDays(1);
                    break;
                case 16:
                    start = current.minusDays(364);
                    end = current.plusDays(1);
                    break;
            }
            return new LocalDateTime[]{LocalDateTime.of(start, LocalTime.MIN), LocalDateTime.of(end, LocalTime.MIN)};
        }

        /**
         * 获取当前日期所在季度的第一天
         *
         * @param current 当前日期
         * @return
         */
        private static LocalDate getQuaterStart(LocalDate current) {
            LocalDate start;
            int monthValue = current.getMonthValue();
            if (monthValue <= 3) {
                start = LocalDate.of(current.getYear(), Month.JANUARY, 1);
            } else if (monthValue <= 6) {
                start = LocalDate.of(current.getYear(), Month.APRIL, 1);
            } else if (monthValue < 9) {
                start = LocalDate.of(current.getYear(), Month.JULY, 1);
            } else {
                start = LocalDate.of(current.getYear(), Month.OCTOBER, 1);
            }
            return start;
        }
    }
}
