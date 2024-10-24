package com.steccn.pms.schedule;

import com.steccn.pms.service.PmsReportDashboardService;
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

    @XxlJob("JP_RH_RATE")
    public void JP_RH_RATE() {
        XxlJobHelper.log("JP_RH_RATE-任务开始");
        LocalDate date = LocalDate.now().minusDays(1L);
        pmsReportDashboardService.meltingRateMonitor(date, "JP_RH_RATE");
        XxlJobHelper.log("JP_RH_RATE-任务完成");
    }
}
