package com.steccn.qms.scheduled;

import com.steccn.common.constant.LockConstant;
import com.steccn.qms.service.QtChartGenerateService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 计算拉引量
 *
 * @author AndrewLuo
 * @date 2023/05/17
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class PullAmountCalculateTask {

    private final QtChartGenerateService qtChartGenerateService;

    /**
     * @desc 每天 8点10分 20点10分执行一次
     * 08:10:00
     * 20:10:00
     */
    @XxlJob("calculatePullAmountHandler")
    public void calculatePullAmount() {
        XxlJobHelper.log("计算拉引量-任务开始");
        qtChartGenerateService.calculatePullAmountTask(LocalDateTime.now(), LocalDate.now());
        XxlJobHelper.log("计算拉引量-任务开始");
    }
}
