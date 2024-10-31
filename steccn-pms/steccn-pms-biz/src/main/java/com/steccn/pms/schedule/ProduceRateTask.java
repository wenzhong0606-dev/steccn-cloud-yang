package com.steccn.pms.schedule;

import com.steccn.common.util.LocalDateUtil;
import com.steccn.pms.service.PmsReportDashboardService;
import com.steccn.pms.service.PmsSjgReportDashboardCtService;
import com.steccn.pms.service.PmsSjgReportDashboardService;
import com.steccn.qms.constant.QtManagerConstant;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Component
public class ProduceRateTask {

    private final PmsReportDashboardService pmsReportDashboardService;
    private final PmsSjgReportDashboardService pmsSjgReportDashboardService;
    private final PmsSjgReportDashboardCtService pmsSjgReportDashboardCtService;

    @XxlJob("JP_RH_RATE")
    public void JP_RH_RATE() {
        XxlJobHelper.log("JP_RH_RATE-任务开始");
        try {
            LocalDate date = LocalDate.now().minusDays(1L);
            pmsReportDashboardService.meltingRateMonitor(date, "JP_RH_RATE");
        } catch (RuntimeException e) {
            log.error("业务运行异常 -> {}", e.getCause());
            XxlJobHelper.log("业务运行异常 -> {}", e.getCause());
        }
        XxlJobHelper.log("JP_RH_RATE-任务完成");
    }

    @XxlJob("JP_PRODUCT_PASS_RATE")
    public void JP_PRODUCT_PASS_RATE() {
        XxlJobHelper.log("JP_PRODUCT_PASS_RATE-任务开始");
        try {
            LocalDate date = LocalDate.now().minusDays(1L);
            pmsReportDashboardService.lineCompleteMonitor(date, "JP_PRODUCT_PASS_RATE");
        } catch (RuntimeException e) {
            log.error("业务运行异常 -> {}", e.getCause());
            XxlJobHelper.log("业务运行异常 -> {}", e.getCause());
        }
        XxlJobHelper.log("JP_PRODUCT_PASS_RATE-任务完成");
    }

    @XxlJob("JP_IN_STORAGE_RATE")
    public void JP_IN_STORAGE_RATE() {
        XxlJobHelper.log("JP_IN_STORAGE_RATE-任务开始");
        try {
            LocalDate date = LocalDate.now().minusDays(1L);
            pmsReportDashboardService.lineInStorageMonitor(date, "JP_IN_STORAGE_RATE");
        } catch (RuntimeException e) {
            log.error("业务运行异常 -> {}", e.getCause());
            XxlJobHelper.log("业务运行异常 -> {}", e.getCause());
        }
        XxlJobHelper.log("JP_IN_STORAGE_RATE-任务完成");
    }

    @XxlJob("DEEP_PRODUCT_PASS_RATE")
    public void DEEP_PRODUCT_PASS_RATE() {
        XxlJobHelper.log("DEEP_PRODUCT_PASS_RATE-任务开始");
        try {
            LocalDate date = LocalDate.now().minusDays(1L);
            pmsSjgReportDashboardService.lineCompleteMonitor(date, "DEEP_PRODUCT_PASS_RATE");
        } catch (RuntimeException e) {
            log.error("业务运行异常 -> {}", e.getCause());
            XxlJobHelper.log("业务运行异常 -> {}", e.getCause());
        }
        XxlJobHelper.log("DEEP_PRODUCT_PASS_RATE-任务完成");
    }

    @XxlJob("DEEP_IN_STORAGE_PASS_RATE")
    public void DEEP_IN_STORAGE_PASS_RATE() {
        XxlJobHelper.log("DEEP_IN_STORAGE_PASS_RATE-任务开始");
        try {
            LocalDate date = LocalDate.now().minusDays(1L);
            pmsSjgReportDashboardService.lineInStorageMonitor(date, "DEEP_IN_STORAGE_PASS_RATE");
        } catch (RuntimeException e) {
            log.error("业务运行异常 -> {}", e.getCause());
            XxlJobHelper.log("业务运行异常 -> {}", e.getCause());
        }
        XxlJobHelper.log("DEEP_IN_STORAGE_PASS_RATE-任务完成");
    }

    /**
     * 班次粒度的任务
     */
    @XxlJob("DEEP_REPORT_WORK_FINISH_NUM_CLASS")
    public void DEEP_REPORT_WORK_FINISH_NUM_CLASS() {
        XxlJobHelper.log("DEEP_REPORT_WORK_FINISH_NUM_CLASS-任务开始");
        try {
            LocalDateTime time = LocalDateTime.now();
            //判定它属于什么班次 -- 执行它的上一个班次
            Integer classId = LocalDateUtil.getClassId(time, Boolean.FALSE);
            LocalDateTime start, end;
            if (classId == 0) {
                //获取昨天晚班
                start = LocalDateUtil.getNight(time).minusDays(1);
            } else {
                //获取今天早班
                start = LocalDateUtil.getMorning(time);
            }
            end = start.plusHours(12);
            pmsSjgReportDashboardCtService.linePassRateMonitor(start, end, "DEEP_REPORT_WORK_FINISH_NUM", QtManagerConstant.QtGranularityEnum.CLASS);
        } catch (RuntimeException e) {
            log.error("业务运行异常 -> {}", e.getCause());
            XxlJobHelper.log("业务运行异常 -> {}", e.getCause());
        }
        XxlJobHelper.log("DEEP_REPORT_WORK_FINISH_NUM_CLASS-任务完成");
    }

    /**
     * 天粒度的任务
     */
    @XxlJob("DEEP_REPORT_WORK_FINISH_NUM_DAY")
    public void DEEP_REPORT_WORK_FINISH_NUM_DAY() {
        XxlJobHelper.log("DEEP_REPORT_WORK_FINISH_NUM_DAY-任务开始");
        try {
            LocalDate date = LocalDate.now().minusDays(1);
            LocalDateTime start, end;
            start = LocalDateUtil.getMorning(date);
            end = start.plusDays(1);
            pmsSjgReportDashboardCtService.linePassRateMonitor(start, end, "DEEP_REPORT_WORK_FINISH_NUM_DAY", QtManagerConstant.QtGranularityEnum.DAY);
        } catch (RuntimeException e) {
            log.error("业务运行异常 -> {}", e.getCause());
            XxlJobHelper.log("业务运行异常 -> {}", e.getCause());
        }
        XxlJobHelper.log("DEEP_REPORT_WORK_FINISH_NUM_DAY-任务完成");
    }
}