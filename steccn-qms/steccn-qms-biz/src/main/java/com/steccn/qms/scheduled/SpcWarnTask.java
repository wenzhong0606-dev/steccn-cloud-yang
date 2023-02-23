package com.steccn.qms.scheduled;

import com.steccn.common.constant.LockConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author yequan
 * @desc spc预警任务
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class SpcWarnTask {

    private final RedissonClient redissonClient;

    /**
     * @desc 每小时执行一次
     * 00:05:00
     * 01:05:00
     * 02:05:00
     */
    @Async
    @Scheduled(cron = "0 5 * * * ? *")
    public void hour() {
        RLock lock = redissonClient.getLock(LockConstant.SPC_WARN_HOUR);
        boolean b = lock.tryLock();
        if (!b) {
            log.info("spc预警任务获取锁失败");
            return;
        }
        log.info("spc预警任务开始");
        try {
            // TODO: 2023/2/23  spc预警任务
        } catch (Exception e) {
            log.error("spc预警任务异常", e);
        } finally {
            log.info("spc预警任务结束");
            lock.unlock();
        }
    }

    /**
     * @desc 每天执行一次
     * 2023-02-24 00:10:00
     * 2023-02-25 00:10:00
     * 2023-02-26 00:10:00
     */
    @Async
    @Scheduled(cron = "0 10 0 * * ? *")
    public void day() {
        RLock lock = redissonClient.getLock(LockConstant.SPC_WARN_DAY);
        boolean b = lock.tryLock();
        if (!b) {
            log.info("spc预警任务获取锁失败");
            return;
        }
        log.info("spc预警任务开始");
        try {
            // TODO: 2023/2/23  spc预警任务
        } catch (Exception e) {
            log.error("spc预警任务异常", e);
        } finally {
            log.info("spc预警任务结束");
            lock.unlock();
        }
    }

    /**
     * @desc 每周执行一次
     * 2023-02-27 00:15:00
     * 2023-03-06 00:15:00
     * 2023-03-13 00:15:00
     * 2023-03-20 00:15:00
     */
    @Async
    @Scheduled(cron = "0 15 0 ? * 2 *")
    public void week() {
        RLock lock = redissonClient.getLock(LockConstant.SPC_WARN_WEEK);
        boolean b = lock.tryLock();
        if (!b) {
            log.info("spc预警任务获取锁失败");
            return;
        }
        log.info("spc预警任务开始");
        try {
            // TODO: 2023/2/23  spc预警任务
        } catch (Exception e) {
            log.error("spc预警任务异常", e);
        } finally {
            log.info("spc预警任务结束");
            lock.unlock();
        }
    }

    /**
     * @desc 每月执行一次
     * 2023-03-01 00:20:00
     * 2023-04-01 00:20:00
     * 2023-05-01 00:20:00
     * 2023-06-01 00:20:00
     */
    @Async
    @Scheduled(cron = "0 20 0 1 1/1 ? *")
    public void month() {
        RLock lock = redissonClient.getLock(LockConstant.SPC_WARN_MONTH);
        boolean b = lock.tryLock();
        if (!b) {
            log.info("spc预警任务获取锁失败");
            return;
        }
        log.info("spc预警任务开始");
        try {
            // TODO: 2023/2/23  spc预警任务
        } catch (Exception e) {
            log.error("spc预警任务异常", e);
        } finally {
            log.info("spc预警任务结束");
            lock.unlock();
        }
    }

    /**
     * @desc 每季度执行一次
     * 2023-04-01 00:25:00
     * 2023-07-01 00:25:00
     * 2023-10-01 00:25:00
     * 2024-01-01 00:25:00
     * 2024-04-01 00:25:00
     */
    @Async
    @Scheduled(cron = "0 25 0 1 1,4,7,10 ? *")
    public void season() {
        RLock lock = redissonClient.getLock(LockConstant.SPC_WARN_SEASON);
        boolean b = lock.tryLock();
        if (!b) {
            log.info("spc预警任务获取锁失败");
            return;
        }
        log.info("spc预警任务开始");
        try {
            // TODO: 2023/2/23  spc预警任务
        } catch (Exception e) {
            log.error("spc预警任务异常", e);
        } finally {
            log.info("spc预警任务结束");
            lock.unlock();
        }
    }

    /**
     * @desc 每年执行一次
     * 2024-01-01 00:30:00
     * 2025-01-01 00:30:00
     * 2026-01-01 00:30:00
     * 2027-01-01 00:30:00
     */
    @Async
    @Scheduled(cron = "0 30 0 1 1 ? *")
    public void year() {
        RLock lock = redissonClient.getLock(LockConstant.SPC_WARN_YEAR);
        boolean b = lock.tryLock();
        if (!b) {
            log.info("spc预警任务获取锁失败");
            return;
        }
        log.info("spc预警任务开始");
        try {
            // TODO: 2023/2/23  spc预警任务
        } catch (Exception e) {
            log.error("spc预警任务异常", e);
        } finally {
            log.info("spc预警任务结束");
            lock.unlock();
        }
    }
}
