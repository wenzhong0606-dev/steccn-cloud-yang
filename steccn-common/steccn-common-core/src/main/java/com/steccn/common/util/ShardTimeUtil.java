package com.steccn.common.util;

import cn.hutool.core.lang.Assert;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
        return cutDateRangeNew(dateType, start, end);
    }

    /**
     * 切割时间段
     *
     * eg. start-> 2023-12-10 08:00:00  end-> 2023-12-11 08:00:00 收官点为 end
     * H 2023-12-10 08:00:00,2023-12-10 09:00:00,2023-12-10 10:00:00 ... 2023-12-11 07:00:00,2023-12-11 08:00:00 共计24个时间区间
     * C 2023-12-10 08:00:00,2023-12-10 20:00:00,2023-12-11 08:00:00 显示为2023-12-10的班次日期
     * D 2023-12-10 08:00:00 2023-12-11 08:00:00
     * W 2023-12-04 08:00:00 2023-12-11 08:00:00
     * M 2023-12-01 08:00:00 2024-01-01 08:00:00
     * Q 2023-10-01 08:00:00 2024-01-01 08:00:00
     * Y 2023-01-01 08:00:00 2024-01-01 08:00:00
     *
     *
     * eg. start-> 2023-12-10 08:00:00  end-> 2023-12-10 20:00:00 显示1天的结果
     * H 2023-12-10 08:00:00,2023-12-10 09:00:00,2023-12-10 10:00:00 ... 2023-12-10 19:00:00,2023-12-10 20:00:00 共计12个时间区间
     * D 2023-12-10 08:00:00 2023-12-11 08:00:00
     * W 2023-12-04 08:00:00 2023-12-11 08:00:00
     * M 2023-12-01 08:00:00 2024-01-01 08:00:00
     * Q 2023-10-01 08:00:00 2024-01-01 08:00:00
     * Y 2023-01-01 08:00:00 2024-01-01 08:00:00
     *
     * @param dateType 交易類型 Y/Q/M/W/D/H -->每年/每季度/每月/每周/每天/每小時
     * @param start    yyyy-MM-dd HH:mm:ss
     * @param end      yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static List<LocalDateTime> cutDateRangeNew(String dateType, LocalDateTime start, LocalDateTime end) {
        //考虑是否是无粒度
        if ("N".equals(dateType)) {
            return Lists.newArrayList(start, end);
        }
        boolean contains = Sets.newHashSet("Y", "Q", "M", "W", "D", "C", "H").contains(dateType);
        Assert.isTrue(contains, "粒度类型错误");
        List<LocalDateTime> listDate = new ArrayList<>();
        while (LocalDateUtil.lt(start,end)) {
            switch (dateType) {
                case "Y":
                    start = LocalDateTime.of(LocalDateUtil.getFirstDayOfYearStr(start.toLocalDate()).toLocalDate(), start.toLocalTime());
                    listDate.add(start);
                    start = start.plusYears(1);
                    if (LocalDateUtil.le(end,start)) {
                        listDate.add(start);
                    }
                    break;
                case "Q":
                    int currentMonth = start.getMonthValue();
                    if (currentMonth >= 1 && currentMonth <= 3) {
                        start = LocalDateTime.of(LocalDate.of(start.getYear(), 1, 1), start.toLocalTime());
                    } else if (currentMonth >= 4 && currentMonth <= 6) {
                        start = LocalDateTime.of(LocalDate.of(start.getYear(), 4, 1), start.toLocalTime());
                    } else if (currentMonth >= 7 && currentMonth <= 9) {
                        start = LocalDateTime.of(LocalDate.of(start.getYear(), 7, 1), start.toLocalTime());
                    } else if (currentMonth >= 10 && currentMonth <= 12) {
                        start = LocalDateTime.of(LocalDate.of(start.getYear(), 10, 1), start.toLocalTime());
                    }
                    listDate.add(start);
                    start = start.plusMonths(3);
                    if (LocalDateUtil.le(end, start)) {
                        listDate.add(start);
                    }
                    break;
                case "M":
                    start = LocalDateTime.of(LocalDate.of(start.getYear(), start.getMonth(), 1), start.toLocalTime());
                    listDate.add(start);
                    start = start.plusMonths(1);
                    if (LocalDateUtil.le(end, start)) {
                        listDate.add(start);
                    }
                    break;
                case "W":
                    start = LocalDateTime.of(LocalDateUtil.getFirstDayOfWeekStr(start.toLocalDate()).toLocalDate(), start.toLocalTime());
                    listDate.add(start);
                    start = start.plusWeeks(1);
                    if (LocalDateUtil.le(end, start)) {
                        listDate.add(start);
                    }
                    break;
                case "D":
                    listDate.add(start);
                    start = start.plusDays(1);
                    if (LocalDateUtil.le(end, start)) {
                        listDate.add(start);
                    }
                    break;
                case "C":
                    listDate.add(start);
                    start = start.plusHours(12);
                    if (LocalDateUtil.le(end, start)) {
                        listDate.add(start);
                    }
                    break;
                case "H":
                    listDate.add(start);
                    start = start.plusHours(1);
                    if (LocalDateUtil.le(end, start)) {
                        listDate.add(start);
                    }
                    break;
            }
        }
        return listDate;
    }

    /**
     * 切割日期段
     * eg. start-> 2023-12-10  end-> 2023-12-11 显示2天的结果
     * D 2023-12-10 2023-12-11 2023-12-12
     * W 2023-12-04 2023-12-11 2023-12-17
     * M 2023-12-01 2024-01-01
     * Q 2023-10-01 2024-01-01
     * Y 2023-01-01 2024-01-01
     *
     *
     * eg. start-> 2023-12-10  end-> 2023-12-10 显示1天的结果
     * D 2023-12-10 2023-12-11
     * W 2023-12-04 2023-12-11
     * M 2023-12-01 2024-01-01
     * Q 2023-10-01 2024-01-01
     * Y 2023-01-01 2024-01-01
     *
     * @param dateType 交易類型 Y/Q/M/W/D/H -->每年/每季度/每月/每周/每天/每小時
     * @param start    yyyy-MM-dd
     * @param end      yyyy-MM-dd
     * @return
     */
    public static List<LocalDate> cutDate(String dateType, LocalDate start, LocalDate end) {
        //考虑是否是无粒度
        if ("N".equals(dateType)) {
            return Lists.newArrayList(start, end);
        }
        List<LocalDate> listDate = new ArrayList<>();
        //需要校验type
        boolean contains = Sets.newHashSet("Y", "Q", "M", "W", "D").contains(dateType);
        Assert.isTrue(contains, "粒度类型错误");
        while (LocalDateUtil.le(start,end)) {
            switch (dateType) {
                case "Y":
                    listDate.add(LocalDate.of(start.getYear(), 1, 1));
                    start = start.plusYears(1);
                    start = LocalDate.of(start.getYear(), 1, 1);
                    //永远加最后一个元素
                    if (LocalDateUtil.lt(end,start)) {
                        listDate.add(start);
                    }
                    break;
                case "Q":
                    int currentMonth = start.getMonthValue();
                    if (currentMonth >= 1 && currentMonth <= 3) {
                        start = LocalDate.of(start.getYear(), 1, 1);
                    } else if (currentMonth >= 4 && currentMonth <= 6) {
                        start = LocalDate.of(start.getYear(), 4, 1);
                    } else if (currentMonth >= 7 && currentMonth <= 9) {
                        start = LocalDate.of(start.getYear(), 7, 1);
                    } else {
                        start = LocalDate.of(start.getYear(), 10, 1);
                    }
                    listDate.add(start);
                    start = start.plusMonths(3);
                    //需要加上最后一个收边值 start ~ (end+1)
                    if (LocalDateUtil.lt(end,start)) {
                        listDate.add(start);
                    }
                    break;
                case "M":
                    start = LocalDate.of(start.getYear(), start.getMonth(), 1);
                    listDate.add(start);
                    start = start.plusMonths(1);
                    //需要加上最后一个收边值 start ~ (end+1)
                    if (LocalDateUtil.lt(end,start)) {
                        listDate.add(start);
                    }
                    break;
                case "W":
                    LocalDate startWeek = LocalDateUtil.getFirstDayOfWeekStr(start).toLocalDate();
                    listDate.add(startWeek);
                    start = startWeek.plusWeeks(1);
                    //需要加上最后一个收边值 start ~ (end+1)
                    if (LocalDateUtil.lt(end, start)) {
                        listDate.add(start);
                    }
                    break;
                case "D":
                    listDate.add(start);
                    start = start.plusDays(1);
                    //需要加上最后一个收边值 start ~ (end+1)
                    if (LocalDateUtil.lt(end, start)) {
                        listDate.add(start);
                    }
                    break;
                default:
                    break;
            }
        }
        return listDate;
    }
//
//    public static void main(String[] args) {
////    * eg. start-> 2023-12-10 08:00:00  end-> 2023-12-11 08:00:00 收官点为 end
////    * H 2023-12-10 08:00:00,2023-12-10 09:00:00,2023-12-10 10:00:00 ... 2023-12-11 07:00:00,2023-12-11 08:00:00 共计24个时间区间
////    * C 2023-12-10 08:00:00,2023-12-10 20:00:00,2023-12-11 08:00:00 显示为2023-12-10的班次日期
////    * D 2023-12-10 08:00:00 2023-12-11 08:00:00
////    * W 2023-12-04 08:00:00 2023-12-11 08:00:00
////    * M 2023-12-01 08:00:00 2024-01-01 08:00:00
////    * Q 2023-10-01 08:00:00 2024-01-01 08:00:00
////    * Y 2023-01-01 08:00:00 2024-01-01 08:00:00
//
//        List<LocalDateTime> dateList = cutDateRange("D", LocalDateTime.of(2023, 12, 8,7,0,0), LocalDateTime.of(2023, 12, 11,8,0,0));
////        System.out.print(dateList);
//        dateList.forEach(ele ->{
//            System.out.println(ele);
//
//        });
//    }


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
