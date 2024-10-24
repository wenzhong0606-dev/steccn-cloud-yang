package com.steccn.mes.scheduled;

import com.steccn.mes.service.MesEqpInsTaskService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@RequiredArgsConstructor
@Component
public class SpotCheckTask {

    private final MesEqpInsTaskService mesEqpInsTaskService;

    @XxlJob("SPOT_CHECK_RATE")
    public void JP_RH_RATE() {
        XxlJobHelper.log("SPOT_CHECK_RATE-任务开始");
        try {
            LocalDate date = LocalDate.now().minusDays(1L);
            mesEqpInsTaskService.taskCompleteMonitor(date, "SPOT_CHECK_RATE");
        } catch (RuntimeException e) {
            log.error("业务运行异常 -> {}", e.getCause());
            XxlJobHelper.log("业务运行异常 -> {}", e.getCause());
        }
        XxlJobHelper.log("SPOT_CHECK_RATE-任务完成");
    }

}
