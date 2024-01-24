package com.steccn.ems.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import com.alibaba.excel.util.MapUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.SteccnChartException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.EpChartGraphDTO;
import com.steccn.common.dto.EpChartGraphxAxisDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.*;
import com.steccn.common.vo.*;
import com.steccn.ems.constant.EpManagerConstant;
import com.steccn.ems.domain.EpEnergyType;
import com.steccn.ems.domain.EpInstrumentAccount;
import com.steccn.ems.domain.EpPartition;
import com.steccn.ems.domain.EpProductUnitConsumeStandard;
import com.steccn.ems.dto.EpTableDTO;
import com.steccn.ems.dto.EpTotalConsumeDTO;
import com.steccn.ems.service.*;
import com.steccn.ems.util.DownloadUtil;
import com.steccn.ems.vo.*;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.util.LogicalPageUtil;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.util.XAxisUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private final EpPartitionService epPartitionService;
    private final RemoteDictFeignApi remoteDictFeignApi;
    private final EpEnergyTypeService epEnergyTypeService;
    private final EpProductUnitConsumeStandardService epProductUnitConsumeStandardService;

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

        classDateAllData = classDateAllData.stream().filter(v -> dto.getPartitionIdList().contains(v.getOfficeId())).collect(Collectors.toList());
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
            List<EpMeterReadAllDataVO> filterDataList = classDateAllData;
            LocalDateTime dataStart = shardDateTime.get(i - 1);
            LocalDateTime dataEnd = shardDateTime.get(i);
            EpChartGraphxAxisDTO<Long, String, BigDecimal> epChartGraphxAxisDTO = new EpChartGraphxAxisDTO<>();
            if (!QtManagerConstant.QtGranularityEnum.NONE.getCode().equals(byCode.getCode())) {
                epChartGraphxAxisDTO.setXAxis(XAxisUtil.convert(byCode, dataStart));
                filterDataList = classDateAllData.stream()
                        .filter(v -> LocalDateUtil.isRange(LocalDateTime.of(v.getReadDate(), ClassUtil.MORNING_START_TIME), dataStart, dataEnd, true, false)).collect(Collectors.toList());

            }
            List<TupleDTO<Long, List<TupleDTO<String, BigDecimal>>>> data = Lists.newArrayList();
            //遍历所有的分区节点
            for (Long partitionId : dto.getPartitionIdList()) {
                //获取分区数据集合
                List<EpMeterReadAllDataVO> dataSet = filterDataList.stream().filter(v -> Objects.nonNull(partitionId) && partitionId.equals(v.getOfficeId())).collect(Collectors.toList());
                List<TupleDTO<String, BigDecimal>> poleList = Lists.newArrayList();
                TupleDTO<Long, List<TupleDTO<String, BigDecimal>>> pole = new TupleDTO<>();
                pole.setField1(partitionId);
                pole.setField2(poleList);
                //根据厚度遍历
                for (BigDecimal thick : dto.getThick()) {
                    String thickName = null;
                    List<EpMeterReadAllDataVO> thickData;
                    if (Objects.isNull(thick) || thick.compareTo(BigDecimalUtil.MINUS_ONE) == 0) {
                        thickData = dataSet.stream().filter(v -> Objects.isNull(v.getThick()) || v.getThick().equals(BigDecimalUtil.MINUS_ONE)).collect(Collectors.toList());
                        thickName = "无";
                    } else {
                        thickData = dataSet.stream().filter(v -> Objects.nonNull(v.getThick()) && thick.compareTo(v.getThick()) == 0).collect(Collectors.toList());
                        thickName = thick.setScale(1) + "mm";
                    }

                    List<BigDecimal> reduceList = thickData.stream().filter(v -> Objects.nonNull(v.getConsume())).map(v -> v.getConsume()).collect(Collectors.toList());
                    BigDecimal reduce = BigDecimalUtil.sum(reduceList);
                    TupleDTO<String, BigDecimal> stack = new TupleDTO<>(thickName, reduce);
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
    public ConsumePartitionContrastTableVO consumePartitionContrastTable(EpTableDTO dto) {
        QtManagerConstant.QtGranularityEnum granularityEnum = QtManagerConstant.QtGranularityEnum.getByCode(dto.getGranularity());
        Assert.notNull(granularityEnum, "粒度不存在");
        //1.列出在区间 [起始日期8:00:00, 终止日期+1 8:00:00) 内的所有班次集合A
        //①区间 [起始日期8:00:00, 终止日期+1 8:00:00)
        LocalDateTime start = LocalDateUtil.getMorning(dto.getStart());
        LocalDateTime end = LocalDateUtil.getMorning(dto.getEnd()).plusDays(1);
        //②时间区间内所有班次集合
        List<TupleDTO<LocalDate, Integer>> tupleDTOS = ClassUtil.classRange(start, end);
        //获取全量数据表
        Set<LocalDate> dateSet = tupleDTOS.stream().map(v -> v.getField1()).collect(Collectors.toSet());
        List<EpMeterReadAllDataVO> classDateAllData = epMeterReadService.classDateAllData(dateSet, dto.getEnergyType(), dto.getPartitionIdList());
        SteccnTableAssertUtil.notEmpty(classDateAllData, "全量数据表为空");
        ConsumePartitionContrastTableVO resVo = new ConsumePartitionContrastTableVO();
        List<String> dateStrList = Lists.newArrayList();
        resVo.setDateStrList(dateStrList);

        List<LocalDate> timeList = ShardTimeUtil.cutDate(granularityEnum.getType(), dto.getStart(), dto.getEnd());
        Assert.notEmpty(timeList, "时间参数异常");
        for (int i = 1; i < timeList.size(); i++) {
            LocalDate startArea = timeList.get(i - 1);
            LocalDate endArea = timeList.get(i);
            if (QtManagerConstant.QtGranularityEnum.NONE.equals(granularityEnum)) {
                dateStrList.add(LocalDateUtil.format(startArea, "yyyy-MM-dd") + "~" + LocalDateUtil.format(endArea, "yyyy-MM-dd"));
            } else {
                dateStrList.add(XAxisUtil.convert(granularityEnum, startArea));
            }
        }
        List<ConsumePartitionContrastTableRowVO> rowList = Lists.newArrayList();
        resVo.setRowList(rowList);
        //获取能耗节点
        Map<Long, String> partitionIdNameMap = epPartitionService.listByIdCh(dto.getPartitionIdList());
        for (Long partitionId : dto.getPartitionIdList()) {
            //节点存在
            String partitionName = partitionIdNameMap.get(partitionId);
            for (BigDecimal thick : dto.getThick()) {
                ConsumePartitionContrastTableRowVO row = new ConsumePartitionContrastTableRowVO();
                row.setPartitionName(partitionName);
                //厚度转换
                if (noThick(thick)) {
                    row.setThickName("无");
                } else {
                    row.setThickName(thick.setScale(1) + "mm");
                }
                List<BigDecimal> valueList = Lists.newArrayList();
                row.setValueList(valueList);
                List<EpMeterReadAllDataVO> tmpDataList = classDateAllData.stream().filter(ele ->
                        LocalDateUtil.isRange(ele.getReadDate(), dto.getStart(), dto.getEnd(), true, true) &&
                                BigDecimalUtil.equals(ele.getThick(), thick) && Objects.equals(partitionId, ele.getOfficeId())).collect(Collectors.toList());
                //如果数据集为空，考虑跳过
                if (CollUtil.isEmpty(tmpDataList)) {
                    continue;
                }
                List<BigDecimal> collect = tmpDataList.stream().map(EpMeterReadAllDataVO::getConsume).collect(Collectors.toList());
                row.setSumValue(BigDecimalUtil.sum(collect));
                row.setAvgValue(BigDecimalUtil.divide(row.getSumValue(), new BigDecimal(timeList.size() - 1)));
                rowList.add(row);
                BigDecimal maxTmp = null;
                String maxTmpOccur = null;
                BigDecimal minTmp = null;
                String minTmpOccur = null;
                for (int i = 1; i < timeList.size(); i++) {
                    LocalDate startArea = timeList.get(i - 1);
                    LocalDate endArea = timeList.get(i);
                    //获取平均值，最大值，区间粒度
                    //根据班次日期和厚度筛选出数据
                    List<EpMeterReadAllDataVO> dataList = tmpDataList.stream().filter(ele -> LocalDateUtil.isRange(ele.getReadDate(), startArea, endArea, true, false)).collect(Collectors.toList());
                    List<BigDecimal> valueTmpList = dataList.stream().map(EpMeterReadAllDataVO::getConsume).collect(Collectors.toList());
                    BigDecimal sumArea = BigDecimalUtil.sum(valueTmpList);
                    valueList.add(sumArea);
                    String tmpStr ;
                    if (QtManagerConstant.QtGranularityEnum.NONE.equals(granularityEnum)) {
                        tmpStr = LocalDateUtil.format(startArea, "yyyy-MM-dd") + "~" + LocalDateUtil.format(endArea, "yyyy-MM-dd");
                    } else {
                        tmpStr = XAxisUtil.convert(granularityEnum, startArea);
                    }
                    if (Objects.nonNull(sumArea) && (Objects.isNull(maxTmp) || BigDecimalUtil.lt(maxTmp, sumArea))) {
                        //如果相等，得追加
                        if (BigDecimalUtil.equals(sumArea, maxTmp)) {
                            maxTmpOccur = maxTmpOccur + "," + tmpStr;
                        } else {
                            maxTmpOccur = tmpStr;
                        }
                        maxTmp = sumArea;
                    }
                    if (Objects.nonNull(sumArea) && (Objects.isNull(minTmp) || BigDecimalUtil.gt(minTmp, sumArea))) {
                        //如果相等，得追加
                        if (BigDecimalUtil.equals(sumArea, minTmp)) {
                            minTmpOccur = minTmpOccur + "," + tmpStr;
                        } else {
                            minTmpOccur = tmpStr;
                        }
                        minTmp = sumArea;
                    }
                }
                row.setMaxValue(maxTmp);
                row.setMaxValueOccur(maxTmpOccur);
                row.setMinValue(minTmp);
                row.setMinValueOccur(minTmpOccur);
            }
        }
        return resVo;
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

        List<EpMeterReadAllDataVO> filterList = Lists.newArrayList();
        for (BigDecimal cThick : dto.getThick()) {
            List<EpMeterReadAllDataVO>  list = classDateAllData.stream().filter(v -> BigDecimalUtil.equals(v.getThick(), cThick)).collect(Collectors.toList());
            if(CollUtil.isNotEmpty(list)){
                filterList.addAll(list);
            }
        }
        classDateAllData = filterList;
        classDateAllData = classDateAllData.stream().filter(v -> dto.getPartitionIdList().contains(v.getOfficeId())).collect(Collectors.toList());
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
                if (!"null".equals(s[1])) {
                    rowVO.setThick(new BigDecimal(s[1]).setScale(1));
                    rowVO.setThickStr(rowVO.getThick() + "mm");
                    if (BigDecimalUtil.equals(rowVO.getThick(), BigDecimalUtil.MINUS_ONE)) {
                        rowVO.setThickStr("无");
                    }
                }
                if (!"null".equals(s[0])) {
                    rowVO.setClassId(Integer.parseInt(s[0]));
                    QtManagerConstant.QtClassesEnum code = QtManagerConstant.QtClassesEnum.code(rowVO.getClassId());
                    rowVO.setClassIdStr(Objects.nonNull(code) ? code.getName() : null);
                }
                Map<Long, BigDecimal> consumeMap = Maps.newHashMap();
                thickEntry.getValue().forEach(ele -> consumeMap.put(ele.getOfficeId(), ele.getConsume()));
                List<String> rowList = Lists.newArrayList();
                for (Map.Entry<Long, String> partitionEntry : partitionIdNameMap.entrySet()) {
                    BigDecimal consume = consumeMap.get(partitionEntry.getKey());
                    if (Objects.nonNull(consume)) {
                        rowList.add(BigDecimalUtil.format(consume));
                    } else {
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
        detailComparator(resList);

        //图例过滤
        if (Objects.nonNull(dto.getFilters())) {
            resList = resList.stream().filter(v -> dto.getFilters().contains(v.getThickStr())).collect(Collectors.toList());
        }

        Page<EpConsumeDetailRowVO> page = LogicalPageUtil.page(resList, dto.getCurrent(), dto.getSize());
        resVo.setDataList(page);
        resVo.setHeadData(headData);
        return resVo;
    }

    /**
     * 下载能源分区
     *
     * @param dto
     * @param response
     */
    @SneakyThrows
    @Override
    public void downloadConsume(EpTableDTO dto, HttpServletResponse response) {
        dto.setCurrent(1);
        dto.setSize(999999);
        EpConsumeDetailVO detail = consumePartitionContrastDetail(dto);
        EpEnergyType energyType = epEnergyTypeService.getOne(new LambdaQueryWrapper<EpEnergyType>().eq(EpEnergyType::getId, dto.getEnergyType()), false);
        Map<String, Object> map = MapUtils.newHashMap();
        map.put("energyType", energyType.getName());
        map.put("dateStart", LocalDateUtil.localDateFormat(dto.getStart(), DatePattern.NORM_DATE_PATTERN));
        map.put("dateEnd", LocalDateUtil.localDateFormat(dto.getEnd(), DatePattern.NORM_DATE_PATTERN));
        if (CollUtil.isNotEmpty(dto.getThick())) {
            StringBuilder sb = new StringBuilder();
            dto.getThick().forEach(tmp -> {
               String data =  tmp.toString()+ "mm ";
                if(BigDecimalUtil.equals(tmp,BigDecimalUtil.MINUS_ONE)){
                    data = "无 ";
                }
                sb.append(data);
            });
            map.put("thick", sb.toString());
        } else {
            map.put("thick", "无");
        }
        map.put("granularity", QtManagerConstant.QtGranularityEnum.getByCode(dto.getGranularity()).getName());
        String excelName = "能耗分区对比(" + energyType.getName() + ")_"
                + LocalDateUtil.localDateFormat(dto.getStart(), DatePattern.NORM_DATE_PATTERN) + "_"
                + LocalDateUtil.localDateFormat(dto.getEnd(), DatePattern.NORM_DATE_PATTERN);
        DownloadUtil.downloadEnergyTemplate(response, EpManagerConstant.EP_DOWNLOAD_MODEL_NAME_NHFQDB, excelName, map, detail, dto.getBase64());
    }


    /**
     * 明细比较器
     * 行排序规则为
     * 1级排序：按班次日期升序排列
     * 2级排序：班次，白班在上、夜班在下、“无”在最下
     * 3级排序：按产品厚度升序排列，“无”在最下
     *
     * @param resList
     */
    private void detailComparator(List<? extends EpDetailVO> resList) {
        resList.sort(Comparator.comparing(EpDetailVO::getClassDate).thenComparing((o1, o2) -> {
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
        }).thenComparing((o1, o2) -> {
            if (Objects.nonNull(o1.getSort()) && Objects.nonNull(o2.getSort())) {
                return o1.getSort().compareTo(o2.getSort());
            } else if (Objects.isNull(o1.getSort())) {
                return 1;
            } else {
                return -1;
            }
        }));
    }

    @Override
    public EpChartGraphVO<EpChartGraphXAxisVO, BigDecimal> consumeHistoryContrast(EpTableDTO dto) {
        LocalDateTime start = LocalDateTime.of(dto.getStart(), ClassUtil.MORNING_START_TIME);
        LocalDateTime end = LocalDateTime.of(dto.getEnd().plusDays(1), ClassUtil.MORNING_START_TIME.minusNanos(1));
        //②时间区间内所有班次集合
        List<TupleDTO<LocalDate, Integer>> tupleDTOS = ClassUtil.classRange(start, end);
        //获取全量数据表
        Set<LocalDate> dateSet = tupleDTOS.stream().map(v -> v.getField1()).collect(Collectors.toSet());
        List<EpMeterReadAllDataVO> classDateAllData = epMeterReadService.classDateAllData(dateSet, dto.getEnergyType(), dto.getPartitionIdList());

        LocalDateTime historyStart = LocalDateTime.of(dto.getHistoryStart(), ClassUtil.MORNING_START_TIME);
        LocalDateTime historyEnd = LocalDateTime.of(dto.getHistoryEnd().plusDays(1), ClassUtil.MORNING_START_TIME.minusNanos(1));
        //②时间区间内所有班次集合
        List<TupleDTO<LocalDate, Integer>> historyTupleDTOS = ClassUtil.classRange(historyStart, historyEnd);
        //获取全量数据表
        Set<LocalDate> historyDateSet = historyTupleDTOS.stream().map(v -> v.getField1()).collect(Collectors.toSet());
        List<EpMeterReadAllDataVO> historyClassDateAllData = epMeterReadService.classDateAllData(historyDateSet, dto.getEnergyType(), dto.getPartitionIdList());

        //根据组织机构的id过滤
        classDateAllData = classDateAllData.stream().filter(v -> dto.getPartitionIdList().contains(v.getOfficeId())).collect(Collectors.toList());
        historyClassDateAllData = historyClassDateAllData.stream().filter(v -> dto.getPartitionIdList().contains(v.getOfficeId())).collect(Collectors.toList());

        if (CollUtil.isEmpty(classDateAllData) && CollUtil.isEmpty(historyClassDateAllData)) {
            throw new SteccnChartException("历史全量数据表为空");
        }

        List<String> timeList = Lists.newArrayList();
        timeList.add(dto.getStart() + "-" + dto.getEnd());
        timeList.add(dto.getHistoryStart() + "-" + dto.getHistoryEnd());
        List<List<EpMeterReadAllDataVO>> historyContrastList = Lists.newArrayList(classDateAllData, historyClassDateAllData);
        EpChartGraphDTO<Long, String, BigDecimal> epChartGraphDTO = new EpChartGraphDTO<>();
        List<EpChartGraphxAxisDTO<Long, String, BigDecimal>> dataList = Lists.newArrayList();
        Map<Long, String> officeMap = Maps.newHashMap();


        for (int i = 0; i < historyContrastList.size(); i++) {
            List<EpMeterReadAllDataVO> dataVOList = historyContrastList.get(i);
            EpChartGraphxAxisDTO<Long, String, BigDecimal> epChartGraphxAxisDTO = new EpChartGraphxAxisDTO<>();
            epChartGraphxAxisDTO.setXAxis(timeList.get(i));
            List<TupleDTO<Long, List<TupleDTO<String, BigDecimal>>>> data = Lists.newArrayList();
            List<TupleDTO<String, BigDecimal>> poleList = Lists.newArrayList();
            TupleDTO<Long, List<TupleDTO<String, BigDecimal>>> pole = new TupleDTO<>();
            pole.setField1(Long.valueOf(i));
            pole.setField2(poleList);
            //根据厚度遍历
            for (BigDecimal thick : dto.getThick()) {
                String thickName = null;
                List<EpMeterReadAllDataVO> thickData;
                if (Objects.isNull(thick) || thick.compareTo(BigDecimalUtil.MINUS_ONE) == 0) {
                    thickData = dataVOList.stream().filter(v -> Objects.isNull(v.getThick()) || v.getThick().equals(BigDecimalUtil.MINUS_ONE)).collect(Collectors.toList());
                    thickName = "无";
                } else {
                    thickData = dataVOList.stream().filter(v -> Objects.nonNull(v.getThick()) && thick.compareTo(v.getThick()) == 0).collect(Collectors.toList());
                    thickName = thick.setScale(1) +"mm";
                }
                List<BigDecimal> bdList = thickData.stream().filter(v -> Objects.nonNull(v.getConsume())).map(v -> v.getConsume()).collect(Collectors.toList());
                BigDecimal reduce = BigDecimalUtil.sum(bdList);
                TupleDTO<String, BigDecimal> stack = new TupleDTO<>(thickName, reduce);
                poleList.add(stack);
            }
            data.add(pole);
            epChartGraphxAxisDTO.setData(data);
            dataList.add(epChartGraphxAxisDTO);
            officeMap.put(Long.valueOf(i), timeList.get(i));
        }
        epChartGraphDTO.setOfficeMap(officeMap);
        epChartGraphDTO.setDataList(dataList);
        EpChartGraphVO<EpChartGraphXAxisVO, BigDecimal> convert = EpChartGraphUtil.convert(epChartGraphDTO, Boolean.TRUE);
        convert.getXData().setData(Lists.newArrayList());
        return convert;
    }

    @Override
    public Page<ConsumeHistoryContrastDetailVO> consumeHistoryContrastDetail(EpTableDTO dto) {
        LocalDateTime start = LocalDateTime.of(dto.getStart(), ClassUtil.MORNING_START_TIME);
        LocalDateTime end = LocalDateTime.of(dto.getEnd().plusDays(1), ClassUtil.MORNING_START_TIME.minusNanos(1));
        //②时间区间内所有班次集合
        List<TupleDTO<LocalDate, Integer>> tupleDTOS = ClassUtil.classRange(start, end);
        //获取全量数据表
        Set<LocalDate> dateSet = tupleDTOS.stream().map(v -> v.getField1()).collect(Collectors.toSet());
        List<EpMeterReadAllDataVO> classDateAllData = epMeterReadService.classDateAllData(dateSet, dto.getEnergyType(), dto.getPartitionIdList());
        List<EpMeterReadAllDataVO> filterList = Lists.newArrayList();
        for (BigDecimal cThick : dto.getThick()) {
            List<EpMeterReadAllDataVO>  list = classDateAllData.stream().filter(v -> BigDecimalUtil.equals(v.getThick(), cThick)).collect(Collectors.toList());
            if(CollUtil.isNotEmpty(list)){
                filterList.addAll(list);
            }
        }
        classDateAllData = filterList;

        //根据组织机构的id过滤
        classDateAllData = classDateAllData.stream().filter(v -> dto.getPartitionIdList().contains(v.getOfficeId())).collect(Collectors.toList());
        SteccnTableAssertUtil.notEmpty(classDateAllData, "全量数据表为空");
        //时间排序
        CollUtil.sort(dateSet, Comparator.comparing(v -> v));

        //定义返回集合
        List<ConsumeHistoryContrastDetailVO> resList = Lists.newArrayList();
        Map<LocalDate, List<EpMeterReadAllDataVO>> classDateMap = classDateAllData.stream().collect(Collectors.groupingBy(EpMeterReadAllDataVO::getReadDate));
        for (Map.Entry<LocalDate, List<EpMeterReadAllDataVO>> classDateEntry : classDateMap.entrySet()) {
            Map<String, List<EpMeterReadAllDataVO>> thickMap = classDateEntry.getValue().stream().collect(Collectors.groupingBy(v -> v.getClassId() + "_" + v.getThick()));
            for (Map.Entry<String, List<EpMeterReadAllDataVO>> thickEntry : thickMap.entrySet()) {
                String[] s = thickEntry.getKey().split("_");
                ConsumeHistoryContrastDetailVO rowVO = new ConsumeHistoryContrastDetailVO();
                rowVO.setClassDate(classDateEntry.getKey());
                rowVO.setClassDateStr(LocalDateTimeUtil.formatNormal(classDateEntry.getKey()));
                if (!"null".equals(s[1])) {
                    rowVO.setThick(new BigDecimal(s[1]).setScale(1));
                    rowVO.setThickStr(rowVO.getThick() +"mm");
                    if(BigDecimalUtil.equals(rowVO.getThick(),BigDecimalUtil.MINUS_ONE)){
                        rowVO.setThickStr("无");
                    }
                }
                if (!"null".equals(s[0])) {
                    rowVO.setClassId(Integer.parseInt(s[0]));
                    QtManagerConstant.QtClassesEnum code = QtManagerConstant.QtClassesEnum.code(rowVO.getClassId());
                    rowVO.setClassIdStr(Objects.nonNull(code) ? code.getName() : null);
                }
                List<BigDecimal> reduceList = thickEntry.getValue().stream().filter(v -> Objects.nonNull(v.getConsume())).map(v -> v.getConsume()).collect(Collectors.toList());
                BigDecimal reduce = BigDecimalUtil.sum(reduceList);
                rowVO.setConsumeStr(BigDecimalUtil.format(reduce));
                resList.add(rowVO);
            }
        }
        //行排序规则为
        //1级排序：按班次日期升序排列
        //2级排序：班次，白班在上、夜班在下、“无”在最下
        //3级排序：按产品厚度升序排列，“无”在最下
        detailComparator(resList);

        //图例过滤
        if (Objects.nonNull(dto.getFilters())) {
            resList = resList.stream().filter(v -> dto.getFilters().contains(v.getThickStr())).collect(Collectors.toList());
        }

        return LogicalPageUtil.page(resList, dto.getCurrent(), dto.getSize());
    }

    /**
     * 下载历史
     *
     * @param dto
     * @param response
     */
    @SneakyThrows
    @Override
    public void downloadHistoryConsume(EpTableDTO dto, HttpServletResponse response) {
        dto.setCurrent(1);
        dto.setSize(999999);
        Page<ConsumeHistoryContrastDetailVO> page = consumeHistoryContrastDetail(dto);
        EpEnergyType energyType = epEnergyTypeService.getOne(new LambdaQueryWrapper<EpEnergyType>().eq(EpEnergyType::getId, dto.getEnergyType()), false);
        EpPartition partition = epPartitionService.getOne(new LambdaQueryWrapper<EpPartition>().eq(EpPartition::getId, dto.getPartitionIdList().get(0)), false);
        Map<String, Object> map = MapUtils.newHashMap();
        map.put("energyType", energyType.getName());
        map.put("dateStart", LocalDateUtil.localDateFormat(dto.getStart(), DatePattern.NORM_DATE_PATTERN));
        map.put("dateEnd", LocalDateUtil.localDateFormat(dto.getEnd(), DatePattern.NORM_DATE_PATTERN));
        map.put("hisDateStart", LocalDateUtil.localDateFormat(dto.getHistoryStart(), DatePattern.NORM_DATE_PATTERN));
        map.put("hisDateEnd", LocalDateUtil.localDateFormat(dto.getHistoryEnd(), DatePattern.NORM_DATE_PATTERN));
        map.put("partitionName", partition.getOfficeName());
        if (CollUtil.isNotEmpty(dto.getThick())) {
            StringBuilder sb = new StringBuilder();
            dto.getThick().forEach(tmp -> {
                String data = tmp.toString() + "mm ";
                if (BigDecimalUtil.equals(tmp, BigDecimalUtil.MINUS_ONE)) {
                    data = "无 ";
                }
                sb.append(data);
            });
            map.put("thick", sb.toString());
        } else {
            map.put("thick", "无");
        }
        String excelName = "能耗历史对比(" + energyType.getName() + ")_"
                + LocalDateUtil.localDateFormat(dto.getStart(), DatePattern.NORM_DATE_PATTERN) + "_"
                + LocalDateUtil.localDateFormat(dto.getEnd(), DatePattern.NORM_DATE_PATTERN);
        DownloadUtil.downloadTemplate(response, EpManagerConstant.EP_DOWNLOAD_MODEL_NAME_NHLSDB, excelName, map, page.getRecords(), dto.getBase64());
    }

    @Override
    public List<BaseSelectVO<BigDecimal>> thick(EpTableDTO dto) {
        LocalDateTime start = LocalDateTime.of(dto.getStart(), ClassUtil.MORNING_START_TIME);
        LocalDateTime end = LocalDateTime.of(dto.getEnd().plusDays(1), ClassUtil.MORNING_START_TIME.minusNanos(1));
        if (Objects.isNull(dto.getConsumeType())) {
            dto.setConsumeType(0);
        }
        Set<BigDecimal> thickSet = thickSelect(start, end, dto.getEnergyType(), dto.getPartitionIdList(), dto.getConsumeType());
        if (Objects.nonNull(dto.getHistoryStart()) && Objects.nonNull(dto.getHistoryEnd())) {
            LocalDateTime historyStart = LocalDateTime.of(dto.getHistoryStart(), ClassUtil.MORNING_START_TIME);
            LocalDateTime historyEnd = LocalDateTime.of(dto.getHistoryEnd().plusDays(1), ClassUtil.MORNING_START_TIME.minusNanos(1));
            thickSet.addAll(thickSelect(historyStart, historyEnd, dto.getEnergyType(), dto.getPartitionIdList(), dto.getConsumeType()));
        }
        //如果是电单耗 则删除无选项
        if(dto.getEnergyType() == 1 && dto.getConsumeType() ==1){
            thickSet.remove(new BigDecimal("-1"));
        }

        //排序问题 厚度为空，放最后
        List<BigDecimal> sort = CollUtil.sort(thickSet, (o1, o2) -> {
            if (noThick(o1)) {
                return Integer.MAX_VALUE;
            }
            return o1.compareTo(o2);
        });

        List<BaseSelectVO<BigDecimal>> resList = Lists.newArrayList();
        for (BigDecimal thick : sort) {
            String name;
            if (noThick(thick)) {
                thick = BigDecimalUtil.MINUS_ONE;
                name = "无";
            } else {
                thick = thick.setScale(1);
                name = String.valueOf(thick);
            }
            BaseSelectVO<BigDecimal> baseSelectVO = new BaseSelectVO<>(thick, name);
            resList.add(baseSelectVO);
        }
        return resList;
    }

    @Override
    public EpChartGraphVO<EpChartGraphXAxisVO, BigDecimal> consumePartitionUnitClassChart(EpTableDTO dto) {
        LocalDateTime start = LocalDateTime.of(dto.getStart(), ClassUtil.MORNING_START_TIME);
        LocalDateTime end = LocalDateTime.of(dto.getEnd().plusDays(1), ClassUtil.MORNING_START_TIME.minusNanos(1));
        //②时间区间内所有班次集合
        List<TupleDTO<LocalDate, Integer>> tupleDTOS = ClassUtil.classRange(start, end);
        //获取全量数据表
        Set<LocalDate> dateSet = tupleDTOS.stream().map(v -> v.getField1()).collect(Collectors.toSet());
        List<EpMeterReadAllDataVO> classDateAllData = null;
        if (dto.getEnergyType() != 5) {
            classDateAllData = epMeterReadService.classDateAllDataUnit(dateSet, dto.getEnergyType(), dto.getPartitionIdList());
        } else {
            //燃料(气+油)的单耗
            classDateAllData = epMeterReadService.kcalDataUnit(dateSet, dto.getPartitionIdList());
        }
        classDateAllData = classDateAllData.stream().filter(v -> dto.getPartitionIdList().contains(v.getOfficeId())).collect(Collectors.toList());
        SteccnTableAssertUtil.notEmpty(classDateAllData, "全量数据表为空");

        List<EpMeterReadAllDataVO> thickAllDataList = classDateAllData.stream().filter(v -> Objects.nonNull(v.getOfficeId()))
                .filter(v -> Objects.nonNull(v.getClassGroupId()))
                .filter(v -> BigDecimalUtil.equals(v.getThick(), dto.getThick().get(0)))
                .collect(Collectors.toList());
        SteccnTableAssertUtil.notEmpty(thickAllDataList, "筛选厚度和班组后全量数据表为空");

        //获取班组集合
        Set<Long> classGroupIdSet = thickAllDataList.stream().map(v -> v.getClassGroupId()).collect(Collectors.toSet());
        Map<Long, String> dictIdNameMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(classGroupIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

        //获取分区节点的map
        Map<Long, String> partitionIdNameMap = epPartitionService.listByIdCh(dto.getPartitionIdList());

        EpChartGraphDTO<Long, String, BigDecimal> epChartGraphDTO = new EpChartGraphDTO<>();
        List<EpChartGraphxAxisDTO<Long, String, BigDecimal>> dataList = Lists.newArrayList();

        long idx = 1;
        Map<Long, String> officeMap = Maps.newHashMap();


        //遍历所有的分区节点
        for (Map.Entry<Long, String> partitionEntry : partitionIdNameMap.entrySet()) {
            //班组分类
            for (Map.Entry<Long, String> classGroupEntry : dictIdNameMap.entrySet()) {
                //过滤之后的数据
                List<EpMeterReadAllDataVO> filterData = thickAllDataList.stream().filter(v -> v.getOfficeId().equals(partitionEntry.getKey()) && v.getClassGroupId().equals(classGroupEntry.getKey())).collect(Collectors.toList());
                //计算单耗的平均值
                List<BigDecimal> consumeList = filterData.stream().filter(v -> Objects.nonNull(v.getConsume())).map(EpMeterReadAllDataVO::getConsume).collect(Collectors.toList());
                BigDecimal consume = BigDecimalUtil.sum(consumeList);
                List<BigDecimal> productNumList = filterData.stream().filter(v -> Objects.nonNull(v.getProductNum())).map(EpMeterReadAllDataVO::getProductNum).collect(Collectors.toList());
                BigDecimal productNum = BigDecimalUtil.sum(productNumList);
                BigDecimal divide = BigDecimalUtil.divide(consume, productNum);
                EpChartGraphxAxisDTO<Long, String, BigDecimal> epChartGraphxAxisDTO = new EpChartGraphxAxisDTO<>();
                epChartGraphxAxisDTO.setXAxis(Strings.EMPTY);
                List<TupleDTO<Long, List<TupleDTO<String, BigDecimal>>>> list = Lists.newArrayList();
                TupleDTO tupleDTO = new TupleDTO(idx, Lists.newArrayList(new TupleDTO("单耗平均值", divide)));
                officeMap.put(idx, partitionEntry.getValue() + classGroupEntry.getValue());
                list.add(tupleDTO);
                epChartGraphxAxisDTO.setData(list);
                dataList.add(epChartGraphxAxisDTO);
                idx++;
            }
        }

        epChartGraphDTO.setDataList(dataList);
        epChartGraphDTO.setOfficeMap(officeMap);

        //查询节点是否包含有基片和深加工
        Map<Long, String> partitionMap = epPartitionService.listByIdCh(dto.getPartitionIdList());
        Set<String> partitionNameSet = partitionMap.values().stream().collect(Collectors.toSet());
        Set<Long> typeSet = Sets.newHashSet();
        for (String name : partitionNameSet) {
            if(name.indexOf("基片") != -1){
                typeSet.add(0L);
            }else if(name.indexOf("深加工") != -1){
                typeSet.add(1L);
            }
        }

        //部门行业的单耗标准
        List<StandardVO<BigDecimal>> standardVOList = epProductUnitConsumeStandardService.validStandardList(dto.getEnergyType(), dto.getStart(), dto.getEnd());
        standardVOList = standardVOList.stream().filter(v -> {
            if (BigDecimalUtil.equals(dto.getThick().get(0), BigDecimalUtil.MINUS_ONE)) {
                return BigDecimalUtil.equals(null, v.getThick());
            }
            return BigDecimalUtil.equals(dto.getThick().get(0), v.getThick());
        }).collect(Collectors.toList());
        if(CollUtil.isNotEmpty(typeSet)){
            standardVOList = standardVOList.stream().filter(v -> typeSet.contains(v.getProductType())).collect(Collectors.toList());
        }
        epChartGraphDTO.setStandardList(standardVOList);
        EpChartGraphVO<EpChartGraphXAxisVO, BigDecimal> convert = EpChartGraphUtil.convert(epChartGraphDTO, Boolean.TRUE);
        convert.getXData().setData(Lists.newArrayList());
        return convert;
    }

    @Override
    public Page<ConsumePartitionUnitClassDetailVO> consumePartitionUnitClassDetail(EpTableDTO dto) {
        LocalDateTime start = LocalDateTime.of(dto.getStart(), ClassUtil.MORNING_START_TIME);
        LocalDateTime end = LocalDateTime.of(dto.getEnd().plusDays(1), ClassUtil.MORNING_START_TIME.minusNanos(1));
        //②时间区间内所有班次集合
        List<TupleDTO<LocalDate, Integer>> tupleDTOS = ClassUtil.classRange(start, end);
        //获取全量数据表
        Set<LocalDate> dateSet = tupleDTOS.stream().map(v -> v.getField1()).collect(Collectors.toSet());
        List<EpMeterReadAllDataVO> classDateAllData = epMeterReadService.classDateAllDataUnit(dateSet, dto.getEnergyType(), dto.getPartitionIdList());
        classDateAllData = classDateAllData.stream().filter(v -> dto.getPartitionIdList().contains(v.getOfficeId())).collect(Collectors.toList());
        SteccnTableAssertUtil.notEmpty(classDateAllData, "单耗全量数据表为空");

        List<EpMeterReadAllDataVO> thickAllDataList = classDateAllData.stream().filter(v -> Objects.nonNull(v.getOfficeId()))
                .filter(v -> Objects.nonNull(v.getClassGroupId()))
                .filter(v -> BigDecimalUtil.equals(v.getThick(), dto.getThick().get(0)))
                .collect(Collectors.toList());
        SteccnTableAssertUtil.notEmpty(thickAllDataList, "筛选厚度和班组后全量数据表为空");
        //获取单耗单位
        String unitName = unitName(dto.getEnergyType());
        Set<Long> partitionIdSet = thickAllDataList.stream().map(v -> v.getOfficeId()).collect(Collectors.toSet());
        Set<Long> classGroupIdSet = thickAllDataList.stream().map(v -> v.getClassGroupId()).collect(Collectors.toSet());
        Map<Long, EpPartition> partitionEntityMap = epPartitionService.listById(partitionIdSet);
        EpPartition defaultEpPartition = new EpPartition();
        Map<Long, String> dictIdNameMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(classGroupIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

        //定义返回集合
        List<ConsumePartitionUnitClassDetailVO> resList = Lists.newArrayList();
        //根据班次日期分组
        Map<LocalDate, List<EpMeterReadAllDataVO>> classDateMap = thickAllDataList.stream().collect(Collectors.groupingBy(EpMeterReadAllDataVO::getReadDate));
        for (Map.Entry<LocalDate, List<EpMeterReadAllDataVO>> classDateEntry : classDateMap.entrySet()) {
            //根据班次和厚度分组
            Map<String, List<EpMeterReadAllDataVO>> thickMap = classDateEntry.getValue().stream().collect(Collectors.groupingBy(v -> v.getClassId() + "_" + v.getThick()));
            for (Map.Entry<String, List<EpMeterReadAllDataVO>> thickEntry : thickMap.entrySet()) {
                //根据组织机构id分组
                for (Long partitionId : dto.getPartitionIdList()) {
                    //基于班组分组
                    Map<Long, List<EpMeterReadAllDataVO>> classGroupMap = thickEntry.getValue().stream()
                            .filter(v -> v.getOfficeId().equals(partitionId))
                            .collect(Collectors.groupingBy(EpMeterReadAllDataVO::getClassGroupId));
                    for (Map.Entry<Long, List<EpMeterReadAllDataVO>> classGroupEntry : classGroupMap.entrySet()) {
                        String[] s = thickEntry.getKey().split("_");
                        ConsumePartitionUnitClassDetailVO rowVO = new ConsumePartitionUnitClassDetailVO();
                        rowVO.setClassDate(classDateEntry.getKey());
                        rowVO.setClassDateStr(LocalDateTimeUtil.formatNormal(classDateEntry.getKey()));
                        if (!"null".equals(s[1])) {
                            rowVO.setThick(new BigDecimal(s[1]).setScale(1));
                            if (BigDecimalUtil.equals(rowVO.getThick(), BigDecimalUtil.MINUS_ONE)) {
                                rowVO.setThickStr("无");
                            } else {
                                rowVO.setThickStr(rowVO.getThick() + "mm");
                            }
                        }
                        if (!"null".equals(s[0])) {
                            rowVO.setClassId(Integer.parseInt(s[0]));
                            QtManagerConstant.QtClassesEnum code = QtManagerConstant.QtClassesEnum.code(rowVO.getClassId());
                            rowVO.setClassIdStr(Objects.nonNull(code) ? code.getName() : null);
                        }
                        List<BigDecimal> consumeList = classGroupEntry.getValue().stream().filter(v -> Objects.nonNull(v.getConsume())).map(v -> v.getConsume()).collect(Collectors.toList());
                        BigDecimal consume = BigDecimalUtil.sum(consumeList);
                        List<BigDecimal> productNumList = classGroupEntry.getValue().stream().filter(v -> Objects.nonNull(v.getProductNum())).map(v -> v.getProductNum()).collect(Collectors.toList());
                        BigDecimal productNum = BigDecimalUtil.sum(productNumList);
                        rowVO.setUnitValueStr(BigDecimalUtil.format(BigDecimalUtil.divide(consume, productNum)) + unitName);
                        EpPartition epPartition = partitionEntityMap.getOrDefault(partitionId, defaultEpPartition);
                        rowVO.setPartitionName(epPartition.getOfficeName());
                        rowVO.setSort(Objects.isNull(epPartition.getSort()) ? Integer.MAX_VALUE : epPartition.getSort());
                        rowVO.setClassGroupName(dictIdNameMap.get(classGroupEntry.getKey()));
                        resList.add(rowVO);
                    }
                }
            }
        }
        //行排序规则为
        //1级排序：按班次日期升序排列
        //2级排序：班次，白班在上、夜班在下、“无”在最下
        //3级排序：按产品厚度升序排列，“无”在最下
        detailComparator(resList);
        //图例过滤
//        if (Objects.nonNull(dto.getFilters())) {
//            resList = resList.stream().filter(v -> dto.getFilters().contains(v.getThickStr())).collect(Collectors.toList());
//        }
        return LogicalPageUtil.page(resList, dto.getCurrent(), dto.getSize());
    }

    /**
     * 获取单耗的单位
     * @param energyType 能源类型
     * @return
     */
    private String unitName(Integer energyType){
        EpEnergyType epEnergyType = epEnergyTypeService.getById(energyType);
        Assert.notNull(epEnergyType,"能源类型不存在");
        Long measureUnit = epEnergyType.getMeasureUnit();
        Map<Long, String> measureUnitMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(Sets.newHashSet(measureUnit)),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        return measureUnitMap.getOrDefault(measureUnit,Strings.EMPTY);
    }

    @Override
    public EpChartGraphVO<EpChartGraphXAxisVO, BigDecimal> totalConsumePartitionLine(EpTotalConsumeDTO dto) {
        //1.列出在区间 [起始日期8:00:00, 终止日期+1 8:00:00) 内的所有班次集合A
        //①区间 [起始日期8:00:00, 终止日期+1 8:00:00)
        LocalDateTime start = LocalDateTime.of(dto.getStart(), ClassUtil.MORNING_START_TIME);
        LocalDateTime end = LocalDateTime.of(dto.getEnd().plusDays(1), ClassUtil.MORNING_START_TIME.minusNanos(1));
        //②时间区间内所有班次集合
        List<TupleDTO<LocalDate, Integer>> tupleDTOS = ClassUtil.classRange(start, end);
        //获取全量数据表
        Set<LocalDate> dateSet = tupleDTOS.stream().map(v -> v.getField1()).collect(Collectors.toSet());

        //根据能耗类型查询能耗分区树根节点
        Map<Integer, EpPartition> rootMap = epPartitionService.rootMap(dto.getEnergyType());
        SteccnTableAssertUtil.notEmpty(rootMap.entrySet(), "暂未配置能耗分区树");
        List<EpMeterReadAllDataVO> allDataList = Lists.newArrayList();

        //查询能耗
        List<EpEnergyType> epEnergyTypes = epEnergyTypeService.listByIds(dto.getEnergyType());
        Map<Integer, EpEnergyType> energyMap = Maps.newHashMap();
        epEnergyTypes.forEach(ele -> energyMap.put(ele.getId(), ele));


        //根据能源类型，获取全量数据表
        for (Integer energy : dto.getEnergyType()) {
            if (rootMap.containsKey(energy)) {
                try {
                    List<EpMeterReadAllDataVO> classDateAllData = epMeterReadService.classDateAllData(dateSet, energy, Lists.newArrayList(rootMap.get(energy).getId()));
                    if (CollUtil.isNotEmpty(classDateAllData)) {
                        allDataList.addAll(classDateAllData);
                    }
                } catch (Exception e) {
                    log.error("{}", e.getLocalizedMessage());
                }
            }
        }
        Set<Long> collect = rootMap.values().stream().map(EpPartition::getId).collect(Collectors.toSet());

        allDataList = allDataList.stream().filter(v -> collect.contains(v.getOfficeId())).collect(Collectors.toList());

        SteccnTableAssertUtil.notEmpty(allDataList, "全量数据表为空");
        //根据时间粒度分区
        CollUtil.sort(dateSet, Comparator.comparing(v -> v));
        EpChartGraphDTO<Long, String, BigDecimal> epChartGraphDTO = new EpChartGraphDTO<>();
        List<EpChartGraphxAxisDTO<Long, String, BigDecimal>> dataList = Lists.newArrayList();

        //时间粒度
        QtManagerConstant.QtGranularityEnum byCode = QtManagerConstant.QtGranularityEnum.getByCode(dto.getGranularity());
        List<LocalDateTime> shardDateTime = ClassUtil.cutDateRange(byCode.getType(), start, end);
        Assert.notEmpty(shardDateTime, "时间分片为空");

        for (int i = 1; i < shardDateTime.size(); i++) {
            List<EpMeterReadAllDataVO> filterDataList = null;
            LocalDateTime dataStart = shardDateTime.get(i - 1);
            LocalDateTime dataEnd = shardDateTime.get(i);
            EpChartGraphxAxisDTO<Long, String, BigDecimal> epChartGraphxAxisDTO = new EpChartGraphxAxisDTO<>();
            if (!QtManagerConstant.QtGranularityEnum.NONE.getCode().equals(byCode.getCode())) {
                epChartGraphxAxisDTO.setXAxis(XAxisUtil.convert(byCode, dataStart));
                filterDataList = allDataList.stream()
                        .filter(v -> !LocalDateTime.of(v.getReadDate(), ClassUtil.MORNING_START_TIME).isBefore(dataStart)
                                && LocalDateTime.of(v.getReadDate(), ClassUtil.MORNING_START_TIME).isBefore(dataEnd.plusDays(1))).collect(Collectors.toList());
            }
            List<TupleDTO<Long, List<TupleDTO<String, BigDecimal>>>> data = Lists.newArrayList();

            List<TupleDTO<String, BigDecimal>> poleList = Lists.newArrayList();
            TupleDTO<Long, List<TupleDTO<String, BigDecimal>>> pole = new TupleDTO<>();
            pole.setField1(1L);
            pole.setField2(poleList);

            //遍历所有的能源
            for (Integer energy : dto.getEnergyType()) {
                if (rootMap.containsKey(energy)) {
                    //根据分区节点去汇总所有能耗
                    Long partitionId = rootMap.get(energy).getId();
                    List<EpMeterReadAllDataVO> dataSet = filterDataList.stream().filter(v -> Objects.nonNull(partitionId) && partitionId.equals(v.getOfficeId())).collect(Collectors.toList());
                    List<BigDecimal> reduceList = dataSet.stream().filter(v -> Objects.nonNull(v.getConsume())).map(v -> v.getConsume()).collect(Collectors.toList());
                    BigDecimal reduce = BigDecimalUtil.sum(reduceList);
                    String name = energyMap.get(energy).getName();
                    TupleDTO<String, BigDecimal> stack = new TupleDTO<>(name, reduce);
                    poleList.add(stack);
                }
            }
            data.add(pole);
            epChartGraphxAxisDTO.setData(data);
            dataList.add(epChartGraphxAxisDTO);
        }
        epChartGraphDTO.setOfficeMap(Maps.newHashMap());
        epChartGraphDTO.setDataList(dataList);
        EpChartGraphVO<EpChartGraphXAxisVO, BigDecimal> convert = EpChartGraphUtil.convert(epChartGraphDTO, Boolean.TRUE);
        convert.getXData().setData2(Lists.newArrayList());
        return convert;
    }

    @Override
    public List<EpEnergyChartPieValueVO<BigDecimal>> totalConsumeEnergyPie(EpTableDTO dto) {
        //1.列出在区间 [起始日期8:00:00, 终止日期+1 8:00:00) 内的所有班次集合A
        //①区间 [起始日期8:00:00, 终止日期+1 8:00:00)
        LocalDateTime start = LocalDateTime.of(dto.getStart(), ClassUtil.MORNING_START_TIME);
        LocalDateTime end = LocalDateTime.of(dto.getEnd().plusDays(1), ClassUtil.MORNING_START_TIME.minusNanos(1));
        //②时间区间内所有班次集合
        List<TupleDTO<LocalDate, Integer>> tupleDTOS = ClassUtil.classRange(start, end);
        //获取全量数据表
        Set<LocalDate> dateSet = tupleDTOS.stream().map(v -> v.getField1()).collect(Collectors.toSet());
        List<EpMeterReadAllDataVO> classDateAllData = epMeterReadService.classDateAllData(dateSet, dto.getEnergyType(), dto.getPartitionIdList());
        classDateAllData = classDateAllData.stream().filter(v -> dto.getPartitionIdList().contains(v.getOfficeId())).collect(Collectors.toList());

        SteccnTableAssertUtil.notEmpty(classDateAllData, "全量数据表为空");
        //获取
        Map<Long, String> partitionIdNameMap = epPartitionService.listByIdCh(dto.getPartitionIdList());
        List<EpEnergyChartPieValueVO<BigDecimal>> resList = Lists.newArrayList();
        //遍历所有的分区节点
        for (Long partitionId : dto.getPartitionIdList()) {
            //获取分区数据集合
            List<EpMeterReadAllDataVO> dataSet = classDateAllData.stream().filter(v -> Objects.nonNull(partitionId) && partitionId.equals(v.getOfficeId())).collect(Collectors.toList());
            List<BigDecimal> reduceList = dataSet.stream().filter(v -> Objects.nonNull(v.getConsume())).map(v -> v.getConsume()).collect(Collectors.toList());
            BigDecimal reduce = BigDecimalUtil.sum(reduceList);
            if (Objects.nonNull(reduce) && BigDecimalUtil.gt(reduce, BigDecimal.ZERO)) {
                EpEnergyChartPieValueVO<BigDecimal> vo = new EpEnergyChartPieValueVO<>();
                vo.setName(partitionIdNameMap.get(partitionId));
                vo.setValue(reduce);
                resList.add(vo);
            }

        }
        //降序排序
        resList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        return resList;
    }

    @Override
    public EpChartGraphVO<EpChartGraphXAxisVO, BigDecimal> consumePartitionUnitHistoryChart(EpTableDTO dto) {
        LocalDateTime start = LocalDateTime.of(dto.getStart(), ClassUtil.MORNING_START_TIME);
        LocalDateTime end = LocalDateTime.of(dto.getEnd().plusDays(1), ClassUtil.MORNING_START_TIME.minusNanos(1));
        //②时间区间内所有班次集合
        List<TupleDTO<LocalDate, Integer>> tupleDTOS = ClassUtil.classRange(start, end);
        //获取全量数据表
        Set<LocalDate> dateSet = tupleDTOS.stream().map(v -> v.getField1()).collect(Collectors.toSet());
        List<EpMeterReadAllDataVO> classDateAllData = epMeterReadService.classDateAllDataUnit(dateSet, dto.getEnergyType(), dto.getPartitionIdList());
        SteccnTableAssertUtil.notEmpty(classDateAllData, "全量数据表为空");

        LocalDateTime historyStart = LocalDateTime.of(dto.getHistoryStart(), ClassUtil.MORNING_START_TIME);
        LocalDateTime historyEnd = LocalDateTime.of(dto.getHistoryEnd().plusDays(1), ClassUtil.MORNING_START_TIME.minusNanos(1));
        //②时间区间内所有班次集合
        List<TupleDTO<LocalDate, Integer>> historyTupleDTOS = ClassUtil.classRange(historyStart, historyEnd);
        //获取全量数据表
        Set<LocalDate> historyDateSet = historyTupleDTOS.stream().map(v -> v.getField1()).collect(Collectors.toSet());
        List<EpMeterReadAllDataVO> historyClassDateAllData = epMeterReadService.classDateAllDataUnit(historyDateSet, dto.getEnergyType(), dto.getPartitionIdList());

        classDateAllData = classDateAllData.stream().filter(v -> dto.getPartitionIdList().contains(v.getOfficeId())).collect(Collectors.toList());
        SteccnTableAssertUtil.notEmpty(historyClassDateAllData, "历史全量数据表为空");


        List<String> timeList = Lists.newArrayList();
        timeList.add(dto.getStart() + "-" + dto.getEnd());
        timeList.add(dto.getHistoryStart() + "-" + dto.getHistoryEnd());
        List<List<EpMeterReadAllDataVO>> historyContrastList = Lists.newArrayList(classDateAllData, historyClassDateAllData);
        EpChartGraphDTO<Long, String, BigDecimal> epChartGraphDTO = new EpChartGraphDTO<>();
        List<EpChartGraphxAxisDTO<Long, String, BigDecimal>> dataList = Lists.newArrayList();
        Map<Long, String> officeMap = Maps.newHashMap();

        //查询这两个时间段内是否有有效期内的部门单耗
        List<StandardVO<BigDecimal>> leftStandardSource = epProductUnitConsumeStandardService.validStandardList(dto.getEnergyType(), dto.getStart(), dto.getEnd());
        List<StandardVO<BigDecimal>> rightStandardSource = epProductUnitConsumeStandardService.validStandardList(dto.getEnergyType(), dto.getHistoryStart(), dto.getHistoryEnd());

        SysDictVO sysDictVO = FeignOptional.of(() -> remoteDictFeignApi.getByCode("departmental_standard", "product_unit_consumption_standard"),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(sysDictVO, "部门标准数据字典已被删除");
        List<StandardVO<BigDecimal>> leftStandard = leftStandardSource.stream().filter(v -> v.getStandardType().equals(sysDictVO.getId())).collect(Collectors.toList());
        List<StandardVO<BigDecimal>> rightStandard = rightStandardSource.stream().filter(v -> v.getStandardType().equals(sysDictVO.getId())).collect(Collectors.toList());
        //条件1：部门标准的有效期覆盖选中的日期范围
        boolean flag = Boolean.TRUE;
        if ((leftStandard.size() > 1 || rightStandard.size() > 1) || (CollUtil.isEmpty(leftStandard) || CollUtil.isEmpty(rightStandard))) {
            flag = Boolean.FALSE;
        }
        List<List<StandardVO<BigDecimal>>> standardList = Lists.newArrayList(leftStandard, rightStandard);

        //遍历2个时间段的集合
        for (int i = 0; i < historyContrastList.size(); i++) {
            List<EpMeterReadAllDataVO> dataVOList = historyContrastList.get(i);
            EpChartGraphxAxisDTO<Long, String, BigDecimal> epChartGraphxAxisDTO = new EpChartGraphxAxisDTO<>();
            epChartGraphxAxisDTO.setXAxis(timeList.get(i));
            List<TupleDTO<Long, List<TupleDTO<String, BigDecimal>>>> data = Lists.newArrayList();
            List<TupleDTO<String, BigDecimal>> poleList = Lists.newArrayList();
            TupleDTO<Long, List<TupleDTO<String, BigDecimal>>> pole = new TupleDTO<>();
            pole.setField1(Long.valueOf(i));
            pole.setField2(poleList);

            if (flag) {
                List<StandardVO<BigDecimal>> standardVO = standardList.get(i);
                StandardVO<BigDecimal> currentStandard = standardVO.get(0);
                BigDecimal standard = Objects.nonNull(currentStandard) ? currentStandard.getValue() : BigDecimal.ZERO;
                TupleDTO<String, BigDecimal> stack = new TupleDTO<>("部门标准", standard);
                poleList.add(stack);
            }
            List<BigDecimal> consumeList = dataVOList.stream().filter(v -> Objects.nonNull(v.getConsume())).map(v -> v.getConsume()).collect(Collectors.toList());
            BigDecimal consume = BigDecimalUtil.sum(consumeList);
            List<BigDecimal> productNumList = dataVOList.stream().filter(v -> Objects.nonNull(v.getProductNum())).map(v -> v.getProductNum()).collect(Collectors.toList());
            BigDecimal productNum = BigDecimalUtil.sum(productNumList);
            TupleDTO<String, BigDecimal> stack = new TupleDTO<>("实际单耗", BigDecimalUtil.divide(consume, productNum));
            poleList.add(stack);
            data.add(pole);
            epChartGraphxAxisDTO.setData(data);
            dataList.add(epChartGraphxAxisDTO);
            officeMap.put(Long.valueOf(i), timeList.get(i));
        }
        epChartGraphDTO.setOfficeMap(officeMap);
        epChartGraphDTO.setDataList(dataList);
        List<StandardVO<BigDecimal>> left = leftStandardSource.stream().filter(v -> !v.getStandardType().equals(sysDictVO.getId())).collect(Collectors.toList());
        List<StandardVO<BigDecimal>> right = rightStandardSource.stream().filter(v -> !v.getStandardType().equals(sysDictVO.getId())).collect(Collectors.toList());
        left.addAll(right);

        //考虑过滤
        //查询节点是否包含有基片和深加工
        Map<Long, String> partitionMap = epPartitionService.listByIdCh(dto.getPartitionIdList());
        Set<String> partitionNameSet = partitionMap.values().stream().collect(Collectors.toSet());
        Set<Long> typeSet = Sets.newHashSet();
        for (String name : partitionNameSet) {
            if(name.indexOf("基片") != -1){
                typeSet.add(0L);
            }else if(name.indexOf("深加工") != -1){
                typeSet.add(1L);
            }
        }

        //部门行业的单耗标准
        left = left.stream().filter(v -> {
            if (BigDecimalUtil.equals(dto.getThick().get(0), BigDecimalUtil.MINUS_ONE)) {
                return BigDecimalUtil.equals(null, v.getThick());
            }
            return BigDecimalUtil.equals(dto.getThick().get(0), v.getThick());
        }).collect(Collectors.toList());
        if(CollUtil.isNotEmpty(typeSet)){
            left = left.stream().filter(v -> typeSet.contains(v.getProductType())).collect(Collectors.toList());
        }
        epChartGraphDTO.setStandardList(left);
        EpChartGraphVO<EpChartGraphXAxisVO, BigDecimal> convert = EpChartGraphUtil.convert(epChartGraphDTO, Boolean.TRUE);
        convert.getXData().setData2(Lists.newArrayList());
        convert.getSeriesDataVO().stream().forEach(ele -> ele.setStack(null));
        return convert;
    }

    @Override
    public Page<ConsumeHistoryContrastDetailVO> consumePartitionUnitHistoryDetail(EpTableDTO dto) {
        LocalDateTime start = LocalDateTime.of(dto.getStart(), ClassUtil.MORNING_START_TIME);
        LocalDateTime end = LocalDateTime.of(dto.getEnd().plusDays(1), ClassUtil.MORNING_START_TIME.minusNanos(1));
        //②时间区间内所有班次集合
        List<TupleDTO<LocalDate, Integer>> tupleDTOS = ClassUtil.classRange(start, end);
        //获取全量数据表
        Set<LocalDate> dateSet = tupleDTOS.stream().map(v -> v.getField1()).collect(Collectors.toSet());
        List<EpMeterReadAllDataVO> classDateAllData = epMeterReadService.classDateAllDataUnit(dateSet, dto.getEnergyType(), dto.getPartitionIdList());
        LocalDateTime historyStart = LocalDateTime.of(dto.getStart(), ClassUtil.MORNING_START_TIME);
        LocalDateTime historyEnd = LocalDateTime.of(dto.getEnd().plusDays(1), ClassUtil.MORNING_START_TIME.minusNanos(1));
        //②时间区间内所有班次集合
        List<TupleDTO<LocalDate, Integer>> historyTupleDTOS = ClassUtil.classRange(historyStart, historyEnd);
        //获取全量数据表
        Set<LocalDate> historyDateSet = historyTupleDTOS.stream().map(v -> v.getField1()).collect(Collectors.toSet());
        List<EpMeterReadAllDataVO> historyClassDateAllData = epMeterReadService.classDateAllDataUnit(historyDateSet, dto.getEnergyType(), dto.getPartitionIdList());

        Set<EpMeterReadAllDataVO> allDataSet = Sets.newHashSet();
        allDataSet.addAll(classDateAllData);
        allDataSet.addAll(historyClassDateAllData);
        classDateAllData = Lists.newArrayList(allDataSet);
        classDateAllData = classDateAllData.stream().filter(v -> dto.getPartitionIdList().contains(v.getOfficeId())).collect(Collectors.toList());
        SteccnTableAssertUtil.notEmpty(classDateAllData, "全量数据表为空");
        //时间排序
        CollUtil.sort(dateSet, Comparator.comparing(v -> v));
        //定义返回集合
        List<ConsumeHistoryContrastDetailVO> resList = Lists.newArrayList();
        Map<LocalDate, List<EpMeterReadAllDataVO>> classDateMap = classDateAllData.stream().collect(Collectors.groupingBy(EpMeterReadAllDataVO::getReadDate));
        for (Map.Entry<LocalDate, List<EpMeterReadAllDataVO>> classDateEntry : classDateMap.entrySet()) {
            Map<String, List<EpMeterReadAllDataVO>> thickMap = classDateEntry.getValue().stream().collect(Collectors.groupingBy(v -> v.getClassId() + "_" + v.getThick()));
            for (Map.Entry<String, List<EpMeterReadAllDataVO>> thickEntry : thickMap.entrySet()) {
                String[] s = thickEntry.getKey().split("_");
                ConsumeHistoryContrastDetailVO rowVO = new ConsumeHistoryContrastDetailVO();
                rowVO.setClassDate(classDateEntry.getKey());
                rowVO.setClassDateStr(LocalDateTimeUtil.formatNormal(classDateEntry.getKey()));
                if (!"null".equals(s[1])) {
                    rowVO.setThick(new BigDecimal(s[1]).setScale(1));
                    rowVO.setThickStr(rowVO.getThick() + "mm");
                    if(BigDecimalUtil.equals(rowVO.getThick(),BigDecimalUtil.MINUS_ONE)){
                        rowVO.setThickStr("无");
                    }
                }
                if (!"null".equals(s[0])) {
                    rowVO.setClassId(Integer.parseInt(s[0]));
                    QtManagerConstant.QtClassesEnum code = QtManagerConstant.QtClassesEnum.code(rowVO.getClassId());
                    rowVO.setClassIdStr(Objects.nonNull(code) ? code.getName() : null);
                }
                List<BigDecimal> consumeList = thickEntry.getValue().stream().filter(v -> Objects.nonNull(v.getConsume())).map(v -> v.getConsume()).collect(Collectors.toList());
                BigDecimal consume = BigDecimalUtil.sum(consumeList);
                List<BigDecimal> productNumList = thickEntry.getValue().stream().filter(v -> Objects.nonNull(v.getProductNum())).map(v -> v.getProductNum()).collect(Collectors.toList());
                BigDecimal productNum = BigDecimalUtil.sum(productNumList);
                BigDecimal divide = BigDecimalUtil.divide(consume, productNum);
                rowVO.setConsumeStr(BigDecimalUtil.format(divide) + unitName(dto.getEnergyType()));
                resList.add(rowVO);
            }
        }
        //行排序规则为
        //1级排序：按班次日期升序排列
        //2级排序：班次，白班在上、夜班在下、“无”在最下
        //3级排序：按产品厚度升序排列，“无”在最下
        detailComparator(resList);

        //图例过滤
        if (Objects.nonNull(dto.getFilters())) {
            resList = resList.stream().filter(v -> dto.getFilters().contains(v.getThickStr())).collect(Collectors.toList());
        }
        return LogicalPageUtil.page(resList, dto.getCurrent(), dto.getSize());
    }

    @Override
    public EpChartGraphVO<EpChartGraphXAxisVO, BigDecimal> unitConsumePartitionContrastChart(EpTableDTO dto) {
        //1.列出在区间 [起始日期8:00:00, 终止日期+1 8:00:00) 内的所有班次集合A
        //①区间 [起始日期8:00:00, 终止日期+1 8:00:00)
        LocalDateTime start = LocalDateTime.of(dto.getStart(), ClassUtil.MORNING_START_TIME);
        LocalDateTime end = LocalDateTime.of(dto.getEnd().plusDays(1), ClassUtil.MORNING_START_TIME.minusNanos(1));
        //②时间区间内所有班次集合
        List<TupleDTO<LocalDate, Integer>> tupleDTOS = ClassUtil.classRange(start, end);
        //获取全量数据表
        Set<LocalDate> dateSet = tupleDTOS.stream().map(v -> v.getField1()).collect(Collectors.toSet());
        List<EpMeterReadAllDataVO> classDateAllData ;
        if(dto.getEnergyType() == 1){
            classDateAllData = epMeterReadService.classDateAllDataUnit(dateSet, dto.getEnergyType(), dto.getPartitionIdList());
        } else if (dto.getEnergyType() != 5) {
            classDateAllData = epMeterReadService.classDateAllDataUnitOther(dateSet, dto.getEnergyType(), dto.getPartitionIdList());
        } else {
            //燃料(气+油)的单耗
            classDateAllData = epMeterReadService.kcalDataUnit(dateSet, dto.getPartitionIdList());
        }
        SteccnTableAssertUtil.notEmpty(classDateAllData, "全量数据表为空");
        List<EpMeterReadAllDataVO> classDateFilterData = new ArrayList<>();

        List<EpProductUnitConsumeStandard> consumeStandardList = new ArrayList<>();
        List<StandardVO<BigDecimal>> standardVOList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(dto.getThick())) {
            //产品厚度
            String thick = dto.getThick().get(0) + "mm";
            if (BigDecimalUtil.equals(dto.getThick().get(0),BigDecimal.valueOf(2)) || BigDecimalUtil.equals(dto.getThick().get(0),BigDecimal.valueOf(4))) {
                thick = dto.getThick().get(0) + ".0mm";
            }
            String finalThick = thick;
            SysDictVO sysDictVO = FeignOptional.of(() -> remoteDictFeignApi.getByCode(finalThick, "product_thickness_range"), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

            //获取部门标准
            consumeStandardList = getDepartmentStandardList(dto, sysDictVO);
            //企业行业标准
            standardVOList = getStandardVOList(dto, sysDictVO);
        }


        //根据时间粒度分区
        CollUtil.sort(dateSet, Comparator.comparing(v -> v));
        EpChartGraphDTO<Long, String, BigDecimal> epChartGraphDTO = new EpChartGraphDTO<>();
        List<EpChartGraphxAxisDTO<Long, String, BigDecimal>> dataList = Lists.newArrayList();
        //时间粒度
        QtManagerConstant.QtGranularityEnum granularityEnum = QtManagerConstant.QtGranularityEnum.getByCode(dto.getGranularity());
        Assert.notNull(granularityEnum, "粒度不存在");
        //获取
        Map<Long, String> partitionIdNameMap = epPartitionService.listByIdCh(dto.getPartitionIdList());

        // 班次分组
        if (QtManagerConstant.QtGranularityEnum.CLASS.equals(granularityEnum)) {
            for (int i = 0; i < 2; i++) {
                EpChartGraphxAxisDTO<Long, String, BigDecimal> epChartGraphxAxisDTO = new EpChartGraphxAxisDTO<>();
                if (i == 0) {
                    epChartGraphxAxisDTO.setXAxis("早班");
                } else {
                    epChartGraphxAxisDTO.setXAxis("晚班");
                }
                int finalI = i;
                classDateFilterData = classDateAllData.stream().filter(data -> data.getClassId().equals(finalI)).collect(Collectors.toList());
                List<TupleDTO<Long, List<TupleDTO<String, BigDecimal>>>> tupleDTOList = getTupleDTOList(dto, classDateFilterData, consumeStandardList);
                epChartGraphxAxisDTO.setData(tupleDTOList);
                dataList.add(epChartGraphxAxisDTO);
            }
        } else {
            List<LocalDate> timeList = ShardTimeUtil.cutDate(granularityEnum.getType(), dto.getStart(), dto.getEnd());
            //其他类型分组
            for (int i = 1; i < timeList.size(); i++) {
                LocalDate dateStart = timeList.get(i - 1);
                LocalDate dateEnd = timeList.get(i);
                EpChartGraphxAxisDTO<Long, String, BigDecimal> epChartGraphxAxisDTO = new EpChartGraphxAxisDTO<>();
                epChartGraphxAxisDTO.setXAxis(XAxisUtil.convert(granularityEnum, dateStart));
                classDateFilterData = classDateAllData.stream().filter(v -> LocalDateUtil.isRange(v.getReadDate(), dateStart, dateEnd, true, false)).collect(Collectors.toList());
                List<TupleDTO<Long, List<TupleDTO<String, BigDecimal>>>> data = getTupleDTOList(dto, classDateFilterData, consumeStandardList);
                epChartGraphxAxisDTO.setData(data);
                dataList.add(epChartGraphxAxisDTO);
            }
        }

        epChartGraphDTO.setOfficeMap(partitionIdNameMap);
        epChartGraphDTO.setDataList(dataList);
        epChartGraphDTO.setStandardList(standardVOList);
        epChartGraphDTO.setCheckStandardMap(getDpStandardList(dto.getEnergyType(), consumeStandardList));
        epChartGraphDTO.setEnergyType(dto.getEnergyType());
        EpChartGraphVO<EpChartGraphXAxisVO, BigDecimal> convert = EpChartGraphUtil.convert(epChartGraphDTO, Boolean.TRUE);
        if (QtManagerConstant.QtGranularityEnum.NONE.equals(granularityEnum)) {
            convert.getXData().setData(Lists.newArrayList());
        }
        return convert;
    }

    /**
     * 获取部门标准map
     * @param energyType 能源类型
     * @param consumeStandardList 部门单耗标准list
     * @return
     */
    private Map<Long,TupleDTO<Integer,BigDecimal>>  getDpStandardList(Integer energyType, List<EpProductUnitConsumeStandard> consumeStandardList) {
        Map<Long,TupleDTO<Integer,BigDecimal>> dpStandardMap = new HashMap<>();
        if (CollUtil.isNotEmpty(consumeStandardList)) {
            //获取整个树结构
            Set<Integer> officeIdSet = consumeStandardList.stream().map(v -> v.getApplyOffice()).flatMap(Collection::stream).collect(Collectors.toSet());
            Map<Integer, EpInstrumentAccount> partitionMap = epPartitionService.listByOfficeId(energyType, officeIdSet);
            for (EpProductUnitConsumeStandard consumeStandard : consumeStandardList) {
                consumeStandard.getApplyOffice().forEach(ele -> {
                    EpInstrumentAccount account = partitionMap.get(ele);
                    if (Objects.nonNull(account)) {
                        dpStandardMap.put(account.getPartitionNode(), new TupleDTO<>(consumeStandard.getId(), consumeStandard.getUnitValue()));
                    }
                });
            }
        }
        return dpStandardMap;
    }

    @Override
    public EpConsumeDetailVO unitConsumePartitionContrastDetail(EpTableDTO dto) {
        //1.列出在区间 [起始日期8:00:00, 终止日期+1 8:00:00) 内的所有班次集合A
        //①区间 [起始日期8:00:00, 终止日期+1 8:00:00)
        LocalDateTime start = LocalDateTime.of(dto.getStart(), ClassUtil.MORNING_START_TIME);
        LocalDateTime end = LocalDateTime.of(dto.getEnd().plusDays(1), ClassUtil.MORNING_START_TIME.minusNanos(1));
        //②时间区间内所有班次集合
        List<TupleDTO<LocalDate, Integer>> tupleDTOS = ClassUtil.classRange(start, end);
        //获取全量数据表
        Set<LocalDate> dateSet = tupleDTOS.stream().map(v -> v.getField1()).collect(Collectors.toSet());
        List<EpMeterReadAllDataVO> classDateAllData = null;
        if (dto.getEnergyType() == 1) {
            classDateAllData = epMeterReadService.classDateAllDataUnit(dateSet, dto.getEnergyType(), dto.getPartitionIdList());
        } else if (dto.getEnergyType() != 5) {
            classDateAllData = epMeterReadService.classDateAllDataUnitOther(dateSet, dto.getEnergyType(), dto.getPartitionIdList());
        } else {
            //燃料(气+油)的单耗
            classDateAllData = epMeterReadService.kcalDataUnit(dateSet, dto.getPartitionIdList());
        }
        classDateAllData = classDateAllData.stream().filter(v -> BigDecimalUtil.equals(dto.getThick().get(0), v.getThick())).collect(Collectors.toList());
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
                BigDecimal thick = BigDecimalUtil.valueOf(s[1]);
                if (!noThick(thick)) {
                    rowVO.setThick(thick);
                    rowVO.setThickStr(String.valueOf(rowVO.getThick()));
                } else {
                    rowVO.setThick(new BigDecimal("-1"));
                    rowVO.setThickStr("无");
                }
                if (!"null".equals(s[0])) {
                    rowVO.setClassId(Integer.parseInt(s[0]));
                    QtManagerConstant.QtClassesEnum code = QtManagerConstant.QtClassesEnum.code(rowVO.getClassId());
                    rowVO.setClassIdStr(Objects.nonNull(code) ? code.getName() : null);
                }

                List<String> rowList = Lists.newArrayList();
                for (Map.Entry<Long, String> partitionEntry : partitionIdNameMap.entrySet()) {
                    Optional<EpMeterReadAllDataVO> optional = thickEntry.getValue().stream().filter(epMeterReadAllDataVO -> Objects.equals(partitionEntry.getKey(), epMeterReadAllDataVO.getOfficeId())).findAny();
                    if (optional.isPresent()) {
                        BigDecimal consume = BigDecimalUtil.divide(optional.get().getConsume(), optional.get().getProductNum());
                        rowList.add(BigDecimalUtil.format(consume));
                    } else {
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
        detailComparator(resList);

        //图例过滤
        if (Objects.nonNull(dto.getFilters())) {
            resList = resList.stream().filter(v -> dto.getFilters().contains(v.getThickStr())).collect(Collectors.toList());
        }

        Page<EpConsumeDetailRowVO> page = LogicalPageUtil.page(resList, dto.getCurrent(), dto.getSize());
        resVo.setDataList(page);
        resVo.setHeadData(headData);
        return resVo;
    }

    @SneakyThrows
    @Override
    public void unitContrastDownload(EpTableDTO dto, HttpServletResponse response) {
        dto.setCurrent(1);
        dto.setSize(999999);
        EpConsumeDetailVO detail = unitConsumePartitionContrastDetail(dto);
        EpEnergyType energyType = epEnergyTypeService.getOne(new LambdaQueryWrapper<EpEnergyType>().eq(EpEnergyType::getId, dto.getEnergyType()), false);
        Map<String, Object> map = MapUtils.newHashMap();
        map.put("energyType", energyType.getName());
        map.put("dateStart", LocalDateUtil.localDateFormat(dto.getStart(), DatePattern.NORM_DATE_PATTERN));
        map.put("dateEnd", LocalDateUtil.localDateFormat(dto.getEnd(), DatePattern.NORM_DATE_PATTERN));
        if (CollUtil.isNotEmpty(dto.getThick())) {
            StringBuilder sb = new StringBuilder();
            dto.getThick().forEach(tmp -> {
                sb.append(tmp.toString() + "mm ");
            });
            map.put("thick", sb.toString());
        } else {
            map.put("thick", "无");
        }
        String modelName;
        if (dto.getEnergyType().equals(5)) {
            modelName = EpManagerConstant.EP_DOWNLOAD_MODEL_NAME_DHFQDBRL;
        } else {
            modelName = EpManagerConstant.EP_DOWNLOAD_MODEL_NAME_DHFQDBW;
        }
        map.put("granularity", QtManagerConstant.QtGranularityEnum.getByCode(dto.getGranularity()).getName());
        String excelName = "单耗分区对比(" + energyType.getName() + ")_"
                + LocalDateUtil.localDateFormat(dto.getStart(), DatePattern.NORM_DATE_PATTERN) + "_"
                + LocalDateUtil.localDateFormat(dto.getEnd(), DatePattern.NORM_DATE_PATTERN);
        DownloadUtil.downloadEnergyTemplate(response, modelName, excelName, map, detail, dto.getBase64());
    }

    @Override
    @SneakyThrows
    public void unitClassDownload(EpTableDTO dto, HttpServletResponse response) {
        dto.setCurrent(1);
        dto.setSize(999999);
        Page<ConsumePartitionUnitClassDetailVO> detailPage = consumePartitionUnitClassDetail(dto);
        EpEnergyType energyType = epEnergyTypeService.getOne(new LambdaQueryWrapper<EpEnergyType>().eq(EpEnergyType::getId, dto.getEnergyType()), false);
        Map<String, Object> map = MapUtils.newHashMap();
        map.put("energyType", energyType.getName());
        map.put("dateStart", LocalDateUtil.localDateFormat(dto.getStart(), DatePattern.NORM_DATE_PATTERN));
        map.put("dateEnd", LocalDateUtil.localDateFormat(dto.getEnd(), DatePattern.NORM_DATE_PATTERN));
        if (CollUtil.isNotEmpty(dto.getThick())) {
            StringBuilder sb = new StringBuilder();
            dto.getThick().forEach(tmp -> {
                sb.append(tmp.toString() + "mm ");
            });
            map.put("thick", sb.toString());
        } else {
            map.put("thick", "无");
        }
        map.put("granularity", QtManagerConstant.QtGranularityEnum.getByCode(dto.getGranularity()).getName());
        String excelName = "单耗班组对比(" + energyType.getName() + ")_"
                + LocalDateUtil.localDateFormat(dto.getStart(), DatePattern.NORM_DATE_PATTERN) + "_"
                + LocalDateUtil.localDateFormat(dto.getEnd(), DatePattern.NORM_DATE_PATTERN);
        DownloadUtil.downloadTemplate(response, EpManagerConstant.EP_DOWNLOAD_MODEL_NAME_DHBZDB, excelName, map, detailPage.getRecords(), dto.getBase64());
    }

    @Override
    @SneakyThrows
    public void unitHistoryDownload(EpTableDTO dto, HttpServletResponse response) {
        dto.setCurrent(1);
        dto.setSize(999999);
        Page<ConsumeHistoryContrastDetailVO> detailPage = consumePartitionUnitHistoryDetail(dto);
        EpEnergyType energyType = epEnergyTypeService.getOne(new LambdaQueryWrapper<EpEnergyType>().eq(EpEnergyType::getId, dto.getEnergyType()), false);
        Map<String, Object> map = MapUtils.newHashMap();
        map.put("energyType", energyType.getName());
        map.put("dateStart", LocalDateUtil.localDateFormat(dto.getStart(), DatePattern.NORM_DATE_PATTERN));
        map.put("dateEnd", LocalDateUtil.localDateFormat(dto.getEnd(), DatePattern.NORM_DATE_PATTERN));
        map.put("historyDateStart", LocalDateUtil.localDateFormat(dto.getHistoryStart(), DatePattern.NORM_DATE_PATTERN));
        map.put("historyDateEnd", LocalDateUtil.localDateFormat(dto.getHistoryEnd(), DatePattern.NORM_DATE_PATTERN));

        EpPartition byId = epPartitionService.getById(dto.getPartitionIdList().get(0));
        map.put("aa", Objects.nonNull(byId) ? byId.getOfficeName() : "单耗");
        if (CollUtil.isNotEmpty(dto.getThick())) {
            StringBuilder sb = new StringBuilder();
            dto.getThick().forEach(tmp -> {
                sb.append(tmp.toString() + "mm ");
            });
            map.put("thick", sb.toString());
        } else {
            map.put("thick", "无");
        }
        String excelName = "单耗历史对比(" + energyType.getName() + ")_"
                + LocalDateUtil.localDateFormat(dto.getStart(), DatePattern.NORM_DATE_PATTERN) + "_"
                + LocalDateUtil.localDateFormat(dto.getEnd(), DatePattern.NORM_DATE_PATTERN);
        DownloadUtil.downloadTemplate(response, EpManagerConstant.EP_DOWNLOAD_MODEL_NAME_DHLSDB, excelName, map, detailPage.getRecords(), dto.getBase64());

    }

    private List<StandardVO<BigDecimal>> getStandardVOList(EpTableDTO dto, SysDictVO thick) {
        List<EpPartition> partitionList = epPartitionService.listByIds(dto.getPartitionIdList());
        boolean subFlag = false;
        boolean deepFlag = false;
        for (EpPartition epPartition : partitionList) {
            if (Objects.nonNull(epPartition.getParentId())) {
                subFlag = true;
                deepFlag = true;
                break;
            }
            if (epPartition.getOfficeName().contains("基片")) {
                subFlag = true;
            }
            if (epPartition.getOfficeName().contains("深加工")) {
                deepFlag = true;
            }
        }

        List<Long> standardList = new ArrayList<>();
        //标准类型：企业标准
        SysDictVO sysDictVO = remoteDictFeignApi.getByCode("company_standard", "product_unit_consumption_standard").getData();
        //标准类型：行业标准
        SysDictVO industrialSysDictVO = remoteDictFeignApi.getByCode("industrial_standard", "product_unit_consumption_standard").getData();
        standardList.add(sysDictVO.getId());
        standardList.add(industrialSysDictVO.getId());
        List<Long> productTypeList = new ArrayList<>();
        if (subFlag) {
            productTypeList.add(EpManagerConstant.EpProductTypes.SUBSTRATE.getId());
        }
        if (deepFlag) {
            //产品类型：深加工
            productTypeList.add(EpManagerConstant.EpProductTypes.DEEP_PROCESSING.getId());
        }
        LambdaQueryWrapper<EpProductUnitConsumeStandard> standardQueryWrapper = new LambdaQueryWrapper<EpProductUnitConsumeStandard>()
                .eq(EpProductUnitConsumeStandard::getEnergyType, dto.getEnergyType())
                .le(EpProductUnitConsumeStandard::getValidStart, dto.getStart()).ge(EpProductUnitConsumeStandard::getValidEnd, dto.getEnd())
                .in(EpProductUnitConsumeStandard::getStandardType, standardList)
                .in(CollUtil.isNotEmpty(productTypeList), EpProductUnitConsumeStandard::getProductType, productTypeList);
        if (Objects.nonNull(thick)) {
            standardQueryWrapper.eq(EpProductUnitConsumeStandard::getProductThick, thick.getId());
        } else {
            standardQueryWrapper.isNull(EpProductUnitConsumeStandard::getProductThick);
        }
        List<EpProductUnitConsumeStandard> unitConsumeStandardList = epProductUnitConsumeStandardService.list(standardQueryWrapper);
        List<StandardVO<BigDecimal>> standardVOList = new ArrayList<>();
        for (EpProductUnitConsumeStandard consumeStandard : unitConsumeStandardList) {
            StandardVO<BigDecimal> standardVO = new StandardVO<>();
            BeanUtils.copyProperties(consumeStandard, standardVO);
            if (consumeStandard.getStandardType().equals(sysDictVO.getId())) {
                standardVO.setStandardTypeName("企业标准");
            } else {
                standardVO.setStandardTypeName("行业标准");
            }
            standardVO.setValue(consumeStandard.getUnitValue());
            standardVOList.add(standardVO);
        }
        return standardVOList;
    }

    private List<EpProductUnitConsumeStandard> getDepartmentStandardList(EpTableDTO dto, SysDictVO sysDictVO) {
        List<EpPartition> partitionList = epPartitionService.list(new LambdaQueryWrapper<EpPartition>().eq(EpPartition::getType, dto.getEnergyType()).in(EpPartition::getId, dto.getPartitionIdList()));
        List<Integer> partitionIdList = partitionList.stream().map(EpPartition::getOfficeId).collect(Collectors.toList());
        LambdaQueryWrapper<EpProductUnitConsumeStandard> queryWrapper = new LambdaQueryWrapper<EpProductUnitConsumeStandard>();
        queryWrapper.and(query -> {
            for (int i = 0; i < partitionIdList.size(); i++) {
                if (i == partitionIdList.size() - 1) {
                    query.like(EpProductUnitConsumeStandard::getApplyOffice, partitionIdList.get(i));
                } else {
                    query.like(EpProductUnitConsumeStandard::getApplyOffice, partitionIdList.get(i)).or();
                }
            }
        });
        queryWrapper.le(EpProductUnitConsumeStandard::getValidStart, dto.getStart()).ge(EpProductUnitConsumeStandard::getValidEnd, dto.getEnd());
        queryWrapper.eq(EpProductUnitConsumeStandard::getEnergyType, dto.getEnergyType());
        if (dto.getThick().contains(BigDecimalUtil.MINUS_ONE)) {
            queryWrapper.isNull(EpProductUnitConsumeStandard::getProductThick);
        } else if(Objects.nonNull(sysDictVO)){
            queryWrapper.eq(Objects.nonNull(sysDictVO.getId()), EpProductUnitConsumeStandard::getProductThick, sysDictVO.getId());
        }
        return epProductUnitConsumeStandardService.list(queryWrapper);
    }

    private List<TupleDTO<Long, List<TupleDTO<String, BigDecimal>>>> getTupleDTOList(EpTableDTO dto, List<EpMeterReadAllDataVO> classDateAllData, List<EpProductUnitConsumeStandard> consumeStandardList) {
        List<TupleDTO<Long, List<TupleDTO<String, BigDecimal>>>> data = Lists.newArrayList();
        List<EpPartition> partitionList = epPartitionService.list(new LambdaQueryWrapper<EpPartition>().eq(EpPartition::getType, dto.getEnergyType()).in(EpPartition::getId, dto.getPartitionIdList()));

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
                List<EpMeterReadAllDataVO> thickData;
                if (noThick(thick)) {
                    thickData = dataSet.stream().filter(v -> noThick(v.getThick())).collect(Collectors.toList());
                } else {
                    thickData = dataSet.stream().filter(v -> Objects.nonNull(v.getThick()) && thick.compareTo(v.getThick()) == 0).collect(Collectors.toList());
                }
                List<BigDecimal> sumConsumeList = thickData.stream().filter(v -> Objects.nonNull(v.getConsume())).map(EpMeterReadAllDataVO::getConsume).collect(Collectors.toList());
                BigDecimal sumConsume = BigDecimalUtil.sum(sumConsumeList);
                List<BigDecimal> sumProductNumList = thickData.stream().filter(v -> Objects.nonNull(v.getProductNum())).map(EpMeterReadAllDataVO::getProductNum).collect(Collectors.toList());
                BigDecimal sumProductNum = BigDecimalUtil.sum(sumProductNumList);
                //获取单耗值
                TupleDTO<String, BigDecimal> stack = new TupleDTO<>("实际单耗", BigDecimalUtil.divide(sumConsume, sumProductNum, 3));
                poleList.add(stack);
                //获取部门标准
                BigDecimal standard = null;
                Optional<EpPartition> partitionOptional = partitionList.stream().filter(partition -> partition.getId().equals(partitionId)).findAny();
                if (partitionOptional.isPresent()) {
                    Optional<EpProductUnitConsumeStandard> optional = consumeStandardList.stream().filter(consumestandard -> consumestandard.getApplyOffice().contains(partitionOptional.get().getOfficeId())).findAny();
                    if (optional.isPresent()) {
                        standard = optional.get().getUnitValue();
                    }
                }
                TupleDTO<String, BigDecimal> standardTupleDTO = new TupleDTO<>("部门标准", standard);
                poleList.add(standardTupleDTO);
            }

            data.add(pole);
        }

        return data;
    }

    private boolean noThick(BigDecimal thick){
        return Objects.isNull(thick) || thick.compareTo(BigDecimalUtil.MINUS_ONE) == 0;
    }

    /**
     * 根据时间范围和组织结构树，查询厚度下拉选项
     *
     * @param start           开始时间
     * @param end             结束时间
     * @param energyType      能源类型id
     * @param partitionIdList 分区节点id集合
     * @param consumeType     0能耗 1单耗
     */
    private Set<BigDecimal> thickSelect(LocalDateTime start, LocalDateTime end, Integer energyType, List<Long> partitionIdList, Integer consumeType) {
        //时间区间内所有班次集合
        List<TupleDTO<LocalDate, Integer>> tupleDTOS = ClassUtil.classRange(start, end);
        //获取全量数据表
        Set<LocalDate> dateSet = tupleDTOS.stream().map(v -> v.getField1()).collect(Collectors.toSet());
        List<EpMeterReadAllDataVO> classDateAllData ;
        if (energyType == 5) {
            //燃料油+气单耗
            classDateAllData = epMeterReadService.kcalDataUnit(dateSet, partitionIdList);
        } else if (energyType == 1 && consumeType == 2) {
            //电单耗
            classDateAllData = epMeterReadService.classDateAllDataUnit(dateSet, energyType, partitionIdList);
        } else if (energyType != 1 && consumeType == 2) {
            //其他能源的单耗
            classDateAllData = epMeterReadService.classDateAllDataUnitOther(dateSet, energyType, partitionIdList);
        } else {
            //所有能源能耗
            classDateAllData = epMeterReadService.classDateAllData(dateSet, energyType, partitionIdList);
        }
        return classDateAllData.stream().map(v -> v.getThick()).collect(Collectors.toSet());
    }
}
