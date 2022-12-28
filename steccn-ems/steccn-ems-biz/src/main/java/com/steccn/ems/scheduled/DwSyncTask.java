package com.steccn.ems.scheduled;


import com.steccn.common.constant.LockConstant;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.ems.domain.EpMeterRead;
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
public class DwSyncTask{

    private final RedissonClient redissonClient;

    @Async
    @Scheduled(cron = "0 3 9 * * ?")
    public void execute() {
        RLock lock = redissonClient.getLock(LockConstant.READ_CORRECT_SCHEDULED_TASK);
        boolean b = lock.tryLock();
        if(!b){
            log.info("获取锁失败");
            return ;
        }
        log.info("数仓归档-读数开始");
        try {


        }catch (Exception e) {
            log.error("数仓归档-读数异常",e);
        } finally {
            log.info("数仓归档-读数结束");
            lock.unlock();
        }
    }

}
