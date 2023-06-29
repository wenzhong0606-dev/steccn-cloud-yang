package com.steccn.qms.scheduled;

import com.google.common.collect.Lists;
import com.steccn.common.constant.LockConstant;
import com.steccn.qms.service.QtReportService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.Local;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalField;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author yequan
 * @desc spc预警任务
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class SpcWarnTask {

    private final QtReportService qtReportService;

    /**
     * @desc 每小时执行一次
     * 00:05:00
     * 01:05:00
     * 02:05:00
     */
    @XxlJob("spcWarnHourHandler")
    public void hour() {
        XxlJobHelper.log("SPC预警任务-小时任务开始");
        List<LocalDateTime> hs = preTimeSlot(LocalDateTime.now(), "H", 1);
        qtReportService.warnSPC(hs.get(0), hs.get(1), "小时");
        List<LocalDateTime> serialHs = preTimeSlot(LocalDateTime.now(), "H", 7);
        qtReportService.warnSerialUpOrDown(serialHs, "小时");
        XxlJobHelper.log("SPC预警任务-小时任务结束");
    }

    /**
     * @desc 每天执行一次
     * 2023-02-24 00:10:00
     * 2023-02-25 00:10:00
     * 2023-02-26 00:10:00
     */
    @Async
    @Scheduled(cron = "0 10 0 * * ?")
    @XxlJob("spcWarnDayHandler")
    public void day() {
        XxlJobHelper.log("SPC预警任务-天任务开始");
        List<LocalDateTime> hs = preTimeSlot(LocalDateTime.now(), "D", 1);
        qtReportService.warnSPC(hs.get(0), hs.get(1), "天");
        List<LocalDateTime> serialHs = preTimeSlot(LocalDateTime.now(), "D", 7);
        qtReportService.warnSerialUpOrDown(serialHs, "天");
        XxlJobHelper.log("SPC预警任务-天任务结束");
    }

    /**
     * @desc 每周执行一次
     * 2023-02-27 00:15:00
     * 2023-03-06 00:15:00
     * 2023-03-13 00:15:00
     * 2023-03-20 00:15:00
     */
    @Async
    @Scheduled(cron = "0 15 0 ? * 2")
    @XxlJob("spcWarnWeekHandler")
    public void week() {
        XxlJobHelper.log("SPC预警任务-周任务开始");
        List<LocalDateTime> hs = preTimeSlot(LocalDateTime.now(), "W", 1);
        qtReportService.warnSPC(hs.get(0), hs.get(1), "周");
        List<LocalDateTime> serialHs = preTimeSlot(LocalDateTime.now(), "W", 7);
        qtReportService.warnSerialUpOrDown(serialHs, "周");
        XxlJobHelper.log("SPC预警任务-周任务结束");
    }

    /**
     * @desc 每月执行一次
     * 2023-03-01 00:20:00
     * 2023-04-01 00:20:00
     * 2023-05-01 00:20:00
     * 2023-06-01 00:20:00
     */
    @Async
    @Scheduled(cron = "0 20 0 1 1/1 ?")
    @XxlJob("spcWarnMonthHandler")
    public void month() {
        XxlJobHelper.log("SPC预警任务-月任务开始");
        List<LocalDateTime> hs = preTimeSlot(LocalDateTime.now(), "M", 1);
        qtReportService.warnSPC(hs.get(0), hs.get(1), "月");
        List<LocalDateTime> serialHs = preTimeSlot(LocalDateTime.now(), "M", 7);
        qtReportService.warnSerialUpOrDown(serialHs, "月");
        XxlJobHelper.log("SPC预警任务-月任务结束");
    }

    /**
     * @desc 每季度执行一次
     * 2023-04-01 00:25:00
     * 2023-07-01 00:25:00
     * 2023-10-01 00:25:00
     * 2024-01-01 00:25:00
     * 2024-04-01 00:25:00
     */
    @Async
    @Scheduled(cron = "0 25 0 1 1,4,7,10 ?")
    @XxlJob("spcWarnSeasonHandler")
    public void season() {
        XxlJobHelper.log("SPC预警任务-季度任务开始");
        List<LocalDateTime> hs = preTimeSlot(LocalDateTime.now(), "Q", 1);
        qtReportService.warnSPC(hs.get(0), hs.get(1), "季度");
        List<LocalDateTime> serialHs = preTimeSlot(LocalDateTime.now(), "Q", 7);
        qtReportService.warnSerialUpOrDown(serialHs, "季度");
        XxlJobHelper.log("SPC预警任务-季度任务结束");
    }

    /**
     * @desc 每年执行一次
     * 2024-01-01 00:30:00
     * 2025-01-01 00:30:00
     * 2026-01-01 00:30:00
     * 2027-01-01 00:30:00
     */
    @Async
    @Scheduled(cron = "0 30 0 1 1 ?")
    @XxlJob("spcWarnYearHandler")
    public void year() {
        XxlJobHelper.log("SPC预警任务-年任务开始");
        List<LocalDateTime> hs = preTimeSlot(LocalDateTime.now(), "Y", 1);
        qtReportService.warnSPC(hs.get(0), hs.get(1), "年");
        List<LocalDateTime> serialHs = preTimeSlot(LocalDateTime.now(), "Y", 7);
        qtReportService.warnSerialUpOrDown(serialHs, "年");
        XxlJobHelper.log("SPC预警任务-年任务结束");
    }


    /**
     * 获取前一个时间段
     *
     * @param currentTime 当前时间
     * @param granularity 粒度
     * @return
     */
    public static List<LocalDateTime> preTimeSlot(LocalDateTime currentTime, String granularity, Integer num) {
        LocalDateTime start = null;
        LinkedList<LocalDateTime> resList = Lists.newLinkedList();
        while (num-- > 0) {
            LocalDateTime end = null;
            switch (granularity) {
                case "H":
                    //小时-考虑跨天问题
                    end = currentTime.withMinute(0).withSecond(0).withNano(0);
                    start = end.minusHours(1);
                    break;
                case "D":
                    //天
                    end = LocalDateTime.of(currentTime.toLocalDate(), LocalTime.MIN);
                    start = end.minusDays(1);
                    break;
                case "W":
                    //周
                    //找最近的一个周一为结束时间
                    end = LocalDateTime.of(currentTime.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toLocalDate(), LocalTime.MIN);
                    start = end.minusDays(7);
                    break;
                case "M":
                    //月
                    end = LocalDateTime.of(currentTime.withDayOfMonth(1).toLocalDate(), LocalTime.MIN);
                    start = end.minusMonths(1);
                    break;
                case "Q":
                    //季度-获取当前季度的第一个月,还是当前年份
                    Month month = currentTime.getMonth().firstMonthOfQuarter();
                    end = LocalDateTime.of(currentTime.withMonth(month.getValue()).withDayOfMonth(1).toLocalDate(), LocalTime.MIN);
                    start = end.minusMonths(3);
                    break;
                case "Y":
                    //年
                    end = LocalDateTime.of(LocalDate.of(currentTime.getYear(), 1, 1), LocalTime.MIN);
                    start = end.minusYears(1);
                    break;
            }
            currentTime = start;
            resList.addFirst(end);
        }
        resList.addFirst(start);
        return resList;
    }
}
