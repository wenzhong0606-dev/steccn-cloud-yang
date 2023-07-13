package com.steccn.ems.scheduled;


import cn.hutool.core.date.DatePattern;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.ems.service.EpDwService;
import com.steccn.ems.service.EpTaskExecuteRetryService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author yequan
 * @desc 数仓归档的task
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class DwSyncTask {

    private final EpDwService epDwService;
    private final EpTaskExecuteRetryService epTaskExecuteRetryService;

    @XxlJob("heatPowerHandler")
    public void heatPower() {
        XxlJobHelper.log("余热发电-任务开始");
        Boolean flag = epDwService.heatPower(LocalDate.now().minusDays(1));
        XxlJobHelper.log("余热发电-任务完成，执行结果 -> {}", flag);
    }

    @XxlJob("meterReadDistantHandler")
    public void meterReadDistant() {
        XxlJobHelper.log("远传仪表能耗Distant任务-任务开始");
        Integer count = epDwService.meterRead(LocalDate.now().minusDays(1));
        XxlJobHelper.log("远传仪表能耗Distant任务-任务完成，任务条数 -> {}", count);
    }

    @XxlJob("electricMoneyHandler")
    public void electricMoney() {
        XxlJobHelper.log("峰谷平电费任务-任务开始");
        Boolean flag = epDwService.electricMoney(LocalDate.now().minusDays(1));
        XxlJobHelper.log("峰谷平电费任务-任务完成，结果 -> {}", flag);
    }

    @XxlJob("chaoHandler")
    public void chao() {
        XxlJobHelper.log("抄表管理任务-任务开始");
        Integer count = epDwService.read(LocalDate.now().minusDays(1));
        XxlJobHelper.log("抄表管理任务-任务完成，结果 -> {}", flag);
    }

    @XxlJob("compensate")
    public void compensate() {
        XxlJobHelper.log("补偿任务-任务开始，开始时间{}", LocalDateUtil.format(LocalDateTime.now(), DatePattern.NORM_DATETIME_MINUTE_PATTERN));
        //一次补偿N条，补偿次数超过5次 会停止补偿
        //补偿策略是 ，补偿次数最少的 ， 时间新的先补偿
        int BATCH_SIZE = 1;
        int MAX_EXEC_COUNT = 5;
        epDwService.compensate(BATCH_SIZE, MAX_EXEC_COUNT);
        XxlJobHelper.log("补偿任务-任务完成");
    }

}
