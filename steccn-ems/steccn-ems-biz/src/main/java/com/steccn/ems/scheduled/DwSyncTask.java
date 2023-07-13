package com.steccn.ems.scheduled;


import com.steccn.ems.service.EpDwService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * @author yequan
 * @desc 数仓归档的task
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class DwSyncTask {

    private final EpDwService epDwService;

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
        Boolean flag = epDwService.read(LocalDate.now().minusDays(1));
        XxlJobHelper.log("抄表管理任务-任务完成，结果 -> {}", flag);
    }
}
