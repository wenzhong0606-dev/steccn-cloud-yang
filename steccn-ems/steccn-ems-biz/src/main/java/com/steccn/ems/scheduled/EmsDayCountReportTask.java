package com.steccn.ems.scheduled;

import com.steccn.ems.service.EpDayReportService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 能耗日统计报表
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class EmsDayCountReportTask {

    private final EpDayReportService epDayReportService;

    @XxlJob("dayCountReport")
    public void dayCountReport() {
        LocalDate classDate = LocalDate.now().minusDays(1);
        epDayReportService.countTask(classDate);

    }
}
