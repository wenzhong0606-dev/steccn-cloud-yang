package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.*;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.util.StackGraphUtil;
import com.steccn.platform.constant.SysOfficeConstant;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.dto.ProcessReportWorkPageDTO;
import com.steccn.pms.mapper.PmsReportWorkMapper;
import com.steccn.pms.service.PmsReportWorkService;
import com.steccn.pms.service.PmsSjgReportDashboardCtService;
import com.steccn.pms.vo.LineProduceClassGroupVO;
import com.steccn.pms.vo.ProcessReportWorkPageVO;
import com.steccn.pms.vo.SjgCountReportVO;
import com.steccn.pms.vo.SjgDashboardLineProduceVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @desc 深加工车间看板接口
 * @date 2023/9/5
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PmsSjgReportDashboardCtServiceImpl implements PmsSjgReportDashboardCtService {

    private final PmsReportWorkService reportWorkService;
    private final RemoteOfficeFeign remoteOfficeFeign;
    private final RemoteDictFeignApi remoteDictFeignApi;
    private final PmsReportWorkMapper pmsReportWorkMapper;

    @Override
    public QtLineChartBarDTO lineProduce(Integer workShopId, LocalDate start, LocalDate end, String unit, Integer bit) {
        //产量：钢化工序的合格数（加工数-不良数）
        //入库：选择时间端内，该产线入库数量汇总
        //耗用：磨边工序的加工数
        List<SysOfficeVO> wsLineList = getWsLineList(workShopId);
        Assert.notNull(wsLineList, "车间下暂无产线");
        List<Integer> lineIdList = wsLineList.stream().map(SysOfficeVO::getId).collect(Collectors.toList());

        Map<Integer, BigDecimal> dateRangePmMap = reportWorkService.getDateRangePmUnit(lineIdList, start, end, 1, unit);
        Map<Integer, BigDecimal> dateRangeInStorageMap = reportWorkService.getDateRangeInStorageUnit(lineIdList, start, end, unit);
        Map<Integer, BigDecimal> dateRangeUseCountMap = reportWorkService.getDateRangeUseCountUnit(lineIdList, start, end, unit);

        //定义数据集合
        List<StackGraphDTO<String, BigDecimal>> stackGraphDTOList = Lists.newArrayList();
        wsLineList.forEach(ele -> {
            StackGraphDTO<String, BigDecimal> stackGraphDTO = new StackGraphDTO<>();
            stackGraphDTO.setXAxis(ele.getOfficeName());
            List<TupleDTO<String, BigDecimal>> dataList = Lists.newArrayList();
            //产量，入库，耗用
            dataList.add(new TupleDTO<>("产量", BigDecimalUtil.setScale(dateRangePmMap.get(ele.getId()), bit)));
            dataList.add(new TupleDTO<>("入库", BigDecimalUtil.setScale(dateRangeInStorageMap.get(ele.getId()), bit)));
            dataList.add(new TupleDTO<>("耗用", BigDecimalUtil.setScale(dateRangeUseCountMap.get(ele.getId()), bit)));
            stackGraphDTO.setData(dataList);
            stackGraphDTOList.add(stackGraphDTO);
        });
        //自定义转换工具
        return StackGraphUtil.convertData(stackGraphDTOList);
    }

    @Override
    public SjgDashboardLineProduceVO lineProduceDetail(Integer workShopId, LocalDate start, LocalDate end, String unit, Integer bit) {
        List<SysOfficeVO> wsLineList = getWsLineList(workShopId);
        Assert.notNull(wsLineList, "车间下暂无产线");
        List<Integer> lineIdList = wsLineList.stream().map(SysOfficeVO::getId).collect(Collectors.toList());
        SjgDashboardLineProduceVO resVo = new SjgDashboardLineProduceVO();
        Map<Integer, BigDecimal> dateRangePmMap = reportWorkService.getDateRangePmUnit(lineIdList, start, end, 1, unit);
        Map<Integer, BigDecimal> dateRangeInStorageMap = reportWorkService.getDateRangeInStorageUnit(lineIdList, start, end, unit);
        Map<Integer, BigDecimal> dateRangeUseCountMap = reportWorkService.getDateRangeUseCountUnit(lineIdList, start, end, unit);
        List<String> headList = Lists.newArrayList();
        List<BigDecimal> pmList = Lists.newArrayList();
        List<BigDecimal> inStorageList = Lists.newArrayList();
        List<BigDecimal> consumeList = Lists.newArrayList();
        wsLineList.forEach(ele -> {
            headList.add(ele.getOfficeName());
            pmList.add(BigDecimalUtil.setScale(dateRangePmMap.get(ele.getId()), bit));
            inStorageList.add(BigDecimalUtil.setScale(dateRangeInStorageMap.get(ele.getId()), bit));
            consumeList.add(BigDecimalUtil.setScale(dateRangeUseCountMap.get(ele.getId()), bit));
        });
        resVo.setHeadList(headList);
        List<SteccnRow<String,List<BigDecimal>>> rowList = Lists.newArrayList();
        rowList.add(new SteccnRow<>("产量",pmList));
        rowList.add(new SteccnRow<>("入库",inStorageList));
        rowList.add(new SteccnRow<>("耗用",consumeList));
        resVo.setRowList(rowList);
        return resVo;
    }

    @Override
    public QtLineChartBarDTO lineProduceClassGroup(Integer workShopId, LocalDate start, LocalDate end, String unit, Integer bit) {
        List<SysOfficeVO> wsLineList = getWsLineList(workShopId);
        Assert.notNull(wsLineList, "车间下暂无产线");
        Set<Integer> lineIdList = wsLineList.stream().map(SysOfficeVO::getId).collect(Collectors.toSet());
        Map<String, BigDecimal> pmMap = reportWorkService.lineClassPm(lineIdList, start, end, 1, unit);
        Map<String, BigDecimal> inStorageMap = reportWorkService.lineClassInStorage(lineIdList, start, end, unit);
        Map<String, BigDecimal> useCountMap = reportWorkService.lineClassUseCount(lineIdList, start, end, unit);
        //获取白班与夜班
        List<SysDictVO> sysDictList = FeignOptional.of(() -> remoteDictFeignApi.listByType(PmManagerConstant.SYS_SHIFT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<StackGraphDTO<String, BigDecimal>> stackGraphDTOList = Lists.newArrayList();
        //组装数据
        List<String> xAxisList1 = Lists.newArrayList();
        List<String> xAxisList2 = Lists.newArrayList();
        for (SysOfficeVO line : wsLineList) {
            for (SysDictVO sysDict : sysDictList) {
                StackGraphDTO<String, BigDecimal> stackGraphDTO = new StackGraphDTO<>();
                stackGraphDTO.setXAxis(sysDict.getName());
                List<TupleDTO<String, BigDecimal>> dataList = Lists.newArrayList();
                //产量，入库，耗用
                dataList.add(new TupleDTO<>("产量", BigDecimalUtil.setScale(pmMap.get(line.getId() + "_" + sysDict.getId()), bit)));
                dataList.add(new TupleDTO<>("入库", BigDecimalUtil.setScale(inStorageMap.get(line.getId() + "_" + sysDict.getId()), bit)));
                dataList.add(new TupleDTO<>("耗用", BigDecimalUtil.setScale(useCountMap.get(line.getId() + "_" + sysDict.getId()), bit)));
                stackGraphDTO.setData(dataList);
                stackGraphDTOList.add(stackGraphDTO);
                xAxisList2.add(sysDict.getName());
            }
            xAxisList1.add(line.getOfficeName());
        }
        //自定义转换工具
        QtLineChartBarDTO qtLineChartBarDTO = StackGraphUtil.convertData(stackGraphDTOList);
        qtLineChartBarDTO.setXData(null);
        qtLineChartBarDTO.setXAxis(new LineProduceClassGroupVO(xAxisList1, xAxisList2));
        return qtLineChartBarDTO;
    }

    @Override
    public SjgDashboardLineProduceVO lineProduceClassGroupDetail(Integer workShopId, LocalDate start, LocalDate end, String unit, Integer bit) {
        List<SysOfficeVO> wsLineList = getWsLineList(workShopId);
        Assert.notNull(wsLineList, "车间下暂无产线");
        Set<Integer> lineIdList = wsLineList.stream().map(SysOfficeVO::getId).collect(Collectors.toSet());
        SjgDashboardLineProduceVO resVo = new SjgDashboardLineProduceVO();
        Map<String, BigDecimal> pmMap = reportWorkService.lineClassPm(lineIdList, start, end, 1, unit);
        Map<String, BigDecimal> inStorageMap = reportWorkService.lineClassInStorage(lineIdList, start, end, unit);
        Map<String, BigDecimal> useCountMap = reportWorkService.lineClassUseCount(lineIdList, start, end, unit);
        List<String> headList = Lists.newArrayList();
        List<BigDecimal> pmList = Lists.newArrayList();
        List<BigDecimal> inStorageList = Lists.newArrayList();
        List<BigDecimal> consumeList = Lists.newArrayList();
        //获取白班与夜班
        List<SysDictVO> sysDictList = FeignOptional.of(() -> remoteDictFeignApi.listByType(PmManagerConstant.SYS_SHIFT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        wsLineList.forEach(line -> {
            headList.add(line.getOfficeName());
            for (SysDictVO sysDict : sysDictList) {
                pmList.add(BigDecimalUtil.setScale(pmMap.get(line.getId() + "_" + sysDict.getId()), bit));
                inStorageList.add(BigDecimalUtil.setScale(inStorageMap.get(line.getId() + "_" + sysDict.getId()), bit));
                consumeList.add(BigDecimalUtil.setScale(useCountMap.get(line.getId() + "_" + sysDict.getId()), bit));
            }
        });
        resVo.setHeadList(headList);
        List<SteccnRow<String,List<BigDecimal>>> rowList = Lists.newArrayList();
        rowList.add(new SteccnRow<>("产量",pmList));
        rowList.add(new SteccnRow<>("入库",inStorageList));
        rowList.add(new SteccnRow<>("耗用",consumeList));
        resVo.setRowList(rowList);
        return resVo;
    }

    @Override
    public QtLineChartBarDTO linePassRate(Integer workShopId, LocalDate start, LocalDate end) {
        List<SysOfficeVO> wsLineList = getWsLineList(workShopId);
        Assert.notNull(wsLineList, "车间下暂无产线");
        Set<Integer> lineIdList = wsLineList.stream().map(SysOfficeVO::getId).collect(Collectors.toSet());
        //获取全量数据
        ProcessReportWorkPageDTO dto = new ProcessReportWorkPageDTO();
        dto.setStartDtStart(start);
        dto.setStartDtEnd(end);
        dto.setProductLineIds(lineIdList.stream().collect(Collectors.toList()));
        List<ProcessReportWorkPageVO> processReportWorkPageVOS = pmsReportWorkMapper.processReportDashBoard(dto);
        Assert.notNull(processReportWorkPageVOS, "暂无数据");
        //把所有的工序名称挑出来
        List<String> processNames = processReportWorkPageVOS.stream().map(ProcessReportWorkPageVO::getProcessName).distinct().collect(Collectors.toList());
        //开始组数据
        List<StackGraphDTO<String, BigDecimal>> stackGraphDTOList = Lists.newArrayList();
        wsLineList.forEach(ele -> {
            StackGraphDTO<String, BigDecimal> stackGraphDTO = new StackGraphDTO<>();
            stackGraphDTO.setXAxis(ele.getOfficeName());
            List<TupleDTO<String, BigDecimal>> dataList = Lists.newArrayList();
            List<ProcessReportWorkPageVO> lineData = processReportWorkPageVOS.stream().filter(v -> v.getProductLineId().equals(ele.getId())).collect(Collectors.toList());
            //对工序进行分组
            for(String name:processNames){
                List<ProcessReportWorkPageVO> processData = lineData.stream().filter(v -> v.getProcessName().equals(name)).collect(Collectors.toList());
                if(CollUtil.isNotEmpty(processData)){
                    BigDecimal processQuantity = BigDecimalUtil.sum(processData.stream().map(ProcessReportWorkPageVO::getProcessQuantity).collect(Collectors.toList()));
                    BigDecimal badnessQuantity = BigDecimalUtil.sum(processData.stream().map(ProcessReportWorkPageVO::getBadnessQuantity).collect(Collectors.toList()));
                    BigDecimal minus = BigDecimalUtil.subtract(processQuantity,badnessQuantity);
                    dataList.add(new TupleDTO<>(name, BigDecimalUtil.divide(minus,processQuantity,4)));
                }else {
                    dataList.add(new TupleDTO<>(name, null));
                }
            }
            List<ProcessReportWorkPageVO> moData = lineData.stream().filter(v -> v.getProcessName().equals("磨边工序")).collect(Collectors.toList());
            List<ProcessReportWorkPageVO> gangData = lineData.stream().filter(v -> v.getProcessName().equals("钢化工序")).collect(Collectors.toList());
            if(CollUtil.isNotEmpty(moData)&&CollUtil.isNotEmpty(gangData)){
                BigDecimal moProcessQuantity = BigDecimalUtil.sum(moData.stream().map(ProcessReportWorkPageVO::getProcessQuantity).collect(Collectors.toList()));
                BigDecimal gangBadnessQuantity = BigDecimalUtil.sum(gangData.stream().map(ProcessReportWorkPageVO::getBadnessQuantity).collect(Collectors.toList()));
                BigDecimal gangProcessQuantity = BigDecimalUtil.sum(gangData.stream().map(ProcessReportWorkPageVO::getProcessQuantity).collect(Collectors.toList()));
                BigDecimal minus = BigDecimalUtil.subtract(gangProcessQuantity,gangBadnessQuantity);
                //校验是否比1大
                BigDecimal divide = BigDecimalUtil.divide(minus, moProcessQuantity, 4);
                if(divide.compareTo(new BigDecimal(1))==1){
                    divide = BigDecimal.ONE;
                }
                dataList.add(new TupleDTO<>("合计", divide));
            }
            //算合计
            stackGraphDTO.setData(dataList);
            stackGraphDTOList.add(stackGraphDTO);
        });
        return StackGraphUtil.convertData(stackGraphDTOList);
    }

    @Override
    public SjgDashboardLineProduceVO linePassRateDetail(Integer workShopId, LocalDate start, LocalDate end) {
        List<SysOfficeVO> wsLineList = getWsLineList(workShopId);
        Assert.notNull(wsLineList, "车间下暂无产线");
        Set<Integer> lineIdList = wsLineList.stream().map(SysOfficeVO::getId).collect(Collectors.toSet());
        //获取全量数据
        ProcessReportWorkPageDTO dto = new ProcessReportWorkPageDTO();
        dto.setStartDtStart(start);
        dto.setStartDtEnd(end);
        dto.setProductLineIds(lineIdList.stream().collect(Collectors.toList()));
        List<ProcessReportWorkPageVO> processReportWorkPageVOS = pmsReportWorkMapper.processReportDashBoard(dto);
        Assert.notNull(processReportWorkPageVOS, "暂无数据");
        //开始组数据
        SjgDashboardLineProduceVO result = new SjgDashboardLineProduceVO();
        List<String> headList = Lists.newArrayList();
        List<SteccnRow<String,List<BigDecimal>>> rowList = Lists.newArrayList();
        List<String> processNames = processReportWorkPageVOS.stream().map(ProcessReportWorkPageVO::getProcessName).distinct().collect(Collectors.toList());
        //组表头
        wsLineList.forEach(ele -> {
            headList.add(ele.getOfficeName());
        });
        //对工序进行分组
        for(String name:processNames){
            SteccnRow<String,List<BigDecimal>> row = new SteccnRow<>();
            List<BigDecimal> rowData = Lists.newArrayList();
            for(SysOfficeVO tmp:wsLineList){
                List<ProcessReportWorkPageVO> processData = processReportWorkPageVOS.stream()
                        .filter(v -> v.getProcessName().equals(name)&&v.getProductLineId().equals(tmp.getId())).collect(Collectors.toList());
                if(CollUtil.isNotEmpty(processData)){
                    BigDecimal processQuantity = BigDecimalUtil.sum(processData.stream().map(ProcessReportWorkPageVO::getProcessQuantity).collect(Collectors.toList()));
                    BigDecimal badnessQuantity = BigDecimalUtil.sum(processData.stream().map(ProcessReportWorkPageVO::getBadnessQuantity).collect(Collectors.toList()));
                    BigDecimal minus = BigDecimalUtil.subtract(processQuantity,badnessQuantity);
                    rowData.add(BigDecimalUtil.divide(minus,processQuantity,4));
                }else {
                    rowData.add(null);
                }
            }
            row.setFixRow(name);
            row.setDynamicRow(rowData);
            rowList.add(row);
        }
        //算合计
        SteccnRow<String,List<BigDecimal>> row = new SteccnRow<>();
        List<BigDecimal> rowData = Lists.newArrayList();
        for(SysOfficeVO tmp:wsLineList){
            List<ProcessReportWorkPageVO> processData = processReportWorkPageVOS.stream()
                    .filter(v -> v.getProductLineId().equals(tmp.getId())).collect(Collectors.toList());
            List<ProcessReportWorkPageVO> moData = processData.stream().filter(v -> v.getProcessName().equals("磨边工序")).collect(Collectors.toList());
            List<ProcessReportWorkPageVO> gangData = processData.stream().filter(v -> v.getProcessName().equals("钢化工序")).collect(Collectors.toList());
            if(CollUtil.isNotEmpty(moData)&&CollUtil.isNotEmpty(gangData)){
                BigDecimal moProcessQuantity = BigDecimalUtil.sum(moData.stream().map(ProcessReportWorkPageVO::getProcessQuantity).collect(Collectors.toList()));
                BigDecimal gangBadnessQuantity = BigDecimalUtil.sum(gangData.stream().map(ProcessReportWorkPageVO::getBadnessQuantity).collect(Collectors.toList()));
                BigDecimal gangProcessQuantity = BigDecimalUtil.sum(gangData.stream().map(ProcessReportWorkPageVO::getProcessQuantity).collect(Collectors.toList()));
                BigDecimal minus = BigDecimalUtil.subtract(gangProcessQuantity,gangBadnessQuantity);
                //校验是否比1大
                BigDecimal divide = BigDecimalUtil.divide(minus, moProcessQuantity, 4);
                if(divide.compareTo(new BigDecimal(1))==1){
                    divide = BigDecimal.ONE;
                }
                rowData.add(divide);
            }else {
                rowData.add(null);
            }
        }
        row.setFixRow("合计");
        row.setDynamicRow(rowData);
        rowList.add(row);
        result.setHeadList(headList);
        result.setRowList(rowList);
        return result;
    }

    @Override
    public QtLineChartBarDTO lineGroupPassRate(Integer workShopId, LocalDate start, LocalDate end) {
        List<SysOfficeVO> wsLineList = getWsLineList(workShopId);
        Assert.notNull(wsLineList, "车间下暂无产线");
        Set<Integer> lineIdList = wsLineList.stream().map(SysOfficeVO::getId).collect(Collectors.toSet());
        //获取全量数据
        ProcessReportWorkPageDTO dto = new ProcessReportWorkPageDTO();
        dto.setStartDtStart(start);
        dto.setStartDtEnd(end);
        dto.setProductLineIds(lineIdList.stream().collect(Collectors.toList()));
        List<ProcessReportWorkPageVO> processReportWorkPageVOS = pmsReportWorkMapper.processReportDashBoard(dto);
        Assert.notNull(processReportWorkPageVOS, "暂无数据");
        //获取白班与夜班
        List<SysDictVO> sysDictList = FeignOptional.of(() -> remoteDictFeignApi.listByType(PmManagerConstant.SYS_SHIFT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //把所有的工序名称挑出来
        List<String> processNames = processReportWorkPageVOS.stream().map(ProcessReportWorkPageVO::getProcessName).distinct().collect(Collectors.toList());
        //开始组数据
        List<StackGraphDTO<String, BigDecimal>> stackGraphDTOList = Lists.newArrayList();
        //双重x轴
        List<String> xAxisList1 = Lists.newArrayList();
        List<String> xAxisList2 = Lists.newArrayList();
        wsLineList.forEach(ele -> {
            for (SysDictVO sysDict : sysDictList) {
                StackGraphDTO<String, BigDecimal> stackGraphDTO = new StackGraphDTO<>();
                List<TupleDTO<String, BigDecimal>> dataList = Lists.newArrayList();
                stackGraphDTO.setXAxis(sysDict.getName());
                List<ProcessReportWorkPageVO> classOrderData = processReportWorkPageVOS.stream().filter(v -> v.getProductLineId().equals(ele.getId())&&v.getClassesId().equals(sysDict.getId())).collect(Collectors.toList());
                //对工序进行分组
                for(String name:processNames){
                    List<ProcessReportWorkPageVO> processData = classOrderData.stream().filter(v -> v.getProcessName().equals(name)).collect(Collectors.toList());
                    if(CollUtil.isNotEmpty(processData)){
                        BigDecimal processQuantity = BigDecimalUtil.sum(processData.stream().map(ProcessReportWorkPageVO::getProcessQuantity).collect(Collectors.toList()));
                        BigDecimal badnessQuantity = BigDecimalUtil.sum(processData.stream().map(ProcessReportWorkPageVO::getBadnessQuantity).collect(Collectors.toList()));
                        BigDecimal minus = BigDecimalUtil.subtract(processQuantity,badnessQuantity);
                        dataList.add(new TupleDTO<>(name, BigDecimalUtil.divide(minus,processQuantity,4)));
                    }else {
                        dataList.add(new TupleDTO<>(name, null));
                    }
                }
                //算合计
                List<ProcessReportWorkPageVO> moData = classOrderData.stream().filter(v -> v.getProcessName().equals("磨边工序")).collect(Collectors.toList());
                List<ProcessReportWorkPageVO> gangData = classOrderData.stream().filter(v -> v.getProcessName().equals("钢化工序")).collect(Collectors.toList());
                if(CollUtil.isNotEmpty(moData)&&CollUtil.isNotEmpty(gangData)){
                    BigDecimal moProcessQuantity = BigDecimalUtil.sum(moData.stream().map(ProcessReportWorkPageVO::getProcessQuantity).collect(Collectors.toList()));
                    BigDecimal gangBadnessQuantity = BigDecimalUtil.sum(gangData.stream().map(ProcessReportWorkPageVO::getBadnessQuantity).collect(Collectors.toList()));
                    BigDecimal gangProcessQuantity = BigDecimalUtil.sum(gangData.stream().map(ProcessReportWorkPageVO::getProcessQuantity).collect(Collectors.toList()));
                    BigDecimal minus = BigDecimalUtil.subtract(gangProcessQuantity,gangBadnessQuantity);
                    //校验是否比1大
                    BigDecimal divide = BigDecimalUtil.divide(minus, moProcessQuantity, 4);
                    if(divide.compareTo(new BigDecimal(1))==1){
                        divide = BigDecimal.ONE;
                    }
                    dataList.add(new TupleDTO<>("合计", divide));
                }
                stackGraphDTO.setData(dataList);
                stackGraphDTOList.add(stackGraphDTO);
                xAxisList2.add(sysDict.getName());
            }
            xAxisList1.add(ele.getOfficeName());
        });
        QtLineChartBarDTO qtLineChartBarDTO = StackGraphUtil.convertData(stackGraphDTOList);
        qtLineChartBarDTO.setXData(null);
        qtLineChartBarDTO.setXAxis(new LineProduceClassGroupVO(xAxisList1, xAxisList2));
        return qtLineChartBarDTO;
    }

    @Override
    public SjgDashboardLineProduceVO lineGroupPassRateDetail(Integer workShopId, LocalDate start, LocalDate end) {
        List<SysOfficeVO> wsLineList = getWsLineList(workShopId);
        Assert.notNull(wsLineList, "车间下暂无产线");
        Set<Integer> lineIdList = wsLineList.stream().map(SysOfficeVO::getId).collect(Collectors.toSet());
        //获取全量数据
        ProcessReportWorkPageDTO dto = new ProcessReportWorkPageDTO();
        dto.setStartDtStart(start);
        dto.setStartDtEnd(end);
        dto.setProductLineIds(lineIdList.stream().collect(Collectors.toList()));
        List<ProcessReportWorkPageVO> processReportWorkPageVOS = pmsReportWorkMapper.processReportDashBoard(dto);
        Assert.notNull(processReportWorkPageVOS, "暂无数据");
        //获取白班与夜班
        List<SysDictVO> sysDictList = FeignOptional.of(() -> remoteDictFeignApi.listByType(PmManagerConstant.SYS_SHIFT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //开始组数据
        SjgDashboardLineProduceVO result = new SjgDashboardLineProduceVO();
        List<String> headList = Lists.newArrayList();
        List<SteccnRow<String,List<BigDecimal>>> rowList = Lists.newArrayList();
        List<String> processNames = processReportWorkPageVOS.stream().map(ProcessReportWorkPageVO::getProcessName).distinct().collect(Collectors.toList());
        //组表头
        wsLineList.forEach(ele -> {
            headList.add(ele.getOfficeName());
        });
        //对工序进行分组
        for(String name:processNames){
            SteccnRow<String,List<BigDecimal>> row = new SteccnRow<>();
            List<BigDecimal> rowData = Lists.newArrayList();
            for(SysOfficeVO tmp:wsLineList){
                for(SysDictVO dicTmp:sysDictList){
                    List<ProcessReportWorkPageVO> processData = processReportWorkPageVOS.stream()
                            .filter(v -> v.getProcessName().equals(name)
                                    &&v.getProductLineId().equals(tmp.getId())
                                    &&v.getClassesId().equals(dicTmp.getId())).collect(Collectors.toList());
                    if(CollUtil.isNotEmpty(processData)){
                        BigDecimal processQuantity = BigDecimalUtil.sum(processData.stream().map(ProcessReportWorkPageVO::getProcessQuantity).collect(Collectors.toList()));
                        BigDecimal badnessQuantity = BigDecimalUtil.sum(processData.stream().map(ProcessReportWorkPageVO::getBadnessQuantity).collect(Collectors.toList()));
                        BigDecimal minus = BigDecimalUtil.subtract(processQuantity,badnessQuantity);
                        rowData.add(BigDecimalUtil.divide(minus,processQuantity,4));
                    }else {
                        rowData.add(null);
                    }
                }
            }
            row.setFixRow(name);
            row.setDynamicRow(rowData);
            rowList.add(row);
        }
        //算合计
        SteccnRow<String,List<BigDecimal>> row = new SteccnRow<>();
        List<BigDecimal> rowData = Lists.newArrayList();
        for(SysOfficeVO tmp:wsLineList){
            for(SysDictVO dicTmp:sysDictList){
                List<ProcessReportWorkPageVO> processData = processReportWorkPageVOS.stream()
                        .filter(v -> v.getProductLineId().equals(tmp.getId())
                                &&v.getClassesId().equals(dicTmp.getId())).collect(Collectors.toList());
                List<ProcessReportWorkPageVO> moData = processData.stream().filter(v -> v.getProcessName().equals("磨边工序")).collect(Collectors.toList());
                List<ProcessReportWorkPageVO> gangData = processData.stream().filter(v -> v.getProcessName().equals("钢化工序")).collect(Collectors.toList());
                if(CollUtil.isNotEmpty(moData)&&CollUtil.isNotEmpty(gangData)){
                    BigDecimal moProcessQuantity = BigDecimalUtil.sum(moData.stream().map(ProcessReportWorkPageVO::getProcessQuantity).collect(Collectors.toList()));
                    BigDecimal gangBadnessQuantity = BigDecimalUtil.sum(gangData.stream().map(ProcessReportWorkPageVO::getBadnessQuantity).collect(Collectors.toList()));
                    BigDecimal gangProcessQuantity = BigDecimalUtil.sum(gangData.stream().map(ProcessReportWorkPageVO::getProcessQuantity).collect(Collectors.toList()));
                    BigDecimal minus = BigDecimalUtil.subtract(gangProcessQuantity,gangBadnessQuantity);
                    //校验是否比1大
                    BigDecimal divide = BigDecimalUtil.divide(minus, moProcessQuantity, 4);
                    if(divide.compareTo(new BigDecimal(1))==1){
                        divide = BigDecimal.ONE;
                    }
                    rowData.add(divide);
                }else {
                    rowData.add(null);
                }
            }
        }
        row.setFixRow("合计");
        row.setDynamicRow(rowData);
        rowList.add(row);
        result.setHeadList(headList);
        result.setRowList(rowList);
        return result;
    }

    @Override
    public List<SjgCountReportVO> countReport(Integer workShopId, LocalDate start, LocalDate end) {
        List<SjgCountReportVO> resList = Lists.newArrayList();
        //构建表头
        List<TableHeadColumnDTO> columnList1 = setCountReportHead(start,end);
        SjgCountReportVO sjgCountReportVO = new SjgCountReportVO();
        sjgCountReportVO.setColumns(columnList1);
        resList.add(sjgCountReportVO);

        return resList;
    }

    /**
     * 设置深加工统计报表头
     * @return
     */
    private List<TableHeadColumnDTO> setCountReportHead(LocalDate start, LocalDate end) {
        String currentStartStr = LocalDateUtil.format(start, "MM.dd");
        String currentEndStr = LocalDateUtil.format(end, "MM.dd");
        LocalDate preEnd = start.minusDays(1);
        long daysBetween = ChronoUnit.DAYS.between(start, end);
        LocalDate preStart = preEnd.minusDays(daysBetween);
        String preStartStr = LocalDateUtil.format(preStart, "MM.dd");
        String preEndStr = LocalDateUtil.format(preEnd, "MM.dd");
        List<TableHeadColumnDTO> resList = Lists.newArrayList();
        resList.add(new TableHeadColumnDTO("类别", "categoryName"));
        resList.add(new TableHeadColumnDTO("线别", "lineName"));
        resList.add(new TableHeadColumnDTO("本区间产量" + currentStartStr + "-" + currentEndStr, "currentValueTuple" , Arrays.asList(new TableHeadColumnDTO("currentValue", "㎡"), new TableHeadColumnDTO("currentValueRate", "%"))));
        resList.add(new TableHeadColumnDTO("上区间产量" + preStartStr + "-" + preEndStr, "preValueTuple" , Arrays.asList(new TableHeadColumnDTO("preValue", "㎡"), new TableHeadColumnDTO("preValueRate", "%"))));
        resList.add(new TableHeadColumnDTO("产量","valueDiff"));
        resList.add(new TableHeadColumnDTO("成品率","valueDiffRate"));
        resList.add(new TableHeadColumnDTO("本区间合计", "currentValueSumTuple" , Arrays.asList(new TableHeadColumnDTO("currentValueSum", "㎡"), new TableHeadColumnDTO("currentValueSumRate", "%"))));
        resList.add(new TableHeadColumnDTO("上区间合计", "preValueSumTuple" , Arrays.asList(new TableHeadColumnDTO("preValueSum", "㎡"), new TableHeadColumnDTO("preValueSumRate", "%"))));
        resList.add(new TableHeadColumnDTO("产量","valueDiffSum"));
        resList.add(new TableHeadColumnDTO("成品率","valueDiffSumRate"));
        return resList;
    }

    /**
     * 获取车间的产线集合
     *
     * @param workShopId 车间
     * @return
     */
    private List<SysOfficeVO> getWsLineList(Integer workShopId) {
        //根据车间获取产线集合
        SysOfficeParentDTO sysOfficeParent = new SysOfficeParentDTO();
        sysOfficeParent.setId(workShopId);
        sysOfficeParent.setOfficeType(SysOfficeConstant.PRODUCT_LINE);
        List<SysOfficeVO> lineOfficeList = FeignOptional.of(() -> remoteOfficeFeign.listByParentId(sysOfficeParent), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        return lineOfficeList;
    }

}
