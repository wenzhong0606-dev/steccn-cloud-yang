package com.steccn.pms.schedule;

import com.steccn.pms.service.PmsReportDashboardService;
import com.steccn.pms.service.PmsSjgReportDashboardService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@RequiredArgsConstructor
@Component
public class ProduceRateTask {

    private final PmsReportDashboardService pmsReportDashboardService;
    private final PmsSjgReportDashboardService pmsSjgReportDashboardService;

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
}