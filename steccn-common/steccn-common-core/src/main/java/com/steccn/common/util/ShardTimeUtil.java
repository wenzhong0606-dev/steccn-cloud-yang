package com.steccn.common.util;

import cn.hutool.core.lang.Assert;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;

/**
 * @desc 时间分片类
 */
@Slf4j
public class ShardTimeUtil {

    /**
     * 切割时间段
     *
     * @param dateType 交易類型 Y/Q/M/W/D/H -->每年/每季度/每月/每周/每天/每小時
     * @param start    yyyy-MM-dd HH:mm:ss
     * @param end      yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static List<LocalDateTime> cutDateRange(String dateType, LocalDateTime start, LocalDateTime end) {
        List<LocalDateTime> listDate = new ArrayList<>();
        while (end.isAfter(start)) {

            switch (dateType) {
                case "Y":
                    listDate.add(LocalDateUtil.getFirstDayOfYearStr(start.toLocalDate()));
                    start = start.plusYears(1);
                    start = LocalDateTime.of(LocalDate.of(start.getYear(), 1, 1), LocalTime.MIN);
                    //永远加最后一个元素
                    if (!end.isAfter(start)) {
                        listDate.add(LocalDateUtil.getLastDayOfYearStr(end.toLocalDate()));
                    }
                    break;
                case "Q":
                    int currentMonth = start.getMonthValue();
                    if (currentMonth >= 1 && currentMonth <= 3) {
                        start = LocalDateTime.of(LocalDate.of(start.getYear(), 1, 1), start.toLocalTime());
                        listDate.add(start);
                        if (end.isAfter(start)) {
                            listDate.add(LocalDateTime.of(LocalDate.of(end.getYear(), 3, 31), end.toLocalTime()));
                        }
                        start = start.plusMonths(3);
                    } else if (currentMonth >= 4 && currentMonth <= 6) {
                        start = LocalDateTime.of(LocalDate.of(start.getYear(), 4, 1), start.toLocalTime());
                        listDate.add(start);
                        if (end.isAfter(start)) {
                            listDate.add(LocalDateTime.of(LocalDate.of(end.getYear(), 6, 30), end.toLocalTime()));
                        }
                        start = start.plusMonths(3);
                    } else if (currentMonth >= 7 && currentMonth <= 9) {
                        start = LocalDateTime.of(LocalDate.of(start.getYear(), 7, 1), start.toLocalTime());
                        listDate.add(start);
                        if (end.isAfter(start)) {
                            listDate.add(LocalDateTime.of(LocalDate.of(end.getYear(), 9, 30), end.toLocalTime()));
                        }
                        start = start.plusMonths(3);
                    } else if (currentMonth >= 10 && currentMonth <= 12) {
                        start = LocalDateTime.of(LocalDate.of(start.getYear(), 10, 1), start.toLocalTime());
                        listDate.add(start);
                        if (end.isAfter(start)) {
                            listDate.add(LocalDateTime.of(LocalDate.of(end.getYear(), 12, 31), end.toLocalTime()));
                        }
                        start = start.plusMonths(3);
                    }
                    break;
                case "M":
                    start = LocalDateTime.of(LocalDate.of(start.getYear(), start.getMonth(), 1), start.toLocalTime());
                    listDate.add(start);
                    start = start.plusMonths(1);
                    if (!end.isAfter(start)) {
                        LocalDateTime lastDayOfMonthStr = LocalDateUtil.getLastDayOfMonthStr(end.toLocalDate());
                        listDate.add(LocalDateTime.of(lastDayOfMonthStr.getYear(), lastDayOfMonthStr.getMonth(),
                                lastDayOfMonthStr.getDayOfMonth(), 23, 59, 59));
                    }
                    break;
                case "W":
                    listDate.add(LocalDateUtil.getFirstDayOfWeekStr(start.toLocalDate()));
                    start = start.plusWeeks(1);
                    start = start.with(DayOfWeek.MONDAY);
                    if (!end.isAfter(start)) {
                        LocalDateTime lastDayOfWeekStr = LocalDateUtil.getLastDayOfWeekStr(end.toLocalDate());
                        listDate.add(LocalDateTime.of(lastDayOfWeekStr.getYear(), lastDayOfWeekStr.getMonth(),
                                lastDayOfWeekStr.getDayOfMonth(), 23, 59, 59));
                    }
                    break;
                case "D":
                    listDate.add(start);
                    start = start.plusDays(1);
                    if (!end.isAfter(start)) {
                        LocalDateTime endTimeOfDayStr = LocalDateUtil.getEndTimeOfDayStr(end.toLocalDate());
                        listDate.add(LocalDateTime.of(endTimeOfDayStr.getYear(), endTimeOfDayStr.getMonth()
                                , endTimeOfDayStr.getDayOfMonth(), 23, 59, 59));
                    }
                    break;
                case "H":
                    listDate.add(start);
                    start = start.plusHours(1);
                    if (!end.isAfter(start)) {
                        LocalDateTime endTimeOfDayStr = LocalDateUtil.getEndTimeOfDayStr(end.toLocalDate());
                        listDate.add(LocalDateTime.of(endTimeOfDayStr.getYear(), endTimeOfDayStr.getMonth()
                                , endTimeOfDayStr.getDayOfMonth(), 23, 59, 59));
                    }
                    break;
            }

        }
        return listDate;
    }

    /**
     * 切割时间段
     *
     * @param dateType 交易類型 Y/Q/M/W/D/H -->每年/每季度/每月/每周/每天/每小時
     * @param start    yyyy-MM-dd HH:mm:ss
     * @param end      yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static List<LocalDateTime> cutDateRangeNew(String dateType, LocalDateTime start, LocalDateTime end) {
        List<LocalDateTime> listDate = new ArrayList<>();
        while (end.isAfter(start)) {

            switch (dateType) {
                case "Y":
                    listDate.add(LocalDateUtil.getFirstDayOfYearStr(start.toLocalDate()));
                    start = start.plusYears(1);
                    start = LocalDateTime.of(LocalDate.of(start.getYear(), 1, 1), LocalTime.MIN);
                    //永远加最后一个元素
                    if (!end.isAfter(start)) {
                        listDate.add(LocalDateUtil.getLastDayOfYearStr(end.toLocalDate()));
                    }
                    break;
                case "Q":
                    int currentMonth = start.getMonthValue();
                    if (currentMonth >= 1 && currentMonth <= 3) {
                        start = LocalDateTime.of(LocalDate.of(start.getYear(), 1, 1), start.toLocalTime());
                        listDate.add(start);
                        start = start.plusMonths(3);
                        //永远加最后一个元素
                        if (!end.isAfter(start)) {
                            listDate.add(LocalDateTime.of(LocalDate.of(end.getYear(), 4, 1), end.toLocalTime()));
                        }
                    } else if (currentMonth >= 4 && currentMonth <= 6) {
                        start = LocalDateTime.of(LocalDate.of(start.getYear(), 4, 1), start.toLocalTime());
                        listDate.add(start);
                        start = start.plusMonths(3);
                        //永远加最后一个元素
                        if (!end.isAfter(start)) {
                            listDate.add(LocalDateTime.of(LocalDate.of(end.getYear(), 7, 1), end.toLocalTime()));
                        }
                    } else if (currentMonth >= 7 && currentMonth <= 9) {
                        start = LocalDateTime.of(LocalDate.of(start.getYear(), 7, 1), start.toLocalTime());
                        listDate.add(start);
                        start = start.plusMonths(3);
                        //永远加最后一个元素
                        if (!end.isAfter(start)) {
                            listDate.add(LocalDateTime.of(LocalDate.of(end.getYear(), 10, 1), end.toLocalTime()));
                        }
                    } else if (currentMonth >= 10 && currentMonth <= 12) {
                        start = LocalDateTime.of(LocalDate.of(start.getYear(), 10, 1), start.toLocalTime());
                        listDate.add(start);
                        start = start.plusMonths(3);
                        //永远加最后一个元素
                        if (!end.isAfter(start)) {
                            listDate.add(LocalDateTime.of(LocalDate.of(end.getYear() + 1, 1, 1), end.toLocalTime()));
                        }
                    }
                    break;
                case "M":
                    start = LocalDateTime.of(LocalDate.of(start.getYear(), start.getMonth(), 1), start.toLocalTime());
                    listDate.add(start);
                    start = start.plusMonths(1);
                    if (!end.isAfter(start)) {
                        listDate.add(start);
                    }
                    break;
                case "W":
                    listDate.add(LocalDateUtil.getFirstDayOfWeekStr(start.toLocalDate()));
                    start = start.plusWeeks(1);
                    start = start.with(DayOfWeek.MONDAY);
                    if (!end.isAfter(start)) {
                        LocalDateTime lastDayOfWeekStr = LocalDateUtil.getLastDayOfWeekStr(end.toLocalDate());
                        listDate.add(LocalDateTime.of(lastDayOfWeekStr.getYear(), lastDayOfWeekStr.getMonth(),
                                lastDayOfWeekStr.getDayOfMonth(), 23, 59, 59));
                    }
                    break;
                case "D":
                    listDate.add(LocalDateUtil.getMorning(start));
                    start = start.plusDays(1);
                    if (!end.toLocalDate().isAfter(start.toLocalDate())) {
                        listDate.add(LocalDateUtil.getMorning(end.toLocalDate()));
                    }
                    break;
                case "C":
                    listDate.add(start);
                    start = start.plusHours(12);
                    if (!end.toLocalDate().isAfter(start.toLocalDate())) {
                        listDate.add(LocalDateUtil.getMorning(end.toLocalDate()));
                    }
                    break;
                case "H":
                    listDate.add(start);
                    start = start.plusHours(1);
                    if (!end.isAfter(start)) {
                        LocalDateTime endTimeOfDayStr = LocalDateUtil.getEndTimeOfDayStr(end.toLocalDate());
                        listDate.add(LocalDateTime.of(endTimeOfDayStr.getYear(), endTimeOfDayStr.getMonth()
                                , endTimeOfDayStr.getDayOfMonth(), 23, 59, 59));
                    }
                    break;
            }
        }
        return listDate;
    }

    /**
     * 切割日期段
     *
     * @param dateType 交易類型 Y/Q/M/W/D/H -->每年/每季度/每月/每周/每天/每小時
     * @param start    yyyy-MM-dd
     * @param end      yyyy-MM-dd
     * @return
     */
    public static List<LocalDate> cutDate(String dateType, LocalDate start, LocalDate end) {
        List<LocalDate> listDate = new ArrayList<>();
        //需要校验type
        boolean contains = Sets.newHashSet("Y", "Q", "M", "W", "D").contains(dateType);
        Assert.isTrue(contains, "粒度类型错误");
        while (end.isAfter(start)) {
            switch (dateType) {
                case "Y":
                    listDate.add(LocalDate.of(start.getYear(), 1, 1));
                    start = start.plusYears(1);
                    start = LocalDate.of(start.getYear(), 1, 1);
                    //永远加最后一个元素
                    if (!end.isAfter(start)) {
                        listDate.add(LocalDate.of(end.getYear() + 1, 1, 1));
                    }
                    break;
                case "Q":
                    int currentMonth = start.getMonthValue();
                    if (currentMonth >= 1 && currentMonth <= 3) {
                        start = LocalDate.of(start.getYear(), 1, 1);
                        listDate.add(start);
                        start = start.plusMonths(3);
                        //永远加最后一个元素
                        if (!end.isAfter(start)) {
                            listDate.add(LocalDate.of(end.getYear(), 4, 1));
                        }
                    } else if (currentMonth >= 4 && currentMonth <= 6) {
                        start = LocalDate.of(start.getYear(), 4, 1);
                        listDate.add(start);
                        start = start.plusMonths(3);
                        //永远加最后一个元素
                        if (!end.isAfter(start)) {
                            listDate.add(LocalDate.of(end.getYear(), 7, 1));
                        }
                    } else if (currentMonth >= 7 && currentMonth <= 9) {
                        start = LocalDate.of(start.getYear(), 7, 1);
                        listDate.add(start);
                        start = start.plusMonths(3);
                        //永远加最后一个元素
                        if (!end.isAfter(start)) {
                            listDate.add(LocalDate.of(end.getYear(), 10, 1));
                        }
                    } else if (currentMonth >= 10 && currentMonth <= 12) {
                        start = LocalDate.of(start.getYear(), 10, 1);
                        listDate.add(start);
                        start = start.plusMonths(3);
                        //永远加最后一个元素
                        if (!end.isAfter(start)) {
                            listDate.add(LocalDate.of(end.getYear() + 1, 1, 1));
                        }
                    }
                    break;
                case "M":
                    start = LocalDate.of(start.getYear(), start.getMonth(), 1);
                    listDate.add(start);
                    start = start.plusMonths(1);
                    //
                    if (!end.isAfter(start)) {
                        LocalDate over = end.plusMonths(1);
                        listDate.add(LocalDate.of(over.getYear(), over.getMonth(), 1));
                    }
                    break;
                case "W":
                    listDate.add(LocalDateUtil.getFirstDayOfWeekStr(start).toLocalDate());
                    start = start.plusWeeks(1);
                    start = start.with(DayOfWeek.MONDAY);
                    if (!end.isAfter(start)) {
                        listDate.add(LocalDateUtil.getLastDayOfWeekStr(end).toLocalDate());
                    }
                    break;
                case "D":
                    listDate.add(start);
                    start = start.plusDays(1);
                    if (!end.isAfter(start)) {
                        listDate.add(end);
                    }
                    break;
                default:
                    break;
            }
        }
        return listDate;
    }


