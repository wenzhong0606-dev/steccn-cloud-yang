package com.steccn.qms.scheduled;

import cn.hutool.core.collection.CollUtil;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.dp.dto.DpBusiMonitorDTO;
import com.steccn.dp.feign.RemoteBusiMonitorFeignApi;
import com.steccn.qms.service.QtDayReportService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class WaterRateTask {

    private final QtDayReportService qtDayReportService;

    @XxlJob("GS_WATER")
    public void GS_WATER() {
        XxlJobHelper.log("GS_WATER-任务开始");
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusMinutes(10);
        qtDayReportService.samplingWaterRateSiMonitor(start, end, "GS_WATER");
        XxlJobHelper.log("GS_WATER-任务完成");
    }

    @XxlJob("PHL_WATER_ALARM")
    public void PHL_WATER_ALARM() {
        XxlJobHelper.log("PHL_WATER-任务开始");
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusMinutes(10);
        qtDayReportService.samplingWaterRateBatchMonitor(start, end, "PHL_WATER");
        XxlJobHelper.log("PHL_WATER-任务完成");
    }
}
