package com.steccn.data.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.SplitListUtil;
import com.steccn.data.domain.EpMeterCollect;
import com.steccn.data.domain.PmStationCollect;
import com.steccn.data.dto.DwMeterReadDiffDTO;
import com.steccn.data.dto.PmReportWorkPreloadDTO;
import com.steccn.data.vo.DwEpMeterCollectVO;
import com.steccn.data.dto.DwMeterReadDTO;
import com.steccn.data.service.EpMeterCollectService;
import com.steccn.data.mapper.EpMeterCollectMapper;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yequan
 * @description 针对表【ep_meter_collect(能源绩效-远程仪表采样表)】的数据库操作Service实现
 * @createDate 2022-12-16 13:23:55
 */
@Service
public class EpMeterCollectServiceImpl extends ServiceImpl<EpMeterCollectMapper, EpMeterCollect>
        implements EpMeterCollectService {

    private final ConcurrentHashMap<String, LocalDateTime> lastRecordTimeMap = new ConcurrentHashMap<>(1024);

    private LocalDateTime lastRecordTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);

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
            return BigDecimalUtil.ge(subtract, BigDecimal.ZERO) ? subtract : BigDecimal.ZERO;
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
                .between(EpMeterCollect::getRecordTime, curTime, LocalDateTime.now())
                .groupBy(EpMeterCollect::getCode));
        //如果集合不为空
        if (CollUtil.isNotEmpty(dbList)) {
            dbList.forEach(ele -> {
                if (ele.getRecordTime().isAfter(lastRecordTime)) {
                    lastRecordTime = ele.getRecordTime();
                }
                lastRecordTimeMap.put(ele.getCode(), ele.getRecordTime());
            });
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
}