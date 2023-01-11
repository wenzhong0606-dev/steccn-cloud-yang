package com.steccn.qms.util;

import com.steccn.common.util.LocalDateUtil;
import lombok.extern.slf4j.Slf4j;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
                            listDate.add(LocalDateTime.of(LocalDate.of(end.getYear(),3,31),end.toLocalTime()));
                        }
                        start = start.plusMonths(3);
                    } else if (currentMonth >= 4 && currentMonth <= 6) {
                        start = LocalDateTime.of(LocalDate.of(start.getYear(), 4, 1), start.toLocalTime());
                        listDate.add(start);
                        if (end.isAfter(start)) {
                            listDate.add(LocalDateTime.of(LocalDate.of(end.getYear(),6,30),end.toLocalTime()));
                        }
                        start = start.plusMonths(3);
                    } else if (currentMonth >= 7 && currentMonth <= 9) {
                        start = LocalDateTime.of(LocalDate.of(start.getYear(), 7, 1), start.toLocalTime());
                        listDate.add(start);
                        if (end.isAfter(start)) {
                            listDate.add(LocalDateTime.of(LocalDate.of(end.getYear(),9,30),end.toLocalTime()));
                        }
                        start = start.plusMonths(3);
                    } else if (currentMonth >= 10 && currentMonth <= 12) {
                        start = LocalDateTime.of(LocalDate.of(start.getYear(), 10, 1), start.toLocalTime());
                        listDate.add(start);
                        if (end.isAfter(start)) {
                            listDate.add(LocalDateTime.of(LocalDate.of(end.getYear(),12,31),end.toLocalTime()));
                        }
                        start = start.plusMonths(3);
                    }
                    break;
                case "M":
                    start = LocalDateTime.of(LocalDate.of(start.getYear(), start.getMonth(), 1), start.toLocalTime());
                    listDate.add(start);
                    start = start.plusMonths(1);
                    if (!end.isAfter(start)){
                        LocalDateTime lastDayOfMonthStr = LocalDateUtil.getLastDayOfMonthStr(end.toLocalDate());
                        listDate.add(LocalDateTime.of(lastDayOfMonthStr.getYear(),lastDayOfMonthStr.getMonth(),
                                lastDayOfMonthStr.getDayOfMonth(),23,59,59));
                    }
                    break;
                case "W":
                    listDate.add(LocalDateUtil.getFirstDayOfWeekStr(start.toLocalDate()));
                    start = start.plusWeeks(1);
                    start = start.with(DayOfWeek.MONDAY);
                    if (!end.isAfter(start)){
                        LocalDateTime lastDayOfWeekStr = LocalDateUtil.getLastDayOfWeekStr(end.toLocalDate());
                        listDate.add(LocalDateTime.of(lastDayOfWeekStr.getYear(),lastDayOfWeekStr.getMonth(),
                                lastDayOfWeekStr.getDayOfMonth(),23,59,59));
                    }
                    break;
                case "D":
                    listDate.add(start);
                    start = start.plusDays(1);
                    if (!end.isAfter(start)){
                        LocalDateTime endTimeOfDayStr = LocalDateUtil.getEndTimeOfDayStr(end.toLocalDate());
                        listDate.add(LocalDateTime.of(endTimeOfDayStr.getYear(),endTimeOfDayStr.getMonth()
                                ,endTimeOfDayStr.getDayOfMonth(), 23,59,59));
                    }
                    break;
                case "H":
                    listDate.add(start);
                    start = start.plusHours(1);
                    if (!end.isAfter(start)){
                        LocalDateTime endTimeOfDayStr = LocalDateUtil.getEndTimeOfDayStr(end.toLocalDate());
                        listDate.add(LocalDateTime.of(endTimeOfDayStr.getYear(),endTimeOfDayStr.getMonth()
                                ,endTimeOfDayStr.getDayOfMonth(), 23,59,59));
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
                            listDate.add(LocalDateTime.of(LocalDate.of(end.getYear(),4,1),end.toLocalTime()));
                        }
                    } else if (currentMonth >= 4 && currentMonth <= 6) {
                        start = LocalDateTime.of(LocalDate.of(start.getYear(), 4, 1), start.toLocalTime());
                        listDate.add(start);
                        start = start.plusMonths(3);
                        //永远加最后一个元素
                        if (!end.isAfter(start)) {
                            listDate.add(LocalDateTime.of(LocalDate.of(end.getYear(),7,1),end.toLocalTime()));
                        }
                    } else if (currentMonth >= 7 && currentMonth <= 9) {
                        start = LocalDateTime.of(LocalDate.of(start.getYear(), 7, 1), start.toLocalTime());
                        listDate.add(start);
                        start = start.plusMonths(3);
                        //永远加最后一个元素
                        if (!end.isAfter(start)) {
                            listDate.add(LocalDateTime.of(LocalDate.of(end.getYear(),10,1),end.toLocalTime()));
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
                    if (!end.isAfter(start)){
                        LocalDateTime lastDayOfMonthStr = LocalDateUtil.getLastDayOfMonthStr(end.toLocalDate());
                        listDate.add(LocalDateTime.of(lastDayOfMonthStr.getYear(),lastDayOfMonthStr.getMonth(),
                                lastDayOfMonthStr.getDayOfMonth(),23,59,59));
                    }
                    break;
                case "W":
                    listDate.add(LocalDateUtil.getFirstDayOfWeekStr(start.toLocalDate()));
                    start = start.plusWeeks(1);
                    start = start.with(DayOfWeek.MONDAY);
                    if (!end.isAfter(start)){
                        LocalDateTime lastDayOfWeekStr = LocalDateUtil.getLastDayOfWeekStr(end.toLocalDate());
                        listDate.add(LocalDateTime.of(lastDayOfWeekStr.getYear(),lastDayOfWeekStr.getMonth(),
                                lastDayOfWeekStr.getDayOfMonth(),23,59,59));
                    }
                    break;
                case "D":
                    listDate.add(start);
                    start = start.plusDays(1);
                    if (!end.isAfter(start)){
                        LocalDateTime endTimeOfDayStr = LocalDateUtil.getEndTimeOfDayStr(end.toLocalDate());
                        listDate.add(LocalDateTime.of(endTimeOfDayStr.getYear(),endTimeOfDayStr.getMonth()
                                ,endTimeOfDayStr.getDayOfMonth(), 23,59,59));
                    }
                    break;
                case "H":
                    listDate.add(start);
                    start = start.plusHours(1);
                    if (!end.isAfter(start)){
                        LocalDateTime endTimeOfDayStr = LocalDateUtil.getEndTimeOfDayStr(end.toLocalDate());
                        listDate.add(LocalDateTime.of(endTimeOfDayStr.getYear(),endTimeOfDayStr.getMonth()
                                ,endTimeOfDayStr.getDayOfMonth(), 23,59,59));
                    }
                    break;
            }
        }
        return listDate;
    }


    public static void main(String[] args) {
        LocalDateTime start = LocalDateTime.of(LocalDate.of(2022, 3, 1), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.of(2023, 1, 5), LocalTime.MIN);
        List<LocalDateTime> resList = cutDateRangeNew("Q", start, end);
        resList.forEach(ele -> {
            log.info("{}", ele);
        });
    }
}
