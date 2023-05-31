package com.steccn.ems.scheduled;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.steccn.common.constant.LockConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.ems.constant.EpManagerConstant;
import com.steccn.ems.domain.EpEnergyMoney;
import com.steccn.ems.domain.EpMeterRead;
import com.steccn.ems.domain.EpTaskExecute;
import com.steccn.ems.service.EpDwService;
import com.steccn.ems.service.EpEnergyMoneyService;
import com.steccn.ems.service.EpTaskExecuteService;
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
import java.util.Objects;
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
    private final EpTaskExecuteService epTaskExecuteService;
    private final EpEnergyMoneyService epEnergyMoneyService;

    /**
     * 每天8点05分执行
     */
    @Async
    @Scheduled(cron = "0 5 8 * * ?")
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

    /**
     * 2023-04-12 08:20:00
     * 2023-04-13 08:20:00
     * 2023-04-14 08:20:00
     * 2023-04-15 08:20:00
     */
    @Async
    @Scheduled(cron = "0 20 8 * * ?")
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

    /**
     * 2023-04-11 16:10:00
     * 2023-04-11 17:10:00
     * 2023-04-11 18:10:00
     * 2023-04-11 19:10:00
     * 2023-04-11 20:10:00
     * 2023-04-11 21:10:00
     * 2023-04-11 22:10:00
     * 2023-04-11 23:10:00
     * 2023-04-12 02:10:00
     * 2023-04-12 03:10:00
     */
    @Async
    @Scheduled(cron = "0 10 9/1 * * ?")
    public void eM() {
        RLock lock = redissonClient.getLock(LockConstant.DW_ELECTRIC_MONEY_SCHEDULED_TASK);
        boolean b = lock.tryLock();
        if (!b) {
            log.info("补偿-获取锁失败");
            return;
        }
        log.info("补偿-开始");
        try {
            LocalDate date = LocalDate.now().minusDays(1);
            EpTaskExecute one = epTaskExecuteService.getOne(new LambdaQueryWrapper<EpTaskExecute>()
                    .eq(EpTaskExecute::getType, EpManagerConstant.TaskTypeEnum.TYPE_0)
                    .eq(EpTaskExecute::getTaskDate, date).last(MpConstant.STR_LIMIT_1));
            if(Objects.nonNull(one) && EpManagerConstant.TaskFlagEnum.ALL_FINISHED.getCode().equals(one.getTaskFlag())){
                log.info("无需补偿");
            }else{
                //只补偿当天
                long count = epEnergyMoneyService.count(new LambdaQueryWrapper<EpEnergyMoney>().eq(EpEnergyMoney::getClassDate, date));
                if(Objects.nonNull(one) && one.getTaskParam() <= count){
                    one.setTaskFlag(EpManagerConstant.TaskFlagEnum.ALL_FINISHED.getCode());
                    epTaskExecuteService.updateById(one);
                    log.info("计数完毕");
                }else{
                    log.info("补偿-重新执行");
                    epDwService.electricMoney(date);
                }
            }
        } catch (Exception e) {
            log.error("补偿-异常", e);
        } finally {
            log.info("补偿-结束");
            lock.unlock();
        }
    }

    /**
     * 表读数-用于抄表管理
     */
    @Async
    @Scheduled(cron = "0 5 8 * * ?")
    public void read() {
        RLock lock = redissonClient.getLock(LockConstant.DW_READ_SCHEDULED_TASK);
        boolean b = lock.tryLock();
        if (!b) {
            log.info("表读数同步获取锁失败");
            return;
        }
        log.info("表读数同步");
        try {
            epDwService.read(LocalDate.now().minusDays(1));
        } catch (Exception e) {
            log.error("表读数同步-异常", e);
        } finally {
            log.info("表读数同步-结束");
            lock.unlock();
        }
    }

}
