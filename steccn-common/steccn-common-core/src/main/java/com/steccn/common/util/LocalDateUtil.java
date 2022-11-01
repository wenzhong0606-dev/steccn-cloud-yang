package com.steccn.common.util;

import com.steccn.common.constant.LocalDateEnum;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
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
     * 显示年月日时分秒，例如 2022-11-01 09:51:53
     */
    public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * 仅显示年月日，例如 2022-11-01
     */
    public static final String DATE_PATTERN = "yyyy-MM-dd";


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
     * 获取指定日期开始时间的日期字符串,带日期格式化参数
     *
     * @param localDateTime 指定日期时间
     * @return String 格式：yyyy-MM-dd HH:mm:ss
     */
    public static String getStartTimeOfDayStr(LocalDateTime localDateTime) {
        return format(localDateTime.withHour(0).withMinute(0).withSecond(0), DATETIME_PATTERN);
    }

    /**
     * 获取指定日期结束时间的日期字符串,带日期格式化参数
     *
     * @param localDateTime 指定日期时间
     * @return String 格式：yyyy-MM-dd 23:59:59
     */
    public static String getEndTimeOfDayStr(LocalDateTime localDateTime) {
        return format(localDateTime.withHour(23).withMinute(59).withSecond(59), DATETIME_PATTERN);
    }

    /**
     * 获取指定日期当周第一天的日期字符串,这里第一天为周一,带日期格式化参数
     *
     * @param localDateTime 指定日期时间
     * @param pattern       日期时间格式
     * @return String 格式：yyyy-MM-dd
     */
    public static String getFirstDayOfWeekStr(LocalDateTime localDateTime, String pattern) {
        return format(localDateTime.with(DayOfWeek.MONDAY).withHour(0).withMinute(0).withSecond(0), pattern);
    }

    /**
     * 获取指定日期当周最后一天的日期字符串,这里最后一天为周日,带日期格式化参数
     *
     * @param localDateTime 指定日期时间
     * @param pattern       日期时间格式
     * @return String 格式：yyyy-MM-dd
     */
    public static String getLastDayOfWeekStr(LocalDateTime localDateTime, String pattern) {
        return format(localDateTime.with(DayOfWeek.SUNDAY).withHour(23).withMinute(59).withSecond(59), pattern);
    }

    /**
     * 获取指定日期当月第一天的日期字符串,带日期格式化参数
     *
     * @param localDateTime 指定日期时间
     * @return String 格式：yyyy-MM-dd
     */
    public static String getFirstDayOfMonthStr(LocalDateTime localDateTime, String pattern) {
        return format(localDateTime.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0), pattern);
    }

    /**
     * 获取指定日期当月最后一天的日期字符串,带日期格式化参数
     *
     * @param localDateTime 指定日期时间
     * @param pattern       日期时间格式
     * @return String 格式：yyyy-MM-dd
     */
    public static String getLastDayOfMonthStr(LocalDateTime localDateTime, String pattern) {
        return format(localDateTime.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59), pattern);
    }

    /**
     * 获取指定日期当季度第一天的日期字符串,带日期格式化参数
     *
     * @param localDateTime 指定日期时间
     * @param pattern       日期时间格式
     * @return String 格式：yyyy-MM-dd
     */
    public static String getFirstDayOfQuarterlyStr(LocalDateTime localDateTime, String pattern) {
        Month firstMonthOfQuarter = LocalDate.now().getMonth().firstMonthOfQuarter();
        return format(LocalDateTime.of(LocalDate.now().getYear(), firstMonthOfQuarter, 1,0,0,0), pattern);
    }

    /**
     * 获取指定日期当季度最后一天的日期字符串,带日期格式化参数
     *
     * @param localDateTime 指定日期时间
     * @param pattern       日期时间格式
     * @return String 格式：yyyy-MM-dd
     */
    public static String getLastDayOfQuarterlyStr(LocalDateTime localDateTime, String pattern) {
        Month firstMonthOfQuarter = localDateTime.getMonth().firstMonthOfQuarter();
        Month endMonthOfQuarter = Month.of(firstMonthOfQuarter.getValue()+2);
        return format(LocalDateTime.of(LocalDate.now().getYear(), endMonthOfQuarter, endMonthOfQuarter.length(LocalDate.now().isLeapYear()),23,59,59), pattern);
    }

    /**
     * 获取指定日期当年第一天的日期字符串,带日期格式化参数
     *
     * @param localDateTime 指定日期时间
     * @param pattern       日期时间格式
     * @return String 格式：yyyy-MM-dd 00:00:00
     */
    public static String getFirstDayOfYearStr(LocalDateTime localDateTime, String pattern) {
        return format(localDateTime.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0), pattern);
    }

    /**
     * 获取指定日期当年最后一天的日期字符串,带日期格式化参数
     *
     * @param localDateTime 指定日期时间
     * @param pattern       日期时间格式
     * @return String 格式：yyyy-MM-dd 23:59:59
     */
    public static String getLastDayOfYearStr(LocalDateTime localDateTime, String pattern) {
        return format(localDateTime.with(TemporalAdjusters.lastDayOfYear()).withHour(23).withMinute(59).withSecond(59), pattern);
    }



    /**
     * 根据类型获取开始和结束时间
     *
     * @param type    类型 0：日，1：周，2：月
     * @param pattern 日期时间格式
     * @return List<String> 格式：[2022-11-01 00:00:00, 2022-11-01 23:59:59]
     */
    public static List<String> getFirstAndLastDate(int type, String pattern) {
        List<String> firstAndLastDateList = new ArrayList<>();
        if (Objects.equals(type, LocalDateEnum.DAY.getCode())) {
            firstAndLastDateList.add(getStartTimeOfDayStr(LocalDateTime.now()));
            firstAndLastDateList.add(getEndTimeOfDayStr(LocalDateTime.now()));
        } else if (Objects.equals(type, LocalDateEnum.WEEK.getCode())) {
            firstAndLastDateList.add(getFirstDayOfWeekStr(LocalDateTime.now(), pattern));
            firstAndLastDateList.add(getLastDayOfWeekStr(LocalDateTime.now(), pattern));
        } else if (Objects.equals(type, LocalDateEnum.MONTH.getCode())){
            firstAndLastDateList.add(getFirstDayOfMonthStr(LocalDateTime.now(), pattern));
            firstAndLastDateList.add(getLastDayOfMonthStr(LocalDateTime.now(), pattern));
        } else if (Objects.equals(type, LocalDateEnum.QUARTERLY.getCode())){
            firstAndLastDateList.add(getFirstDayOfQuarterlyStr(LocalDateTime.now(), pattern));
            firstAndLastDateList.add(getLastDayOfQuarterlyStr(LocalDateTime.now(), pattern));
        } else {
            firstAndLastDateList.add(getFirstDayOfYearStr(LocalDateTime.now(), pattern));
            firstAndLastDateList.add(getLastDayOfYearStr(LocalDateTime.now(), pattern));
        }

        return firstAndLastDateList;
    }

    public static void main(String[] args) {
        System.out.println(LocalDate.now());
        System.out.println(getStartTimeOfDayStr(LocalDateTime.now()));
        System.out.println(getEndTimeOfDayStr(LocalDateTime.now()));

        System.out.println(getFirstDayOfWeekStr(LocalDateTime.now(), DATE_PATTERN));
        System.out.println(getLastDayOfWeekStr(LocalDateTime.now(), DATE_PATTERN));

        System.out.println(getFirstDayOfMonthStr(LocalDateTime.now(), DATE_PATTERN));
        System.out.println(getLastDayOfMonthStr(LocalDateTime.now(), DATE_PATTERN));


        System.out.println(getFirstDayOfYearStr(LocalDateTime.now(),DATETIME_PATTERN));
        System.out.println(getLastDayOfYearStr(LocalDateTime.now(),DATETIME_PATTERN));

        System.out.println(getFirstAndLastDate(0, DATETIME_PATTERN));
        System.out.println(getFirstAndLastDate(1, DATETIME_PATTERN));
        System.out.println(getFirstAndLastDate(2, DATETIME_PATTERN));
        System.out.println(getFirstAndLastDate(3, DATETIME_PATTERN));
        System.out.println(getFirstAndLastDate(4, DATETIME_PATTERN));
    }
}