    /**
     * 根据开始时间和结束时间分片
     *
     * @param start           开始时间
     * @param end             结束时间
     * @param intervalSeconds 步长 ，单位秒
     * @return 分片
     */
    public static List<LocalDateTime> shard(LocalDateTime start, LocalDateTime end, int intervalSeconds) {
        List<LocalDateTime> resList = Lists.newArrayList();
        while (start.isBefore(end) || start.isEqual(end)) {
            LocalDateTime resStart = LocalDateTime.from(start);
            start = start.plusSeconds(intervalSeconds);
            resList.add(resStart);
        }
        return resList;
    }

    /**
     * 自然周转日期 一年的第一周必须是完整周 ，周一开始
     * 等同于sql中的函数 select YEARWEEK('2019-01-01',7)
     *
     * @param year 年份
     * @param week 周数
     * @return
     */
    public static LocalDate[] week2Date(Integer year, Integer week) {
        LocalDate currentYear = LocalDate.of(year, 1, 1);
        WeekFields weekFields = WeekFields.of(DayOfWeek.MONDAY, 7);
        LocalDate with = currentYear.with(weekFields.weekOfYear(), week);
        LocalDate with1 = with.with(weekFields.dayOfWeek(), 1);
        LocalDate with2 = with.with(weekFields.dayOfWeek(), 7);
        return new LocalDate[]{with1, with2};
    }

    /**
     * 日期转年+周
     *
     * @param date 日期
     * @return
     */
    public static String date2Week(LocalDate date) {
        WeekFields weekFields = WeekFields.of(DayOfWeek.MONDAY, 7);
        int week = date.get(weekFields.weekOfYear());
        int year = date.getYear();
        if (date.getMonthValue() == 1 && week == 0) {
            return date2Week(LocalDate.of(year - 1, 12, 31));
        }
        String weekStr = week < 10 ? "0" + week : week + "";
        return year + weekStr;
    }

}
