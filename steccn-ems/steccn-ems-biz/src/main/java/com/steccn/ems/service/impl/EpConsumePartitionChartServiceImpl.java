package com.steccn.ems.service.impl;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.ClassUtil;
import com.steccn.common.vo.EpChartGraphVO;
import com.steccn.common.vo.EpChartGraphXAxisVO;
import com.steccn.ems.domain.EpInstrumentAccount;
import com.steccn.ems.domain.EpMeterRead;
import com.steccn.ems.dto.EpTableDTO;
import com.steccn.ems.service.EpConsumePartitionChartService;
import com.steccn.ems.service.EpInstrumentAccountService;
import com.steccn.ems.service.EpMeterReadService;
import com.steccn.ems.vo.EpMeterReadAllDataVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @desc 分区能耗接口
 * @date 2022/12/07
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EpConsumePartitionChartServiceImpl implements EpConsumePartitionChartService {

    private final EpMeterReadService epMeterReadService;
    private final EpInstrumentAccountService epInstrumentAccountService;

    @Override
    public EpChartGraphVO<EpChartGraphXAxisVO, BigDecimal> consumePartitionContrast(EpTableDTO dto) {
        //1.列出在区间 [起始日期8:00:00, 终止日期+1 8:00:00) 内的所有班次集合A
        //①区间 [起始日期8:00:00, 终止日期+1 8:00:00)
        LocalDateTime start = LocalDateTime.of(dto.getStart(), ClassUtil.MORNING_START_TIME);
        LocalDateTime end = LocalDateTime.of(dto.getEnd().plusDays(1), ClassUtil.MORNING_START_TIME.minusNanos(1));
        //②时间区间内所有班次集合
        List<TupleDTO<LocalDate, Integer>> tupleDTOS = ClassUtil.classRange(start, end);

        List<EpMeterReadAllDataVO> classDateAllData = epMeterReadService.classDateAllData(tupleDTOS.stream().map(v -> v.getField1()).collect(Collectors.toSet()), dto.getEnergyType(),dto.getPartitionIdList());
        log.info("classDateAllData -> {}",classDateAllData);
        return null;
    }
}
