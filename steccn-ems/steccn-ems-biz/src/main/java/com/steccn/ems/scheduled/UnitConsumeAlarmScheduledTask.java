package com.steccn.ems.scheduled;

import com.steccn.ems.service.EpConsumePartitionChartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author chenhuan
 * @date 2022/12/26 17:33
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class UnitConsumeAlarmScheduledTask {

    private final EpConsumePartitionChartService epConsumePartitionChartService;

    @Async
    @Scheduled(cron = "0 3 9 * * ?")
    public void execute() {

    }
}
