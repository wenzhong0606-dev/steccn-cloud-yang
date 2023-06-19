package com.steccn.data.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.DingDingUtils;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.data.constant.HbThirdPartyConstant;
import com.steccn.data.domain.EpMeterCollect;
import com.steccn.data.dto.DwMeterReadDTO;
import com.steccn.data.dto.DwMeterReadDiffDTO;
import com.steccn.data.mapper.EpMeterCollectMapper;
import com.steccn.data.service.EpMeterCollectService;
import com.steccn.data.vo.DwEpMeterCollectVO;
import com.steccn.platform.feign.RemoteSysConfigurationFeignApi;
import com.steccn.qms.util.ShardTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yequan
 * @description 针对表【ep_meter_collect(能源绩效-远程仪表采样表)】的数据库操作Service实现
 * @createDate 2022-12-16 13:23:55
 */
@Service
@Slf4j
public class EpMeterCollectServiceImpl extends ServiceImpl<EpMeterCollectMapper, EpMeterCollect>
        implements EpMeterCollectService {

    private final ConcurrentHashMap<String, LocalDateTime> lastRecordTimeMap = new ConcurrentHashMap<>(1024);
    private LocalDateTime lastRecordTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
    @Autowired
    private RemoteSysConfigurationFeignApi sysConfigurationFeignApi;


    @Override
    public DwEpMeterCollectVO meterRead(DwMeterReadDTO dto) {
        DwEpMeterCollectVO resVo = null;
        EpMeterCollect one = getOne(new LambdaQueryWrapper<EpMeterCollect>()
                .eq(EpMeterCollect::getCode, dto.getCode())
                .between(EpMeterCollect::getRecordTime, LocalDateTime.of(dto.getRecordTime().toLocalDate(), LocalTime.MIN), dto.getRecordTime())
                .orderByDesc(EpMeterCollect::getRecordTime)
                .last(MpConstant.STR_LIMIT_1));
        if (Objects.nonNull(one)) {
            resVo = new DwEpMeterCollectVO();
            BeanUtils.copyProperties(one, resVo);
        }
        return resVo;
    }

    @Override
    public BigDecimal meterReadDiff(DwMeterReadDiffDTO dto) {
        //根据条件获取出站采集点的采集数据
        LocalDateTime startTime = dto.getStart().minusDays(1);
        LocalDateTime endTime = dto.getEnd().minusDays(1);
        EpMeterCollect start = getOne(new LambdaQueryWrapper<EpMeterCollect>()
                .eq(EpMeterCollect::getCode, dto.getCode())
                .between(EpMeterCollect::getRecordTime, startTime, dto.getStart())
                .orderByDesc(EpMeterCollect::getRecordTime)
                .last(MpConstant.STR_LIMIT_1));
        EpMeterCollect end = getOne(new LambdaQueryWrapper<EpMeterCollect>()
                .eq(EpMeterCollect::getCode, dto.getCode())
                .between(EpMeterCollect::getRecordTime, endTime, dto.getEnd())
                .orderByDesc(EpMeterCollect::getRecordTime)
                .last(MpConstant.STR_LIMIT_1));
        if (Objects.nonNull(start) && Objects.nonNull(end)) {
            //考虑当前数据断传了， 前一天未断传，则会出现负数
            BigDecimal subtract = BigDecimalUtil.subtract(end.getValue(), start.getValue());
            return Objects.nonNull(subtract) && BigDecimalUtil.ge(subtract, BigDecimal.ZERO) ? subtract : BigDecimal.ZERO;
        }
        return null;
    }

    @Override
    public Map<String, LocalDateTime> lastReadTime(Set<String> distantCodeSet) {
        Map<String, LocalDateTime> resMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(distantCodeSet)) {
            distantCodeSet.forEach(ele -> {
                LocalDateTime lastRecordTime = lastRecordTimeMap.get(ele);
                resMap.put(ele, lastRecordTime);
            });
        }
        return resMap;
    }

    @Override
    public LocalDateTime getLastRecordTime() {
        return lastRecordTime;
    }

    @Override
    public Boolean refreshLastReadTime(LocalDateTime curTime) {
        List<EpMeterCollect> dbList = list(new QueryWrapper<EpMeterCollect>().select("code, max(record_time) as recordTime")
                .lambda()
                .between(EpMeterCollect::getRecordTime, curTime.plusSeconds(1L), LocalDateTime.now())
                .groupBy(EpMeterCollect::getCode));
        //如果集合不为空
        if (CollUtil.isNotEmpty(dbList)) {
            dbList.forEach(ele -> {
                if (LocalDateUtil.gt(ele.getRecordTime(), lastRecordTime)) {
                    lastRecordTime = ele.getRecordTime();
                }
                lastRecordTimeMap.put(ele.getCode(), ele.getRecordTime());
            });
        } else {
            //如果db集合为空 ，此时去查询最近抄表时间，大于阈值则会发送钉钉消息
            LocalDateTime lastRecordTime = getLastRecordTime();
            long between = LocalDateTimeUtil.between(lastRecordTime, LocalDateTime.now(), ChronoUnit.SECONDS);
            //获取阈值
            Map<String, List<Integer>> confMap = FeignOptional.of(() -> sysConfigurationFeignApi.confListInteger(new TripleDTO<>(HbThirdPartyConstant.CONF_AC_LAST_RECORD_CONTENT, Sets.newHashSet(HbThirdPartyConstant.CONF_AC_LAST_RECORD_TIME_MAX_CONTENT), 2)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            if (CollUtil.isNotEmpty(confMap)) {
                List<Integer> maxTimeList = confMap.get(HbThirdPartyConstant.CONF_AC_LAST_RECORD_TIME_MAX_CONTENT);
                if (CollUtil.isNotEmpty(maxTimeList)) {
                    Integer maxTime = maxTimeList.get(0);
                    if (between > maxTime) {
                        Long minutes =  between / 60 ;
                        try {
                            DingDingUtils.sendTextMsg("监控告警-数采数据断传时间已超过"+ minutes +"分钟", Lists.newArrayList("13601717164"), null, false);
                        } catch (Exception e) {
                            log.error("{}", e.getLocalizedMessage());
                        }
                    }
                }
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public List<BigDecimal> seValue(TripleDTO<String, LocalDateTime, LocalDateTime> tripleDTO) {
        String code = tripleDTO.getField1();
        LocalDateTime start = tripleDTO.getField2();
        LocalDateTime end = tripleDTO.getField3();

        EpMeterCollect first = getOne(new LambdaQueryWrapper<EpMeterCollect>()
                .eq(EpMeterCollect::getCode, code)
                .between(EpMeterCollect::getRecordTime, LocalDateTime.of(start.toLocalDate(), LocalTime.MIN), start)
                .orderByDesc(EpMeterCollect::getRecordTime)
                .last(MpConstant.STR_LIMIT_1));
        EpMeterCollect second = getOne(new LambdaQueryWrapper<EpMeterCollect>()
                .eq(EpMeterCollect::getCode, code)
                .between(EpMeterCollect::getRecordTime, LocalDateTime.of(end.toLocalDate(), LocalTime.MIN), end)
                .orderByDesc(EpMeterCollect::getRecordTime)
                .last(MpConstant.STR_LIMIT_1));

        BigDecimal value1 = Objects.nonNull(first) ? first.getValue() : BigDecimal.ZERO;
        BigDecimal value2 = Objects.nonNull(second) ? second.getValue() : value1;
        List<BigDecimal> resList = new ArrayList<>();
        resList.add(value1);
        resList.add(value2);
        return resList;
    }

    @Override
    public BigDecimal heatPower(LocalDate date) {
        //考虑做缓存
        LocalDateTime start = LocalDateUtil.getMorning(date);
        LocalDateTime end = LocalDateUtil.getMorning(date.plusDays(1));
        List<LocalDateTime> shardList = ShardTimeUtil.shard(start, end, 300);

        List<EpMeterCollect> dbList1 = list(new LambdaQueryWrapper<EpMeterCollect>()
                .eq(EpMeterCollect::getCode, "LL1003_P_ER")
                .in(EpMeterCollect::getRecordTime, shardList)
                .gt(EpMeterCollect::getValue, 0)
                .orderByAsc(EpMeterCollect::getRecordTime));

        List<EpMeterCollect> dbList2 = list(new LambdaQueryWrapper<EpMeterCollect>()
                .eq(EpMeterCollect::getCode, "LL1023_P_ER")
                .in(EpMeterCollect::getRecordTime, shardList)
                .gt(EpMeterCollect::getValue, 0)
                .orderByAsc(EpMeterCollect::getRecordTime));
        //根据时间取2个集合的交集
        List<TripleDTO<LocalDateTime, BigDecimal, BigDecimal>> intersection = intersection(dbList1, dbList2);

        BigDecimal resValue = BigDecimal.ZERO;
        if(CollUtil.isNotEmpty(intersection)){
            //根据班次日期查询
            LocalDateTime preLdt = LocalDateUtil.getMorning(intersection.get(0).getField1());
            for (int i = 0; i < intersection.size(); i++) {
                TripleDTO<LocalDateTime, BigDecimal, BigDecimal> ele = intersection.get(i);
                //和班次开始时间差值
                long between = LocalDateTimeUtil.between(preLdt, ele.getField1(), ChronoUnit.MINUTES);
                //求均值
                BigDecimal sum = BigDecimalUtil.add(ele.getField2(), ele.getField3());
                BigDecimal multiply = BigDecimalUtil.multiply(sum, BigDecimal.valueOf(between));
                BigDecimal val = BigDecimalUtil.divide(multiply, BigDecimal.valueOf(60));
                //计算当前节点属于什么班次日期
                resValue = BigDecimalUtil.add(resValue, val);
                preLdt = ele.getField1();
            }
        }
        return resValue;
    }

    /**
     * 获取两个集合的时间交集值
     * @param dbList1 集合1
     * @param dbList2 集合2
     * @return
     */
    public List<TripleDTO<LocalDateTime, BigDecimal, BigDecimal>> intersection(List<EpMeterCollect> dbList1, List<EpMeterCollect> dbList2) {
        int i = 0, j = 0;
        List<TripleDTO<LocalDateTime, BigDecimal, BigDecimal>> resList = Lists.newArrayList();
        while (i < dbList1.size() && j < dbList2.size()) {
            EpMeterCollect one = dbList1.get(i);
            EpMeterCollect two = dbList2.get(j);

            if (one.getRecordTime().isEqual(two.getRecordTime())) {
                resList.add(new TripleDTO<>(one.getRecordTime(), one.getValue(), two.getValue()));
                i++;
                j++;
            }else if(one.getRecordTime().isBefore(two.getRecordTime())){
                i++;
            }else {
                j++;
            }
        }
        return resList;
    }

}