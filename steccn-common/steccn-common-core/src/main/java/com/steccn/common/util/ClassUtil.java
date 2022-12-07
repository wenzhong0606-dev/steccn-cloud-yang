package com.steccn.common.util;

import cn.hutool.core.lang.Assert;
import com.google.common.collect.Lists;
import com.steccn.common.dto.TupleDTO;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private static final Integer MORNING = 0;
    private static final Integer NIGHT = 1;


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
        throw new RuntimeException("班次只能是早班或者夜班");
    }


//    public static void main(String[] args) {
//        LocalDateTime start = LocalDateTime.of(2022, 8, 22, 8, 0, 0);
//        LocalDateTime end = LocalDateTime.of(2022, 8, 23, 8, 0, 0);
//
//        List<TupleDTO<LocalDate, Integer>> tupleDTOS = classRange(start, end);
//        log.info("{}", tupleDTOS);
//
//    }

}
