package com.steccn.common.util;

import cn.hutool.core.lang.Assert;
import com.google.common.collect.Lists;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.exception.SteccnRuntimeException;
import lombok.extern.slf4j.Slf4j;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yequan
 * @desc 班次的工具类
 */
@Slf4j
public class ClassUtil {

    /**
     * 早班开始时间
     */
    public static final LocalTime MORNING_START_TIME = LocalTime.of(8, 0, 0);

    /**
     * 晚班开始时间
     */
    public static final LocalTime NIGHT_START_TIME = LocalTime.of(20, 0, 0);

    /**
     * 0-早班 1-夜班
     */
    public static final Integer MORNING = 0;
    public static final Integer NIGHT = 1;


    /**
     * 班次的区间
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return 班次的集合[{班次日期1，班次1}，{班次日期2，班次2}]
     */
    public static List<TupleDTO<LocalDate, Integer>> classRange(LocalDateTime start, LocalDateTime end) {
        Assert.isTrue(start.isBefore(end), "开始时间不可以大于结束时间");
        List<TupleDTO<LocalDate, Integer>> resList = Lists.newArrayList();
        //找到开始时间和结束的班次日期和班次
        TupleDTO<LocalDate, Integer> startTupleDTO = classAndDate(start);
        TupleDTO<LocalDate, Integer> endTupleDTO = classAndDate(end);
        //开始时间的班次
        LocalDateTime adjustStart = LocalDateTime.of(startTupleDTO.getField1(), classStartTime(startTupleDTO.getField2()));
        //结束时间的班次-结束时间的班次+12小时
        LocalDateTime adjustEnd = LocalDateTime.of(endTupleDTO.getField1(), classStartTime(endTupleDTO.getField2())).plusHours(12);

        while (adjustStart.isBefore(adjustEnd)) {
            resList.add(classAndDate(adjustStart));
            adjustStart = adjustStart.plusHours(12);
        }
        return resList;
    }


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
                    //年
                    listDate.add(LocalDateUtil.getFirstDayOfYearStr(start.toLocalDate()));
                    start = start.plusYears(1);
                    start = LocalDateTime.of(LocalDate.of(start.getYear(), 1, 1), MORNING_START_TIME);
                    //永远加最后一个元素
                    if (!end.isAfter(start)) {
                        listDate.add(LocalDateUtil.getLastDayOfYearStr(end.toLocalDate()));
                    }
                    break;
                case "Q":
                    //季度
                    int currentMonth = start.getMonthValue();
                    if (currentMonth >= 1 && currentMonth <= 3) {
                        start = LocalDateTime.of(LocalDate.of(start.getYear(), 1, 1), MORNING_START_TIME);
                        listDate.add(start);
                        if (end.isAfter(start)) {
                            listDate.add(LocalDateTime.of(LocalDate.of(end.getYear(), 3, 31), MORNING_START_TIME));
                        }
                        start = start.plusMonths(3);
                    } else if (currentMonth >= 4 && currentMonth <= 6) {
                        start = LocalDateTime.of(LocalDate.of(start.getYear(), 4, 1), MORNING_START_TIME);
                        listDate.add(start);
                        if (end.isAfter(start)) {
                            listDate.add(LocalDateTime.of(LocalDate.of(end.getYear(), 6, 30), MORNING_START_TIME));
                        }
                        start = start.plusMonths(3);
                    } else if (currentMonth >= 7 && currentMonth <= 9) {
                        start = LocalDateTime.of(LocalDate.of(start.getYear(), 7, 1), MORNING_START_TIME);
                        listDate.add(start);
                        if (end.isAfter(start)) {
                            listDate.add(LocalDateTime.of(LocalDate.of(end.getYear(), 9, 30), MORNING_START_TIME));
                        }
                        start = start.plusMonths(3);
                    } else if (currentMonth >= 10 && currentMonth <= 12) {
                        start = LocalDateTime.of(LocalDate.of(start.getYear(), 10, 1), MORNING_START_TIME);
                        listDate.add(start);
                        if (end.isAfter(start)) {
                            listDate.add(LocalDateTime.of(LocalDate.of(end.getYear(), 12, 31), MORNING_START_TIME));
                        }
                        start = start.plusMonths(3);
                    }
                    break;
                case "M":
                    start = LocalDateTime.of(LocalDate.of(start.getYear(), start.getMonth(), 1), MORNING_START_TIME);
                    listDate.add(start);
                    start = start.plusMonths(1);
                    if (!end.isAfter(start)) {
                        LocalDateTime lastDayOfMonthStr = LocalDateUtil.getLastDayOfMonthStr(end.toLocalDate());
                        listDate.add(LocalDateTime.of(LocalDate.of(lastDayOfMonthStr.getYear(), lastDayOfMonthStr.getMonth(),
                                lastDayOfMonthStr.getDayOfMonth()), MORNING_START_TIME));
                    }
                    break;
                case "W":
                    LocalDateTime firstDayOfWeekStr = LocalDateUtil.getFirstDayOfWeekStr(start.toLocalDate());
                    firstDayOfWeekStr = LocalDateTime.of(firstDayOfWeekStr.toLocalDate(), MORNING_START_TIME);
                    listDate.add(firstDayOfWeekStr);
                    start = start.plusWeeks(1);
                    start = start.with(DayOfWeek.MONDAY);
                    start = LocalDateTime.of(start.toLocalDate(), MORNING_START_TIME);
                    if (!end.isAfter(start)) {
                        LocalDateTime lastDayOfWeekStr = LocalDateUtil.getLastDayOfWeekStr(end.toLocalDate());
                        listDate.add(LocalDateTime.of(LocalDate.of(lastDayOfWeekStr.getYear(), lastDayOfWeekStr.getMonth(),
                                lastDayOfWeekStr.getDayOfMonth()), MORNING_START_TIME));
                    }
                    break;
                case "D":
                    listDate.add(start);
                    start = start.plusDays(1);
                    if (!end.isAfter(start)) {
                        LocalDateTime endTimeOfDayStr = LocalDateUtil.getEndTimeOfDayStr(end.toLocalDate());
                        listDate.add(LocalDateTime.of(LocalDate.of(endTimeOfDayStr.getYear(), endTimeOfDayStr.getMonth()
                                , endTimeOfDayStr.getDayOfMonth()), MORNING_START_TIME));
                    }
                    break;
                case "H":
                    listDate.add(start);
                    start = start.plusHours(1);
                    if (!end.isAfter(start)) {
                        LocalDateTime endTimeOfDayStr = LocalDateUtil.getEndTimeOfDayStr(end.toLocalDate());
                        listDate.add(LocalDateTime.of(LocalDate.of(endTimeOfDayStr.getYear(), endTimeOfDayStr.getMonth()
                                , endTimeOfDayStr.getDayOfMonth()), MORNING_START_TIME));
                    }
                    break;
                case "N":
                    //无粒度
                    break;
            }

        }
        return listDate;
    }


    /**
     * 班次时间和日期
     *
     * @param dateTime 时间点
     * @return 班次日期和班次
     */
    public static TupleDTO<LocalDate, Integer> classAndDate(LocalDateTime dateTime) {
        LocalDate date = dateTime.toLocalDate();
        LocalTime time = dateTime.toLocalTime();
        TupleDTO<LocalDate, Integer> resTupleDTO;
        if (time.isBefore(MORNING_START_TIME)) {
            //[0-8）属于前一天夜班
            resTupleDTO = new TupleDTO<>(date.minusDays(1), NIGHT);
        } else if (time.isBefore(NIGHT_START_TIME)) {
            //[8,20)属于当前班次日期，早班
            resTupleDTO = new TupleDTO<>(date, MORNING);
        } else {
            //[20,0)属于当前班次日期，晚班
            resTupleDTO = new TupleDTO<>(date, NIGHT);
        }
        return resTupleDTO;
    }


    /**
     * 根据班次id去获取班次时间
     *
     * @param classId 班次id
     * @return 班次时间
     */
    public static LocalTime classStartTime(Integer classId) {
        if (MORNING.equals(classId)) {
            return MORNING_START_TIME;
        } else if (NIGHT.equals(classId)) {
            return NIGHT_START_TIME;
        }
        throw new SteccnRuntimeException("班次只能是早班或者夜班");
    }


//    public static void main(String[] args) {
//        LocalDateTime start = LocalDateTime.of(2022, 8, 22, 8, 0, 0);
//        LocalDateTime end = LocalDateTime.of(2022, 8, 23, 8, 0, 0);
//
//        List<TupleDTO<LocalDate, Integer>> tupleDTOS = classRange(start, end);
//        log.info("{}", tupleDTOS);
//
//    }


//    public static void main(String[] args) {
//        LocalDateTime start = LocalDateTime.of(2022, 8, 28, 8, 0, 0);
//        LocalDateTime end = LocalDateTime.of(2022, 8, 30, 8, 0, 0);
////
//        List<LocalDateTime> d = cutDateRange("Q", start, end);
//        log.info("{}", d);
//    }
}
