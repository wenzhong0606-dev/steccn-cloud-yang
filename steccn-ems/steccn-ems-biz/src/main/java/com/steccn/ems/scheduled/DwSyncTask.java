package com.steccn.ems.scheduled;


import com.steccn.common.constant.LockConstant;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.ems.domain.EpMeterRead;
import com.steccn.ems.service.EpDwService;
import com.steccn.ems.vo.ReadCorrectVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @desc 数仓归档的task
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class DwSyncTask {

    private final RedissonClient redissonClient;
    private final EpDwService epDwService;

    @Async
    @Scheduled(cron = "0 5 0 * * ?")
    public void meterRead() {
        RLock lock = redissonClient.getLock(LockConstant.DW_METER_READ_SCHEDULED_TASK);
        boolean b = lock.tryLock();
        if (!b) {
            log.info("获取锁失败");
            return;
        }
        log.info("抄表归档-读数开始");
        try {
            epDwService.meterRead(LocalDate.now().minusDays(1));
        } catch (Exception e) {
            log.error("抄表归档-读数异常", e);
        } finally {
            log.info("抄表归档-读数结束");
            lock.unlock();
        }
    }


    @Async
    @Scheduled(cron = "0 5 1 * * ?")
    public void electricMoney() {
        RLock lock = redissonClient.getLock(LockConstant.DW_ELECTRIC_MONEY_SCHEDULED_TASK);
        boolean b = lock.tryLock();
        if (!b) {
            log.info("获取锁失败");
            return;
        }
        log.info("数仓归档-读数开始");
        try {
            epDwService.electricMoney(LocalDate.now().minusDays(1));
        } catch (Exception e) {
            log.error("数仓归档-读数异常", e);
        } finally {
            log.info("数仓归档-读数结束");
            lock.unlock();
        }
    }
}
