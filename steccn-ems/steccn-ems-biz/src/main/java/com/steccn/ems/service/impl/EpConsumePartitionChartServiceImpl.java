package com.steccn.ems.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import com.steccn.ems.vo.EpConsumeDetailRowVO;
import com.steccn.ems.vo.EpConsumeDetailVO;
import com.steccn.ems.vo.EpMeterReadAllDataVO;
import com.steccn.platform.util.LogicalPageUtil;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.util.ShardTimeUtil;
import com.steccn.qms.util.XAxisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
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
        if (Objects.nonNull(byCode) && !QtManagerConstant.QtGranularityEnum.NONE.getCode().equals(byCode.getCode())) {
            shardDateTime = ClassUtil.cutDateRange(byCode.getType(), start, end);
            Assert.notEmpty(shardDateTime, "时间分片为空");
        } else if (QtManagerConstant.QtGranularityEnum.NONE.getCode().equals(byCode.getCode())) {
            shardDateTime = Lists.newArrayList(LocalDateTime.now(), LocalDateTime.now());
        }
        //获取
        Map<Long, String> partitionIdNameMap = epPartitionService.listByIdCh(dto.getPartitionIdList());

        for (int i = 1; i < shardDateTime.size(); i++) {
            LocalDateTime dataStart = shardDateTime.get(i - 1);
            LocalDateTime dataEnd = shardDateTime.get(i);
            EpChartGraphxAxisDTO<Long, String, BigDecimal> epChartGraphxAxisDTO = new EpChartGraphxAxisDTO<>();
            if (!QtManagerConstant.QtGranularityEnum.NONE.getCode().equals(byCode.getCode())) {
                epChartGraphxAxisDTO.setXAxis(XAxisUtil.convert(byCode, dataStart));
                classDateAllData = classDateAllData.stream()
                        .filter(v -> !LocalDateTime.of(v.getReadDate(), ClassUtil.MORNING_START_TIME).isBefore(dataStart)
                                && LocalDateTime.of(v.getReadDate(), ClassUtil.MORNING_START_TIME).isBefore(dataEnd.plusDays(1))).collect(Collectors.toList());
            }
            List<TupleDTO<Long, List<TupleDTO<String, BigDecimal>>>> data = Lists.newArrayList();
            //遍历所有的分区节点
            for (Long partitionId : dto.getPartitionIdList()) {
                //获取分区数据集合
                List<EpMeterReadAllDataVO> dataSet = classDateAllData.stream().filter(v -> Objects.nonNull(partitionId) && partitionId.equals(v.getOfficeId())).collect(Collectors.toList());
                List<TupleDTO<String, BigDecimal>> poleList = Lists.newArrayList();
                TupleDTO<Long, List<TupleDTO<String, BigDecimal>>> pole = new TupleDTO<>();
                pole.setField1(partitionId);
                pole.setField2(poleList);
                //根据厚度遍历
                for (BigDecimal thick : dto.getThick()) {
                    List<EpMeterReadAllDataVO> thickData ;
                    if(Objects.isNull(thick)){
                        thickData = dataSet.stream().filter(v -> Objects.isNull(v.getThick())).collect(Collectors.toList());
                    }else {
                        thickData = dataSet.stream().filter(v -> Objects.nonNull(v.getThick()) && thick.compareTo(v.getThick()) == 0).collect(Collectors.toList());
                    }
                    BigDecimal reduce = thickData.stream().filter(v -> Objects.nonNull(v.getConsume())).map(v -> v.getConsume()).reduce(BigDecimal.ZERO, BigDecimal::add);
                    TupleDTO<String, BigDecimal> stack = new TupleDTO<>(String.valueOf(thick), reduce);
                    poleList.add(stack);
                }
                data.add(pole);
            }
            epChartGraphxAxisDTO.setData(data);
            dataList.add(epChartGraphxAxisDTO);
        }
        epChartGraphDTO.setOfficeMap(partitionIdNameMap);
        epChartGraphDTO.setDataList(dataList);
        EpChartGraphVO<EpChartGraphXAxisVO, BigDecimal> convert = EpChartGraphUtil.convert(epChartGraphDTO, Boolean.TRUE);
        if (QtManagerConstant.QtGranularityEnum.NONE.getCode().equals(byCode.getCode())) {
            convert.getXData().setData(Lists.newArrayList());
        }
        return convert;
    }

    @Override
    public EpConsumeDetailVO consumePartitionContrastDetail(EpTableDTO dto) {
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
        //时间排序
        CollUtil.sort(dateSet, Comparator.comparing(v -> v));

        //定义返回集合
        EpConsumeDetailVO resVo = new EpConsumeDetailVO();
        List<EpConsumeDetailRowVO> resList = Lists.newArrayList();
        Map<Long, String> partitionIdNameMap = epPartitionService.listByIdCh(dto.getPartitionIdList());

        //定义表头
        List<String> headData = Lists.newArrayList();
        for (Map.Entry<Long, String> partitionEntry : partitionIdNameMap.entrySet()) {
            headData.add(partitionEntry.getValue());
        }


        Map<LocalDate, List<EpMeterReadAllDataVO>> classDateMap = classDateAllData.stream().collect(Collectors.groupingBy(EpMeterReadAllDataVO::getReadDate));
        for (Map.Entry<LocalDate, List<EpMeterReadAllDataVO>> classDateEntry : classDateMap.entrySet()) {
            Map<String, List<EpMeterReadAllDataVO>> thickMap = classDateEntry.getValue().stream().collect(Collectors.groupingBy(v -> v.getClassId() + "_" + v.getThick()));
            for (Map.Entry<String, List<EpMeterReadAllDataVO>> thickEntry : thickMap.entrySet()) {
                String[] s = thickEntry.getKey().split("_");
                EpConsumeDetailRowVO rowVO = new EpConsumeDetailRowVO();
                rowVO.setClassDate(classDateEntry.getKey());
                rowVO.setClassDateStr(LocalDateTimeUtil.formatNormal(classDateEntry.getKey()));
                if(!"null".equals(s[1])){
                    rowVO.setThick(new BigDecimal(s[1]));
                    rowVO.setThickStr(s[1]);
                }
                if(!"null".equals(s[0])){
                    rowVO.setClassId(Integer.parseInt(s[0]));
                    QtManagerConstant.QtClassesEnum code = QtManagerConstant.QtClassesEnum.code(rowVO.getClassId());
                    rowVO.setClassIdStr(Objects.nonNull(code) ? code.getName() : null);
                }
                Map<Long ,BigDecimal> consumeMap = Maps.newHashMap();
                thickEntry.getValue().forEach(ele -> consumeMap.put(ele.getOfficeId(),ele.getConsume()));
                List<String> rowList = Lists.newArrayList();
                for (Map.Entry<Long, String> partitionEntry : partitionIdNameMap.entrySet()) {
                    BigDecimal consume = consumeMap.get(partitionEntry.getKey());
                    if(Objects.nonNull(consume)){
                        rowList.add(BigDecimalUtil.format(consume));
                    }else{
                        rowList.add(null);
                    }
                }
                rowVO.setConsumeList(rowList);
                resList.add(rowVO);
            }
        }
        //行排序规则为
        //1级排序：按班次日期升序排列
        //2级排序：班次，白班在上、夜班在下、“无”在最下
        //3级排序：按产品厚度升序排列，“无”在最下
        resList.sort(Comparator.comparing(EpConsumeDetailRowVO::getClassDate).thenComparing((o1, o2) -> {
            if (Objects.nonNull(o1.getClassId()) && Objects.nonNull(o2.getClassId())) {
                return o1.getClassId() - o2.getClassId();
            } else if (Objects.isNull(o1.getClassId())) {
                return 1;
            } else {
                return -1;
            }
        }).thenComparing((o1, o2) -> {
            if (Objects.nonNull(o1.getThick()) && Objects.nonNull(o2.getThick())) {
                return o1.getThick().compareTo(o2.getThick());
            } else if (Objects.isNull(o1.getThick())) {
                return 1;
            } else {
                return -1;
            }
        }));

        Page<EpConsumeDetailRowVO> page = LogicalPageUtil.page(resList, dto.getCurrent(), dto.getSize());
        resVo.setDataList(page);
        resVo.setHeadData(headData);
        return resVo;
    }
}
