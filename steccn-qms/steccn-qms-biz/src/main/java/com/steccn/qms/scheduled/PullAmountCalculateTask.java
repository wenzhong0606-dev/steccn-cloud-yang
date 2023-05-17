package com.steccn.qms.scheduled;

import com.steccn.common.constant.LockConstant;
import com.steccn.qms.service.QtChartGenerateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
    private final RedissonClient redissonClient;

    private final QtChartGenerateService qtChartGenerateService;

    /**
     * @desc 每天 8点10分 20点10分执行一次
     * 08:10:00
     * 20:10:00
     */
    @Async
    @Scheduled(cron = "10 8,20 * * *")
    public void calculatePullAmount() {
        RLock lock = redissonClient.getLock(LockConstant.pullAmountCalculate);
        boolean b = lock.tryLock();
        if (!b) {
            log.info("计算拉引量获取锁失败");
            return;
        }
        log.info("计算拉引量任务开始");
        try {
            qtChartGenerateService.calculatePullAmountTask();
        } catch (Exception e) {
            log.error("计算拉引量任务异常", e);
        } finally {
            log.info("计算拉引量任务结束");
            lock.unlock();
        }
    }
}
