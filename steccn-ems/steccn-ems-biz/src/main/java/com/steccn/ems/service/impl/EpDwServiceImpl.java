package com.steccn.ems.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.dto.TupleDTO;
import com.steccn.data.dto.DwConditionDTO;
import com.steccn.data.dto.DwMeterReadDTO;
import com.steccn.data.feign.RemoteDataPlatformFeignApi;
import com.steccn.data.vo.DwEpMeterCollectVO;
import com.steccn.ems.domain.EpEnergyMoney;
import com.steccn.ems.domain.EpMeterRead;
import com.steccn.ems.dto.EpDwEnergyPricePlanDTO;
import com.steccn.ems.dto.EpDwMeterPartitionDTO;
import com.steccn.ems.mapper.EpInstrumentAccountMapper;
import com.steccn.ems.service.*;
import com.steccn.pms.dto.DwCurrentReportWorkDTO;
import com.steccn.pms.feign.RemotePmsFeignApi;
import com.steccn.qms.constant.QtManagerConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @desc 数仓控制器
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EpDwServiceImpl implements EpDwService {

    private final EpInstrumentAccountMapper epInstrumentAccountMapper;
    private final RemotePmsFeignApi remotePmsFeignApi;
    private final RemoteDataPlatformFeignApi remoteDataPlatformFeignApi;
    private final EpMeterReadService epMeterReadService;
    private final EpEnergyPricePlanService epEnergyPricePlanService;
    private final EpEnergyMoneyService epEnergyMoneyService;
    private final EpPartitionService epPartitionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean meterRead(LocalDate date) {
        Assert.notNull(date, "指定日期不可为空");
        List<DwConditionDTO> dwList = Lists.newArrayList();

        //获取所有的远传仪表+挂载的分区节点
        List<EpDwMeterPartitionDTO> meterList = epInstrumentAccountMapper.meterPartition();
        if (CollUtil.isNotEmpty(meterList)) {
            Set<Integer> meterSet = meterList.stream().map(EpDwMeterPartitionDTO::getMeterId).collect(Collectors.toSet());
            long count = epMeterReadService.count(new LambdaQueryWrapper<EpMeterRead>()
                    .eq(EpMeterRead::getReadDate, date).in(EpMeterRead::getMeterId, meterSet));
            if (count > 0) {
                log.info("读数归档 班次日期 -> {} 已跑过", date);
                return Boolean.FALSE;
            }
            //获取当前班次日期所有的报工信息
            List<DwCurrentReportWorkDTO> allReportWorkList = FeignOptional.of(remotePmsFeignApi.currentReportWork(new TupleDTO<>(date, date))).get();
            //获取所有有仪表的节点
            Set<Long> allPartitionIdSet = meterList.stream().map(EpDwMeterPartitionDTO::getPartitionId).collect(Collectors.toSet());
            //其余的节点
            Set<Long> otherPartitionNodeSet = epPartitionService.otherPartition(allPartitionIdSet);

            //遍历所有的远传仪表
            for (EpDwMeterPartitionDTO meter : meterList) {
                if (otherPartitionNodeSet.contains(meter.getPartitionId())) {
                    //仅统计每日能耗（类型II）的节点
                    DwConditionDTO dwConditionDTO = new DwConditionDTO();
                    dwConditionDTO.setCode(meter.getDistantMapCode());
                    dwConditionDTO.setMeterId(meter.getMeterId());
                    dwConditionDTO.setReadDate(date);
                    dwConditionDTO.setStart(LocalDateTime.of(date, LocalTime.of(8, 0, 0)));
                    dwConditionDTO.setEnd(LocalDateTime.of(date.plusDays(1), LocalTime.of(8, 0, 0)));
                    dwList.add(dwConditionDTO);
                } else if(CollUtil.isNotEmpty(allReportWorkList)){
                    //节点N是产线节点或产线节点的下级节点：统计能耗考虑班次、产品厚度（类型I）
                    //根据仪表挂载的节点获取报工记录
                    List<DwCurrentReportWorkDTO> lineList = allReportWorkList.stream().filter(v -> meter.getOfficeId().equals(v.getProductLineId())).collect(Collectors.toList());
                    List<DwCurrentReportWorkDTO> stationList = allReportWorkList.stream().filter(v -> meter.getOfficeId().equals(v.getStationId())).collect(Collectors.toList());
                    dwConditionDTO(date, dwList, meter, lineList);
                    dwConditionDTO(date, dwList, meter, stationList);
                    //如果2个都为空
                    if(CollUtil.isEmpty(lineList) && CollUtil.isEmpty(stationList)){
                        LocalDateTime morningLdt = LocalDateTime.of(date, LocalTime.of(8, 0, 0));
                        LocalDateTime nightLdt = LocalDateTime.of(date, LocalTime.of(20, 0, 0));
                        LocalDateTime lastLdt = LocalDateTime.of(date.plusDays(1), LocalTime.of(8, 0, 0));
                        //查询2条数据 T日8：00 T日20：00 T+1日8：00
                        DwConditionDTO morningDefault = getDefaultDwConditionDTO(meter, date, 0, morningLdt, nightLdt);
                        DwConditionDTO nightDefault = getDefaultDwConditionDTO(meter, date, 1, nightLdt, lastLdt);
                        dwList.add(morningDefault);
                        dwList.add(nightDefault);
                    }
                }
            }
            if (CollUtil.isNotEmpty(dwList)) {
                //按照start时间排序
                dwList.sort(Comparator.comparing(DwConditionDTO::getMeterId).thenComparing(DwConditionDTO::getStart));
                List<DwMeterReadDTO> dwMeterReadDTOList = Lists.newArrayList();
                dwList.forEach(ele -> {
                    DwMeterReadDTO readDTO = new DwMeterReadDTO();
                    BeanUtils.copyProperties(ele, readDTO);
                    dwMeterReadDTOList.add(readDTO);
                });

                List<DwEpMeterCollectVO> sourceData = FeignOptional.of(remoteDataPlatformFeignApi.meterRead(dwMeterReadDTOList)).get();
                List<EpMeterRead> insertList = Lists.newArrayList();
                if (CollUtil.isNotEmpty(sourceData)) {
                    //准备插入数据库
                    dwList.forEach(ele -> {
                        EpMeterRead epMeterRead = new EpMeterRead();
                        BeanUtils.copyProperties(ele, epMeterRead);
                        epMeterRead.setDistantMapCode(ele.getCode());
                        Optional<DwEpMeterCollectVO> max = sourceData.stream().filter(v -> v.getCode().equals(ele.getCode()))
                                .filter(v -> !v.getRecordTime().isAfter(ele.getEnd())).max(Comparator.comparing(DwEpMeterCollectVO::getRecordTime));
                        if (max.isPresent()) {
                            DwEpMeterCollectVO dwEpMeterCollectVO = max.get();
                            epMeterRead.setAdminValue(dwEpMeterCollectVO.getValue());
                            epMeterRead.setCorrectValue(dwEpMeterCollectVO.getValue());
                            epMeterRead.setReadValue(dwEpMeterCollectVO.getValue());
                            epMeterRead.setAdminFlag(0);
                            insertList.add(epMeterRead);
                        }
                    });
                }
                if (CollUtil.isNotEmpty(insertList)) {
                    epMeterReadService.saveBatch(insertList);
                }
            }
        }
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean electricMoney(LocalDate date) {
        //保持幂等，判断今天是否已经执行过
        long count = epEnergyMoneyService.count(new LambdaQueryWrapper<EpEnergyMoney>().eq(EpEnergyMoney::getClassDate, date));
        if (count > 0) {
            log.info("班次日期 -> {} 已跑过", date);
            return Boolean.FALSE;
        }
        //1.查询所有的远传仪表
        List<EpDwMeterPartitionDTO> meterList = epInstrumentAccountMapper.meterPartition();
        if (CollUtil.isNotEmpty(meterList)) {
            //2.查询当前班次日期有效的能源计费方案明细
            List<EpDwEnergyPricePlanDTO> priceList = epEnergyPricePlanService.currentValidPlan(date);
            if (CollUtil.isNotEmpty(priceList)) {
                //3.获取节点对应的方案
                List<EpDwEnergyPricePlanDTO> meterPriceList = Lists.newArrayList();
                List<DwMeterReadDTO> dwConditionDTOList = Lists.newArrayList();
                for (EpDwMeterPartitionDTO meter : meterList) {
                    for (EpDwEnergyPricePlanDTO price : priceList) {
                        EpDwEnergyPricePlanDTO vo = new EpDwEnergyPricePlanDTO();
                        BeanUtils.copyProperties(price, vo);
                        vo.setPartitionId(meter.getPartitionId());
                        vo.setDistantMapCode(meter.getDistantMapCode());
                        DwMeterReadDTO condition1 = new DwMeterReadDTO();
                        //班次日期调整
                        if (price.getEnd().equals(LocalTime.of(0, 0, 0))) {
                            condition1.setStart(LocalDateTime.of(date.plusDays(1), price.getEnd()));
                            condition1.setEnd(LocalDateTime.of(date.plusDays(1), price.getEnd()));
                        } else {
                            condition1.setStart(LocalDateTime.of(date, price.getEnd()));
                            condition1.setEnd(LocalDateTime.of(date, price.getEnd()));
                        }
                        condition1.setCode(meter.getDistantMapCode());
                        dwConditionDTOList.add(condition1);
                        DwMeterReadDTO condition2 = new DwMeterReadDTO();
                        condition2.setStart(LocalDateTime.of(date, price.getStart()));
                        condition2.setEnd(LocalDateTime.of(date, price.getStart()));
                        condition2.setCode(meter.getDistantMapCode());
                        dwConditionDTOList.add(condition2);
                        meterPriceList.add(vo);
                    }
                }
                //4.根据对应方案 去查询读数
                List<DwEpMeterCollectVO> meterReadList = FeignOptional.of(remoteDataPlatformFeignApi.meterRead(dwConditionDTOList)).get();
                //解析原始数据
                Map<String, List<DwEpMeterCollectVO>> collect = meterReadList.stream().collect(Collectors.groupingBy(DwEpMeterCollectVO::getCode));

                //meterReadList
                meterReadList = Lists.newArrayList();
                for (Map.Entry<String, List<DwEpMeterCollectVO>> entry : collect.entrySet()) {
                    if (entry.getValue().size() > priceList.size()) {
                        meterReadList.addAll(entry.getValue());
                    }
                }
                if (CollUtil.isNotEmpty(meterReadList)) {
                    //定义插入表
                    List<EpEnergyMoney> insertList = Lists.newArrayList();
                    for (EpDwEnergyPricePlanDTO meterPrice : meterPriceList) {
                        Optional<DwEpMeterCollectVO> endDataOp;
                        LocalDateTime endLdt;
                        if (meterPrice.getEnd().equals(LocalTime.of(0, 0, 0))) {
                            endLdt = LocalDateTime.of(date.plusDays(1), meterPrice.getEnd());
                        } else {
                            endLdt = LocalDateTime.of(date, meterPrice.getEnd());
                        }
                        LocalDateTime finalEndLdt = endLdt;
                        LocalDateTime start = LocalDateTime.of(date, meterPrice.getStart());
                        Optional<DwEpMeterCollectVO> startDataOp = meterReadList.stream().filter(v -> v.getCode().equals(meterPrice.getDistantMapCode()))
                                .filter(v -> !v.getRecordTime().isAfter(start)).max(Comparator.comparing(DwEpMeterCollectVO::getRecordTime));
                        endDataOp = meterReadList.stream().filter(v -> v.getCode().equals(meterPrice.getDistantMapCode()))
                                .filter(v -> !v.getRecordTime().isAfter(finalEndLdt)).max(Comparator.comparing(DwEpMeterCollectVO::getRecordTime));
                        if (startDataOp.isPresent() && endDataOp.isPresent()) {
                            EpEnergyMoney model = new EpEnergyMoney();
                            BeanUtils.copyProperties(meterPrice, model);
                            model.setStartValue(startDataOp.get().getValue());
                            model.setEndValue(endDataOp.get().getValue());
                            model.setStart(LocalDateTime.of(date, meterPrice.getStart()));
                            model.setEnd(endLdt);
                            insertList.add(model);
                        }
                    }
                    if (CollUtil.isNotEmpty(insertList)) {
                        epEnergyMoneyService.saveBatch(insertList);
                    }
                }
            }
        }
        return Boolean.TRUE;
    }

    /**
     * 根据报工数据组织一天的数据
     *
     * @param date
     * @param dwList
     * @param meter
     * @param reportWorkDTO
     */
    private void dwConditionDTO(LocalDate date, List<DwConditionDTO> dwList, EpDwMeterPartitionDTO meter, List<DwCurrentReportWorkDTO> reportWorkDTO) {
        LocalDateTime morningLdt = LocalDateTime.of(date, LocalTime.of(8, 0, 0));
        LocalDateTime nightLdt = LocalDateTime.of(date, LocalTime.of(20, 0, 0));
        LocalDateTime lastLdt = LocalDateTime.of(date.plusDays(1), LocalTime.of(8, 0, 0));
        //获取一个仪表节点一天的报工数据
        for (int i = 0; i < reportWorkDTO.size(); i++) {
            DwCurrentReportWorkDTO reportWork = reportWorkDTO.get(i);
            DwConditionDTO dwConditionDTO1 = getDwConditionDTO(meter, reportWork);
            dwList.add(dwConditionDTO1);
        }
        //按照时间排序
        reportWorkDTO.sort(Comparator.comparing(DwCurrentReportWorkDTO::getStart));
        //考虑数据重叠，去重
        List<DwCurrentReportWorkDTO> filterList = Lists.newArrayList();
        for (DwCurrentReportWorkDTO reportWork : reportWorkDTO) {
            long count = reportWorkDTO.stream().filter(v -> v != reportWork)
                    .filter(v -> (v.getStart().isBefore(reportWork.getStart()) && v.getEnd().isAfter(reportWork.getEnd()))).count();
            if (count == 0) {
                filterList.add(reportWork);
            }
        }
        //插入值缝隙时间
        for (int i = 1; i < filterList.size(); i++) {
            DwCurrentReportWorkDTO prev = reportWorkDTO.get(i - 1);
            DwCurrentReportWorkDTO cur = reportWorkDTO.get(i);
            if (cur.getStart().isAfter(prev.getEnd())) {
                DwConditionDTO dwConditionDTO = restTimeData(meter, prev.getEnd(), cur.getStart(), cur, cur.getClassId());
                dwList.add(dwConditionDTO);
            }
        }
        //考虑离8点最近的时间点
        DwCurrentReportWorkDTO morning = late(reportWorkDTO, morningLdt, Boolean.TRUE);
        //考虑离20点最近的时间点
        DwCurrentReportWorkDTO night = late(reportWorkDTO, nightLdt, Boolean.FALSE);
        //考虑离第二天8：00最近的时间点
        DwCurrentReportWorkDTO last = late(reportWorkDTO, lastLdt, Boolean.FALSE);
        if (Objects.nonNull(morning)) {
            dwList.add(restTimeData(meter, morningLdt, morning.getStart(), morning, QtManagerConstant.QtClassesEnum.DAY.getCode()));
        }
        if (Objects.nonNull(night)) {
            dwList.add(restTimeData(meter, night.getEnd(), nightLdt, night, QtManagerConstant.QtClassesEnum.DAY.getCode()));
        }
        if (Objects.nonNull(last)) {
            if (last.getEnd().isAfter(nightLdt)) {
                dwList.add(restTimeData(meter, last.getEnd(), lastLdt, last, QtManagerConstant.QtClassesEnum.NIGHT.getCode()));
            } else {
                dwList.add(restTimeData(meter, nightLdt, lastLdt, last, QtManagerConstant.QtClassesEnum.NIGHT.getCode()));
            }
        }
    }

    /**
     * 空闲时间的能耗数据
     *
     * @param meter      仪表信息
     * @param start      开始时间
     * @param end        结束时间
     * @param reportWork 最早报工数据
     * @return 最早报工数据
     */
    private DwConditionDTO restTimeData(EpDwMeterPartitionDTO meter, LocalDateTime start, LocalDateTime end, DwCurrentReportWorkDTO reportWork, Integer classId) {
        DwConditionDTO dwConditionDTO = getDwConditionDTO(meter, reportWork);
        dwConditionDTO.setStart(start);
        dwConditionDTO.setEnd(end);
        dwConditionDTO.setReportWorkId(null);
        dwConditionDTO.setThick(null);
        dwConditionDTO.setClassId(classId);
        return dwConditionDTO;
    }

    /**
     * 找离某个时间最近的数据，如果相等或者没有则返回空
     *
     * @param reportWorkDTO 报工集合
     * @param time          时间点
     * @return
     */
    private DwCurrentReportWorkDTO late(List<DwCurrentReportWorkDTO> reportWorkDTO, LocalDateTime time, boolean prev) {
        DwCurrentReportWorkDTO res = null;
        long count = reportWorkDTO.stream().filter(v -> v.getEnd().equals(time)).count();
        if (count == 0) {
            Optional<DwCurrentReportWorkDTO> op;
            if (prev) {
                op = reportWorkDTO.stream().filter(v -> v.getStart().isAfter(time)).min(Comparator.comparing(DwCurrentReportWorkDTO::getStart));
            } else {
                op = reportWorkDTO.stream().filter(v -> v.getEnd().isBefore(time)).max(Comparator.comparing(DwCurrentReportWorkDTO::getEnd));
            }
            if (op.isPresent()) {
                res = op.get();
            }
        }
        return res;
    }


    /**
     * 构造元素
     *
     * @param meter 仪表信息
     * @param data  报工数据
     * @return
     */
    private DwConditionDTO getDwConditionDTO(EpDwMeterPartitionDTO meter, DwCurrentReportWorkDTO data) {
        DwConditionDTO dwConditionDTO = new DwConditionDTO();
        dwConditionDTO.setMeterId(meter.getMeterId());
        dwConditionDTO.setReadDate(data.getClassDate());
        dwConditionDTO.setClassId(data.getClassId());
        dwConditionDTO.setThick(data.getThick());
        dwConditionDTO.setReportWorkId(data.getReportWorkId());
        dwConditionDTO.setCode(meter.getDistantMapCode());
        dwConditionDTO.setStart(data.getStart());
        dwConditionDTO.setEnd(data.getEnd());
        return dwConditionDTO;
    }

    /**
     * 获取默认的数据
     *
     * @param meter
     * @param date
     * @param classId
     * @param start
     * @param end
     * @return
     */
    private DwConditionDTO getDefaultDwConditionDTO(EpDwMeterPartitionDTO meter, LocalDate date, Integer classId, LocalDateTime start, LocalDateTime end) {
        DwConditionDTO dwConditionDTO = new DwConditionDTO();
        dwConditionDTO.setMeterId(meter.getMeterId());
        dwConditionDTO.setReadDate(date);
        dwConditionDTO.setClassId(classId);
        dwConditionDTO.setCode(meter.getDistantMapCode());
        dwConditionDTO.setStart(start);
        dwConditionDTO.setEnd(end);
        return dwConditionDTO;
    }
}
