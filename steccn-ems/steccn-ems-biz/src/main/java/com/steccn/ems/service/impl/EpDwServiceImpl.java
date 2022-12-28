package com.steccn.ems.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
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
import com.steccn.ems.service.EpDwService;
import com.steccn.ems.service.EpEnergyMoneyService;
import com.steccn.ems.service.EpEnergyPricePlanService;
import com.steccn.ems.service.EpMeterReadService;
import com.steccn.pms.dto.DwCurrentReportWorkDTO;
import com.steccn.pms.feign.RemotePmsFeignApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    @Override
    public Boolean meterRead(LocalDate date) {
        Assert.notNull(date, "指定日期不可为空");
        List<DwConditionDTO> dwList = Lists.newArrayList();

        //获取所有的远传仪表+挂载的分区节点
        List<EpDwMeterPartitionDTO> meterList = epInstrumentAccountMapper.meterPartition();
        if (CollUtil.isNotEmpty(meterList)) {
            //获取当前班次日期所有的报工信息
            List<DwCurrentReportWorkDTO> allReportWorkList = FeignOptional.of(remotePmsFeignApi.currentReportWork(new TupleDTO<>(date, date))).get();
            //遍历所有的远传仪表
            for (EpDwMeterPartitionDTO meter : meterList) {
                //根据仪表挂载的节点获取报工记录
                List<DwCurrentReportWorkDTO> lineList = allReportWorkList.stream().filter(v -> v.getProductLineId().equals(meter.getOfficeId())).collect(Collectors.toList());
                List<DwCurrentReportWorkDTO> stationList = allReportWorkList.stream().filter(v -> v.getStationId().equals(meter.getOfficeId())).collect(Collectors.toList());
                dwConditionDTO(dwList, meter, lineList);
                dwConditionDTO(dwList, meter, stationList);
            }
            if (CollUtil.isNotEmpty(dwList)) {
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
    public Boolean electricMoney(LocalDate date) {
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
                    if (entry.getValue().size() == priceList.size()) {
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
     * @param dwList
     * @param meter
     * @param reportWorkDTO
     */
    private void dwConditionDTO(List<DwConditionDTO> dwList, EpDwMeterPartitionDTO meter, List<DwCurrentReportWorkDTO> reportWorkDTO) {
        for (DwCurrentReportWorkDTO data : reportWorkDTO) {
            DwConditionDTO dwConditionDTO = new DwConditionDTO();
            dwConditionDTO.setMeterId(meter.getMeterId());
            dwConditionDTO.setReadDate(data.getClassDate());
            dwConditionDTO.setClassId(data.getClassId());
            dwConditionDTO.setThick(data.getThick());
            dwConditionDTO.setReportWorkId(data.getReportWorkId());
            dwConditionDTO.setCode(meter.getDistantMapCode());
            dwConditionDTO.setStart(data.getStart());
            dwConditionDTO.setEnd(data.getEnd());
            dwList.add(dwConditionDTO);
        }
    }

}
