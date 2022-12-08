package com.steccn.ems.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.common.dto.EpChartGraphDTO;
import com.steccn.common.dto.EpChartGraphxAxisDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.ClassUtil;
import com.steccn.common.util.EpChartGraphUtil;
import com.steccn.common.vo.EpChartGraphVO;
import com.steccn.common.vo.EpChartGraphXAxisVO;
import com.steccn.ems.domain.EpInstrumentAccount;
import com.steccn.ems.domain.EpMeterRead;
import com.steccn.ems.dto.EpTableDTO;
import com.steccn.ems.service.EpConsumePartitionChartService;
import com.steccn.ems.service.EpInstrumentAccountService;
import com.steccn.ems.service.EpMeterReadService;
import com.steccn.ems.service.EpPartitionService;
import com.steccn.ems.vo.EpMeterReadAllDataVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.util.ShardTimeUtil;
import com.steccn.qms.util.XAxisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
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
    private final EpPartitionService epPartitionService;

    @Override
    public EpChartGraphVO<EpChartGraphXAxisVO, BigDecimal> consumePartitionContrast(EpTableDTO dto) {
        //1.列出在区间 [起始日期8:00:00, 终止日期+1 8:00:00) 内的所有班次集合A
        //①区间 [起始日期8:00:00, 终止日期+1 8:00:00)
        LocalDateTime start = LocalDateTime.of(dto.getStart(), ClassUtil.MORNING_START_TIME);
        LocalDateTime end = LocalDateTime.of(dto.getEnd().plusDays(1), ClassUtil.MORNING_START_TIME.minusNanos(1));
        //②时间区间内所有班次集合
        List<TupleDTO<LocalDate, Integer>> tupleDTOS = ClassUtil.classRange(start, end);
        //获取全量数据表
        Set<LocalDate> dateSet = tupleDTOS.stream().map(v -> v.getField1()).collect(Collectors.toSet());
        List<EpMeterReadAllDataVO> classDateAllData = epMeterReadService.classDateAllData(dateSet, dto.getEnergyType(), dto.getPartitionIdList());
        SteccnTableAssertUtil.notEmpty(classDateAllData, "全量数据表为空");

        //根据时间粒度分区
        CollUtil.sort(dateSet, Comparator.comparing(v -> v));
        EpChartGraphDTO<Long, String, BigDecimal> epChartGraphDTO = new EpChartGraphDTO<>();
        List<EpChartGraphxAxisDTO<Long, String, BigDecimal>> dataList = Lists.newArrayList();

        List<LocalDateTime> shardDateTime = Lists.newArrayList();
        //时间粒度
        QtManagerConstant.QtGranularityEnum byCode = QtManagerConstant.QtGranularityEnum.getByCode(dto.getGranularity());
        if(Objects.nonNull(byCode) && !QtManagerConstant.QtGranularityEnum.NONE.getCode().equals(byCode.getCode())){
            shardDateTime = ShardTimeUtil.cutDateRange(byCode.getType(), start, end);
            Assert.notEmpty(shardDateTime,"时间分片为空");
        }
        //获取
        Map<Long, String> partitionIdNameMap = epPartitionService.listByIdCh(dto.getPartitionIdList());

        for (LocalDateTime date : shardDateTime) {
            EpChartGraphxAxisDTO<Long, String, BigDecimal> epChartGraphxAxisDTO = new EpChartGraphxAxisDTO<>();
            epChartGraphxAxisDTO.setXAxis(XAxisUtil.convert(byCode,date));
            //遍历所有的分区节点
            for (Long partitionId : dto.getPartitionIdList()) {
                //获取分区数据集合
                List<EpMeterReadAllDataVO> dataSet = classDateAllData.stream().filter(v -> Objects.nonNull(partitionId) && partitionId.equals(v.getOfficeId())).collect(Collectors.toList());
                //根据厚度分组
                List<TupleDTO<Long, List<TupleDTO<String, BigDecimal>>>> data = Lists.newArrayList();

                List<TupleDTO<String, BigDecimal>> poleList = Lists.newArrayList();
                TupleDTO<Long, List<TupleDTO<String, BigDecimal>>> pole = new TupleDTO<>();
                pole.setField1(partitionId);
                pole.setField2(poleList);
                //根据厚度遍历
                for (BigDecimal thick : dto.getThick()) {
                    List<EpMeterReadAllDataVO> thickData = dataSet.stream().filter(v -> Objects.nonNull(thick) && Objects.nonNull(v.getThick()) && thick.compareTo(v.getThick()) == 0).collect(Collectors.toList());
                    BigDecimal reduce = thickData.stream().filter(v -> Objects.nonNull(v.getConsume())).map(v -> v.getConsume()).reduce(BigDecimal.ZERO, BigDecimal::add);
                    TupleDTO<String, BigDecimal> stack = new TupleDTO<>(String.valueOf(thick), reduce);
                    poleList.add(stack);
                }
                data.add(pole);
                epChartGraphxAxisDTO.setData(data);
            }
            dataList.add(epChartGraphxAxisDTO);
        }
        epChartGraphDTO.setOfficeMap(partitionIdNameMap);
        epChartGraphDTO.setDataList(dataList);
        EpChartGraphVO<EpChartGraphXAxisVO, BigDecimal> convert = EpChartGraphUtil.convert(epChartGraphDTO, Boolean.TRUE);
        return convert;
    }
}
