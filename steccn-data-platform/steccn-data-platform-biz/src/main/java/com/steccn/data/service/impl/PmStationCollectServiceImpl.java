package com.steccn.data.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.data.domain.EpMeterCollect;
import com.steccn.data.domain.PmStationCollect;
import com.steccn.data.dto.DwStationCollectDTO;
import com.steccn.data.dto.DwStationInfoDTO;
import com.steccn.data.dto.PmReportWorkPreloadDTO;
import com.steccn.data.service.PmStationCollectService;
import com.steccn.data.mapper.PmStationCollectMapper;
import com.steccn.data.vo.DwStationCollectVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * @author yequan
 * @description 针对表【pm_station_collect(生产管理-进出站计数器)】的数据库操作Service实现
 * @createDate 2022-12-16 13:23:55
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PmStationCollectServiceImpl extends ServiceImpl<PmStationCollectMapper, PmStationCollect>
        implements PmStationCollectService {

    @Override
    public List<DwStationCollectVO> stationCollect(DwStationCollectDTO dto) {
        List<DwStationCollectVO> resList = Lists.newArrayList();
        //参数异常，直接返回空集合
        if (CollUtil.isNotEmpty(dto.getCodeList()) && Objects.nonNull(dto.getDate())) {
            List<PmStationCollect> dbList = list(new LambdaQueryWrapper<PmStationCollect>()
                    .in(PmStationCollect::getCode, dto.getCodeList())
                    .between(PmStationCollect::getRecordTime, LocalDateTime.of(dto.getDate(), LocalTime.MIN), LocalDateTime.of(dto.getDate(), LocalTime.MAX)));
            if (CollUtil.isNotEmpty(dbList)) {
                //根据编码分组，查询昨天最大的一条数据，用于差值计算
                Map<String, BigDecimal> previousMap = lastDay(dto);
                //遍历查询结果
                for (int i = 0; i < dbList.size(); i++) {
                    PmStationCollect currentEle = dbList.get(i);
                    DwStationCollectVO vo = new DwStationCollectVO();
                    BeanUtils.copyProperties(currentEle, vo);
                    //获取前一个元素的值
                    BigDecimal previousValue = previousMap.getOrDefault(currentEle.getCode(), BigDecimal.ZERO);
                    //当前元素的值
                    BigDecimal current = currentEle.getValue();
                    //计算差值
                    BigDecimal subtract = BigDecimalUtil.subtract(current, previousValue);
                    previousMap.put(currentEle.getCode(), current);
                    vo.setValue(subtract);
                    resList.add(vo);
                }
            }
        }
        return resList;
    }

    @Override
    public BigDecimal rwPreLoad(PmReportWorkPreloadDTO dto) {
        //根据条件获取出站采集点的采集数据
        LocalDateTime startTime = dto.getStartTime().minusMinutes(10);
        LocalDateTime endTime = dto.getEndTime().minusMinutes(10);
        PmStationCollect start = getOne(new LambdaQueryWrapper<PmStationCollect>()
                .eq(PmStationCollect::getCode, dto.getCode())
                .between(PmStationCollect::getRecordTime, startTime, dto.getStartTime())
                .orderByDesc(PmStationCollect::getRecordTime)
                .last(MpConstant.STR_LIMIT_1));
        PmStationCollect end = getOne(new LambdaQueryWrapper<PmStationCollect>()
                .eq(PmStationCollect::getCode, dto.getCode())
                .between(PmStationCollect::getRecordTime, endTime, dto.getEndTime())
                .orderByDesc(PmStationCollect::getRecordTime)
                .last(MpConstant.STR_LIMIT_1));
        if (Objects.nonNull(start) && Objects.nonNull(end)) {
            if(start.getValue().compareTo(end.getValue()) > 0){
                return end.getValue();
            }else {
                return BigDecimalUtil.subtract(end.getValue(), start.getValue());
            }
        }
        return null;
    }

    @Override
    public List<DwStationCollectVO> stationInfo(DwStationInfoDTO dto) {
        List<DwStationCollectVO> resList = Lists.newArrayList();
        //根据条件获取出站采集点的采集数据
        LocalDateTime startTime = dto.getStartTime().minusDays(1);
        LocalDateTime endTime = dto.getEndTime().minusDays(1);
        List<PmStationCollect> list = list(new LambdaQueryWrapper<PmStationCollect>().in(PmStationCollect::getCode, dto.getCodeList())
                .between(PmStationCollect::getRecordTime, startTime, endTime)
                .orderByDesc(PmStationCollect::getRecordTime));
        list.forEach( pmStationCollect -> {
            DwStationCollectVO dwStationCollectVO = new DwStationCollectVO();
            BeanUtils.copyProperties(pmStationCollect,dwStationCollectVO);
            dwStationCollectVO.setStationId(dto.getStationId());
            dwStationCollectVO.setBatchNo(dto.getBatchNo());
            resList.add(dwStationCollectVO);
        });
        return resList;
    }

    /**
     * 根据编码分组，查询昨天最大的一条数据，用于差值计算
     *
     * @param dto dto
     * @return list
     */
    private Map<String, BigDecimal> lastDay(DwStationCollectDTO dto) {
        Map<String, BigDecimal> previousMap = Maps.newHashMap();
        List<PmStationCollect> lastDayList = list(new QueryWrapper<PmStationCollect>()
                .select("code,max(value) as value")
                .lambda()
                .in(PmStationCollect::getCode, dto.getCodeList())
                .between(PmStationCollect::getRecordTime, LocalDateTime.of(dto.getDate().minusDays(1), LocalTime.MIN), LocalDateTime.of(dto.getDate().minusDays(1), LocalTime.MAX))
                .groupBy(PmStationCollect::getCode));
        if (Objects.nonNull(lastDayList)) {
            lastDayList.forEach(ele -> {
                previousMap.put(ele.getCode(), ele.getValue());
            });
        }
        return previousMap;
    }

}




