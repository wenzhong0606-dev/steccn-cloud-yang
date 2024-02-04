package com.steccn.ems.scheduled;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.lang.Assert;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.SteccnChartException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.util.RegularUtil;
import com.steccn.common.vo.EpChartGraphVO;
import com.steccn.common.vo.EpChartGraphXAxisVO;
import com.steccn.common.vo.EpChartUnitConsumeAlarmVO;
import com.steccn.ems.domain.*;
import com.steccn.ems.dto.EpTableDTO;
import com.steccn.ems.service.*;
import com.steccn.platform.constant.SysMsgConstant;
import com.steccn.platform.dto.SysMsgSendDTO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteMsgFeignApi;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.websocket.constant.WebsocketConstant;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chenhuan
 * @date 2022/12/26 17:33
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class UnitConsumeAlarmScheduledTask {

    private final EpUnitConsumeAlarmService epUnitConsumeAlarmService;

    @XxlJob("unitConsumeAlarm")
    public void execute() {
        LocalDate prevClassDate = LocalDate.now().minusDays(1);
        epUnitConsumeAlarmService.trigger(prevClassDate);
    }

    @XxlJob("dayConsumeAlarm")
    public void dayConsumeAlarm() {
        LocalDate prevClassDate = LocalDate.now().minusDays(1);
        epUnitConsumeAlarmService.consumeTrigger(prevClassDate, QtManagerConstant.QtGranularityEnum.DAY.getCode());
    }

    @XxlJob("weekConsumeAlarm")
    public void weekConsumeAlarm() {
        LocalDate prevClassDate = LocalDate.now().minusDays(1);
        epUnitConsumeAlarmService.consumeTrigger(prevClassDate, QtManagerConstant.QtGranularityEnum.WEEK.getCode());
    }

    @XxlJob("monthConsumeAlarm")
    public void monthConsumeAlarm() {
        LocalDate prevClassDate = LocalDate.now().minusDays(1);
        epUnitConsumeAlarmService.consumeTrigger(prevClassDate, QtManagerConstant.QtGranularityEnum.MONTH.getCode());
    }
}
