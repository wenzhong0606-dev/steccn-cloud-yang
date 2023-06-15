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
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
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
        Boolean flag = epDwService.meterRead(LocalDate.now().minusDays(1));
        XxlJobHelper.log("远传仪表能耗Distant任务-任务完成，结果 -> {}", flag);
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
