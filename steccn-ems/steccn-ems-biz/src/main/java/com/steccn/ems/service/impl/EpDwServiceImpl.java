package com.steccn.ems.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.ems.dto.DwConditionDTO;
import com.steccn.ems.dto.EpDwMeterPartitionDTO;
import com.steccn.ems.mapper.EpInstrumentAccountMapper;
import com.steccn.ems.service.EpDwService;
import com.steccn.pms.dto.DwCurrentReportWorkDTO;
import com.steccn.pms.feign.RemotePmsFeignApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
        }
        log.info("dwList -> {}", dwList);
        return Boolean.TRUE;
    }


    /**
     *
     * @param dwList
     * @param meter
     * @param reportWorkDTO
     */
    private void dwConditionDTO(List<DwConditionDTO> dwList, EpDwMeterPartitionDTO meter, List<DwCurrentReportWorkDTO> reportWorkDTO) {
        for (DwCurrentReportWorkDTO data : reportWorkDTO) {
            DwConditionDTO dwConditionDTO = new DwConditionDTO();
            dwConditionDTO.setCode(meter.getDistantMapCode());
            dwConditionDTO.setStart(data.getStart());
            dwConditionDTO.setEnd(data.getEnd());
            dwList.add(dwConditionDTO);
        }
    }

}
