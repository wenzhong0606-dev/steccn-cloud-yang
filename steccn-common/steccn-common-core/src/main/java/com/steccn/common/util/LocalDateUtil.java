package com.steccn.common.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import com.steccn.common.constant.LocalDateEnum;
import com.steccn.common.dto.TupleDTO;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author chenhuan
 * @date 2022/11/1 14:47
 */
@Data
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
     * LocalDateTime转为日期
     *
     * @param localDateTime LocalDateTime
     * @return 日期
     */
    public static Date localDateTimeToDate(final LocalDateTime localDateTime) {
        if (null == localDateTime) {
            return null;
        }
        final ZoneId zoneId = ZoneId.systemDefault();
        final ZonedDateTime zdt = localDateTime.atZone(zoneId);
        final Date date = Date.from(zdt.toInstant());
        return date;
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
        int day = month.length(localDate.isLeapYear());
        List<LocalDate> localDateList = new ArrayList<>();
        for (int i = 1; i <= day; i++) {
            LocalDate date = LocalDate.of(year, month, i);
            localDateList.add(date);
        }
        return localDateList;
    }

    /**
     * 根据开始结束日期获取所有天数
     *
     * @return
     */
    public static List<LocalDate> getLocalDateByStartEndDate(LocalDate startDate, LocalDate endDate) {

        List<LocalDate> localDateList = new ArrayList<>();
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            localDateList.add(d);
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

    public static String getWeekDay() {
        LocalDate localDate = LocalDate.now();
        String k = String.valueOf(localDate.getDayOfWeek());
        return k;
    }

    public static Boolean checkLocalDate(String date) {
        String rexp = "^((\\d{2}(([02468][048])|([13579][26]))[-]?((((0?[13578])|(1[02]))[-]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[-]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[-]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[-]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[-]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[-]?((0?[1-9])|(1[0-9])|(2[0-8]))))))";

        Pattern pat = Pattern.compile(rexp);
        if (StringUtils.isNotBlank(date)) {
            Matcher mat = pat.matcher(date);
            return mat.matches();
        }

        return false;
    }

    /**
     * 校验年月 2023-01
     *
     * @param date
     * @return
     */
    public static Boolean checkYears(String date) {
        String pattern = "[1-2]\\d{3}-((0[1-9])|(1[0-2]))";
        Pattern pat = Pattern.compile(pattern);
        if (StringUtils.isNotBlank(date)) {
            Matcher mat = pat.matcher(date);
            return mat.matches();
        }

        return false;
    }

    /**
     * 晚班时间
     *
     * @param date
     * @return
     */
    public static LocalDateTime getNight(LocalDate date) {
        return date.atTime(20, 0, 0, 0);
    }

    /**
     * 晚班时间
     *
     * @param date
     * @return
     */
    public static LocalDateTime getNight(LocalDateTime date) {
        return date.withHour(20).withMinute(0).withSecond(0).withNano(0);
    }

    /**
     * 早班时间
     *
     * @param date
     * @return
     */
    public static LocalDateTime getMorning(LocalDate date) {
        return date.atTime(8, 0, 0, 0);
    }

    /**
     * 早班时间
     *
     * @param date
     * @return
     */
    public static LocalDateTime getMorning(LocalDateTime date) {
        return date.withHour(8).withMinute(0).withSecond(0).withNano(0);
    }

    /**
     * 判断time是否在start-end之前
     *
     * @param time    时间
     * @param start   开始时间
     * @param end     结束时间
     * @param leftEq  左包含
     * @param rightEq 右包含
     * @return
     */
    public static Boolean isRange(LocalDateTime time, LocalDateTime start, LocalDateTime end, boolean leftEq, boolean rightEq) {
        Assert.notNull(time, "时间不能为空");
        Assert.notNull(start, "开始时间不能为空");
        Assert.notNull(end, "结束时间不能为空");
        // start < time < end
        if (time.isAfter(start) && time.isBefore(end)) {
            return Boolean.TRUE;
        }
        //左包含 start == time
        if (leftEq && start.isEqual(time)) {
            return Boolean.TRUE;
        }
        //右包含 end == time
        if (rightEq) {
            return end.isEqual(time);
        }
        return Boolean.FALSE;
    }

    /**
     * 大于的判断
     *
     * @param left  左侧的数
     * @param right 右侧的数
     * @return 是否大于的结果
     */
    public static Boolean gt(LocalDateTime left, LocalDateTime right) {
        return greaterMeta(left, right, Boolean.FALSE);
    }

    /**
     * 大于的判断
     *
     * @param left  左侧的数
     * @param right 右侧的数
     * @return 是否大于的结果
     */
    public static Boolean gt(LocalDate left, LocalDate right) {
        return greaterMeta(left, right, Boolean.FALSE);
    }

    /**
     * 小于的判断
     *
     * @param left  左侧的数
     * @param right 右侧的数
     * @return 是否小于的结果
     */
    public static Boolean lt(LocalDateTime left, LocalDateTime right) {
        return greaterMeta(right, left, Boolean.FALSE);
    }

    /**
     * 小于的判断
     *
     * @param left  左侧的数
     * @param right 右侧的数
     * @return 是否小于的结果
     */
    public static Boolean lt(LocalDate left, LocalDate right) {
        return greaterMeta(right, left, Boolean.FALSE);
    }

    /**
     * 小于等于的判断
     *
     * @param left  左侧的数
     * @param right 右侧的数
     * @return 是否小于的结果
     */
    public static Boolean le(LocalDateTime left, LocalDateTime right) {
        return greaterMeta(right, left, Boolean.TRUE);
    }


    /**
     * 小于等于的判断
     *
     * @param left  左侧的数
     * @param right 右侧的数
     * @return 是否小于的结果
     */
    public static Boolean le(LocalDate left, LocalDate right) {
        return greaterMeta(right, left, Boolean.TRUE);
    }

    /**
     * 大于等于的判断
     *
     * @param left  左侧的数
     * @param right 右侧的数
     * @return 是否大于等于的结果
     */
    public static Boolean ge(LocalDateTime left, LocalDateTime right) {
        return greaterMeta(left, right, Boolean.TRUE);
    }


    /**
     * 大于等于的判断
     *
     * @param left  左侧的数
     * @param right 右侧的数
     * @return 是否大于等于的结果
     */
    public static Boolean ge(LocalDate left, LocalDate right) {
        return greaterMeta(left, right, Boolean.TRUE);
    }

    /**
     * 大于【等于】的判断
     *
     * @param left       左侧的数
     * @param right      右侧的数
     * @param equalsFlag 是否等于
     * @return 是否大于【等于】的结果
     */
    public static Boolean greaterMeta(LocalDateTime left, LocalDateTime right, boolean equalsFlag) {
        Assert.notNull(left, "时间不能为空");
        Assert.notNull(right, "时间不能为空");
        return left.isAfter(right) || (equalsFlag && left.isEqual(right));
    }


    /**
     * 大于【等于】的判断
     *
     * @param left       左侧的数
     * @param right      右侧的数
     * @param equalsFlag 是否等于
     * @return 是否大于【等于】的结果
     */
    public static Boolean greaterMeta(LocalDate left, LocalDate right, boolean equalsFlag) {
        Assert.notNull(left, "时间不能为空");
        Assert.notNull(right, "时间不能为空");
        return left.isAfter(right) || (equalsFlag && left.isEqual(right));
    }

    /**
     * 根据时间得到班次日期
     *
     * @param dateTime 时间
     * @param isPrev   临界点，是否算前一天
     * @return
     */
    public static LocalDate getClassDate(LocalDateTime dateTime, Boolean isPrev) {
        if (isRange(dateTime, LocalDateTime.of(dateTime.toLocalDate(), LocalTime.MIN), getMorning(dateTime), true, false)) {
            //在[0:00-8:00)之间 班次日期为T-1
            return dateTime.toLocalDate().minusDays(1);
        } else if (dateTime.isEqual(getMorning(dateTime)) && isPrev) {
            //正好8：00 isPrev == true  算前一天
            return dateTime.toLocalDate().minusDays(1);
        }
        //其余情况算当天
        return dateTime.toLocalDate();
    }

    /**
     * 获取当前时间的班次id
     *
     * @param dateTime 班次时间点
     * @param isPrev   是否是前一个班次，用于临界点判断
     * @return
     */
    public static Integer getClassId(LocalDateTime dateTime, Boolean isPrev) {
        // isPrev == true (8:00,20:00] 为早班
        // isPrev == false [8:00,20:00) 为早班
        if (isRange(dateTime, getMorning(dateTime), getNight(dateTime), !isPrev, isPrev)) {
            return ClassUtil.MORNING;
        }
        return ClassUtil.NIGHT;
    }

    /**
     * 算两段时间相差的小时数
     * @param start1
     * @param end1
     * @param start2
     * @param end2
     * @return
     */
    public static BigDecimal calculateIntersectionHours(
            LocalDateTime start1, LocalDateTime end1,
            LocalDateTime start2, LocalDateTime end2) {
        // 计算交集的开始时间和结束时间
        LocalDateTime intersectionStart = start1.isBefore(start2) ? start2 : start1;
        LocalDateTime intersectionEnd = end1.isBefore(end2) ? end1 : end2;

        // 计算交集的持续时间
        Duration intersectionDuration = Duration.between(intersectionStart, intersectionEnd);

        // 如果持续时间为负数，表示没有交集，返回0
        if (intersectionDuration.isNegative()) {
            return BigDecimal.ZERO.setScale(2);
        }

        // 计算交集的小时数，并保留2位小数
        BigDecimal intersectionHours = BigDecimal.valueOf(intersectionDuration.toMillis())
                .divide(BigDecimal.valueOf(3600000), 2, BigDecimal.ROUND_HALF_UP);

        // 计算交集的小时数
        return intersectionHours;
    }

    /**
     * 获取最大时间和最小时间
     * @param times
     * @return
     */
    public static TupleDTO<LocalDateTime,LocalDateTime> calculateTimeRange(Set<LocalDateTime> times){
        TupleDTO result = new TupleDTO<LocalDateTime,LocalDateTime>();
        if(CollUtil.isEmpty(times)){
            return null;
        }
        // 初始化最小和最大日期
        LocalDateTime minTime = null;
        LocalDateTime maxTime = null;
        for (LocalDateTime time : times) {
            if (minTime == null || time.isBefore(minTime)) {
                minTime = time;
            }
            if (maxTime == null || time.isAfter(maxTime)) {
                maxTime = time;
            }
        }
        result.setField1(minTime);
        result.setField2(maxTime);
        return result;
    }

}
