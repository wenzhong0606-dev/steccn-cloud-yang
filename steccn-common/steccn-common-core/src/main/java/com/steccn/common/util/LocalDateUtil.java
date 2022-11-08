package com.steccn.common.util;

import com.steccn.common.constant.LocalDateEnum;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author chenhuan
 * @date 2022/11/1 14:47
 */
public class LocalDateUtil {


    /**
     * 获取日期时间字符串
     *
     * @param localDateTime 需要转化的日期时间
     * @param pattern       时间格式
     * @return String 日期时间字符串，例如 2022-11-01 09:51:53
     */
    public static String format(LocalDateTime localDateTime, String pattern) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        return dateTimeFormatter.format(localDateTime);
    }

    /**
     * 获取日期时间字符串
     *
     * @param localDate 需要转化的日期
     * @param pattern   时间格式
     * @return String 日期时间字符串，例如 2022-11-01
     */
    public static String localDateFormat(LocalDate localDate, String pattern) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        return dateTimeFormatter.format(localDate);
    }

    /**
     * 获取指定日期开始时间的日期
     *
     * @param localDate 指定日期
     * @return LocalDateTime 格式：yyyy-MM-dd HH:mm:ss
     */
    public static LocalDateTime getStartTimeOfDayStr(LocalDate localDate) {
        return LocalDateTime.of(localDate, LocalTime.MIN);
    }

    /**
     * 获取指定日期结束时间的日期
     *
     * @return LocalDateTime 格式：yyyy-MM-dd 23:59:59
     */
    public static LocalDateTime getEndTimeOfDayStr(LocalDate localDate) {
        return LocalDateTime.of(localDate, LocalTime.MAX);
    }

    /**
     * 获取指定日期当周第一天的日期,这里第一天为周一
     *
     * @param localDate 指定日期时间
     * @return LocalDateTime 格式：yyyy-MM-dd HH:mm:ss
     */
    public static LocalDateTime getFirstDayOfWeekStr(LocalDate localDate) {
        return LocalDateTime.of(localDate.with(DayOfWeek.MONDAY), LocalTime.MIN);
    }

    /**
     * 获取指定日期当周最后一天的日期,这里最后一天为周日
     *
     * @param localDate 指定日期时间
     * @return localDateTime 格式：yyyy-MM-dd HH:mm:ss
     */
    public static LocalDateTime getLastDayOfWeekStr(LocalDate localDate) {
        return LocalDateTime.of(localDate.with(DayOfWeek.SUNDAY), LocalTime.MAX);

    }

    /**
     * 获取指定日期当月第一天的日期字
     *
     * @param localDate 指定日期时间
     * @return localDateTime 格式：yyyy-MM-dd HH:mm:ss
     */
    public static LocalDateTime getFirstDayOfMonthStr(LocalDate localDate) {
        return LocalDateTime.of(localDate.with(TemporalAdjusters.firstDayOfMonth()), LocalTime.MIN);
    }

    /**
     * 获取指定日期当月最后一天的日期
     *
     * @param localDate 指定日期时间
     * @return localDateTime 格式：yyyy-MM-dd
     */
    public static LocalDateTime getLastDayOfMonthStr(LocalDate localDate) {
        return LocalDateTime.of(localDate.with(TemporalAdjusters.lastDayOfMonth()), LocalTime.MAX);
    }

    /**
     * 获取指定日期当季度第一天的日期
     *
     * @param localDate 指定日期时间
     * @return localDateTime 格式：yyyy-MM-dd
     */
    public static LocalDateTime getFirstDayOfQuarterlyStr(LocalDate localDate) {
        int year = localDate.getYear();
        Month firstMonthOfQuarter = localDate.getMonth().firstMonthOfQuarter();
        localDate = LocalDate.of(year, firstMonthOfQuarter, 1);
        return LocalDateTime.of(localDate, LocalTime.MIN);
    }

    /**
     * 获取指定日期当季度最后一天的日期
     *
     * @param localDate 指定日期时间
     * @return localDateTime 格式：yyyy-MM-dd
     */
    public static LocalDateTime getLastDayOfQuarterlyStr(LocalDate localDate) {
        int year = localDate.getYear();
        Month firstMonthOfQuarter = localDate.getMonth().firstMonthOfQuarter();
        Month endMonthOfQuarter = Month.of(firstMonthOfQuarter.getValue() + 2);
        int day = endMonthOfQuarter.maxLength();
        localDate = LocalDate.of(year, endMonthOfQuarter, day);
        return LocalDateTime.of(localDate, LocalTime.MAX);
    }

    /**
     * 获取指定日期当年第一天的日期
     *
     * @param localDate 指定日期时间
     * @return LocalDateTime 格式：yyyy-MM-dd 00:00:00
     */
    public static LocalDateTime getFirstDayOfYearStr(LocalDate localDate) {
        return LocalDateTime.of(localDate.with(TemporalAdjusters.firstDayOfYear()), LocalTime.MIN);
    }

    /**
     * 获取指定日期当年最后一天的日期
     *
     * @param localDate 指定日期时间
     * @return LocalDateTime 格式：yyyy-MM-dd 23:59:59
     */
    public static LocalDateTime getLastDayOfYearStr(LocalDate localDate) {
        return LocalDateTime.of(localDate.with(TemporalAdjusters.lastDayOfYear()), LocalTime.MAX);
    }

    /**
     * 获取当前月所有日期
     *
     * @param localDate
     * @return
     */
    public static List<LocalDate> getCurrentMonthDateAll(LocalDate localDate) {
        int year = localDate.getYear();
        Month month = localDate.getMonth();
        int day = month.maxLength();
        List<LocalDate> localDateList = new ArrayList<>();
        for (int i = 1; i <= day; i++) {
            LocalDate date = LocalDate.of(year, month, i);
            localDateList.add(date);
        }
        return localDateList;
    }


    /**
     * 根据类型获取开始和结束时间
     *
     * @param type 类型 0：日，1：周，2：月，3：季度，4：年
     * @return List<LocalDateTime> 格式：[2022-11-01 00:00:00, 2022-11-01 23:59:59]
     */
    public static List<LocalDateTime> getFirstAndLastDate(int type) {
        List<LocalDateTime> firstAndLastDateList = new ArrayList<>();
        if (Objects.equals(type, LocalDateEnum.DAY.getCode())) {
            firstAndLastDateList.add(getStartTimeOfDayStr(LocalDate.now()));
            firstAndLastDateList.add(getEndTimeOfDayStr(LocalDate.now()));
        } else if (Objects.equals(type, LocalDateEnum.WEEK.getCode())) {
            firstAndLastDateList.add(getFirstDayOfWeekStr(LocalDate.now()));
            firstAndLastDateList.add(getLastDayOfWeekStr(LocalDate.now()));
        } else if (Objects.equals(type, LocalDateEnum.MONTH.getCode())) {
            firstAndLastDateList.add(getFirstDayOfMonthStr(LocalDate.now()));
            firstAndLastDateList.add(getLastDayOfMonthStr(LocalDate.now()));
        } else if (Objects.equals(type, LocalDateEnum.QUARTERLY.getCode())) {
            firstAndLastDateList.add(getFirstDayOfQuarterlyStr(LocalDate.now()));
            firstAndLastDateList.add(getLastDayOfQuarterlyStr(LocalDate.now()));
        } else {
            firstAndLastDateList.add(getFirstDayOfYearStr(LocalDate.now()));
            firstAndLastDateList.add(getLastDayOfYearStr(LocalDate.now()));
        }

        return firstAndLastDateList;
    }

}
