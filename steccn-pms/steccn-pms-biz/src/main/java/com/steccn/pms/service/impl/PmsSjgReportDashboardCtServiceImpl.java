package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.WriteTable;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.dto.*;
import com.steccn.common.util.*;
import com.steccn.dp.dto.DpBusiMonitorDTO;
import com.steccn.dp.feign.RemoteBusiMonitorFeignApi;
import com.steccn.ems.feign.RemoteMeterReadFeignApi;
import com.steccn.platform.constant.SysMsgConstant;
import com.steccn.platform.constant.SysOfficeConstant;
import com.steccn.platform.dto.SysMsgSendDTO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteMsgFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSysProductionCapacityFeignApi;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysMsgAdditionDTO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.platform.vo.SysProductionCapacityDetailVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.dto.PmsDefectDayOrderBasicDTO;
import com.steccn.pms.dto.ProcessReportWorkPageDTO;
import com.steccn.pms.mapper.PmsReportWorkHourMapper;
import com.steccn.pms.mapper.PmsReportWorkMapper;
import com.steccn.pms.service.*;
import com.steccn.pms.vo.*;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.excel.strategy.ExcelFillCellMergeStrategy;
import com.steccn.qms.excel.strategy.HeadContentStyleStrategy;
import com.steccn.qms.excel.strategy.RowHeightStyleStrategy;
import com.steccn.qms.util.MysqlDateKeyUtil;
import com.steccn.websocket.constant.WebsocketConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
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
    private final PmsOfficeManager pmsOfficeManager;
    private final RemoteDictFeignApi remoteDictFeignApi;
    private final PmsReportWorkMapper pmsReportWorkMapper;
    private final PmsReportWorkHourMapper pmsReportWorkHourMapper;
    private final RemoteSysProductionCapacityFeignApi sysProductionCapacityFeignApi;
    private final RemoteMeterReadFeignApi remoteMeterReadFeignApi;
    private final PmsCompleteDeliveryService pmsCompleteDeliveryService;
    private final RemoteOfficeFeign remoteOfficeFeign;
    private final PmsWorkOrderBatchService pmsWorkOrderBatchService;
    private final RemoteBusiMonitorFeignApi remoteBusiMonitorFeignApi;
    private final PmsProduceOrderService produceOrderService;
    private final RemoteMsgFeignApi remoteMsgFeignApi;

    @Override
    public QtLineChartBarDTO lineProduce(Integer workShopId, LocalDate start, LocalDate end, String unit, Integer bit) {
        //产量：钢化工序的合格数（加工数-不良数）
        //入库：选择时间端内，该产线入库数量汇总
        //耗用：磨边工序的加工数
        List<SysOfficeVO> wsLineList = pmsOfficeManager.getWsLineList(workShopId);
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
        List<SysOfficeVO> wsLineList = pmsOfficeManager.getWsLineList(workShopId);
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
        List<SteccnRow<String, List<BigDecimal>>> rowList = Lists.newArrayList();
        rowList.add(new SteccnRow<>("产量", pmList));
        rowList.add(new SteccnRow<>("入库", inStorageList));
        rowList.add(new SteccnRow<>("耗用", consumeList));
        resVo.setRowList(rowList);
        return resVo;
    }

    @Override
    public QtLineChartBarDTO lineProduceClassGroup(Integer workShopId, LocalDate start, LocalDate end, String unit, Integer bit) {
        List<SysOfficeVO> wsLineList = pmsOfficeManager.getWsLineList(workShopId);
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
        List<SysOfficeVO> wsLineList = pmsOfficeManager.getWsLineList(workShopId);
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
        List<SteccnRow<String, List<BigDecimal>>> rowList = Lists.newArrayList();
        rowList.add(new SteccnRow<>("产量", pmList));
        rowList.add(new SteccnRow<>("入库", inStorageList));
        rowList.add(new SteccnRow<>("耗用", consumeList));
        resVo.setRowList(rowList);
        return resVo;
    }

    @Override
    public QtLineChartBarDTO linePassRate(Integer workShopId, LocalDate start, LocalDate end) {
        List<SysOfficeVO> wsLineList = pmsOfficeManager.getWsLineList(workShopId);
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
            for (String name : processNames) {
                List<ProcessReportWorkPageVO> processData = lineData.stream().filter(v -> v.getProcessName().equals(name)).collect(Collectors.toList());
                if (CollUtil.isNotEmpty(processData)) {
                    BigDecimal processQuantity = BigDecimalUtil.sum(processData.stream().map(ProcessReportWorkPageVO::getProcessQuantity).collect(Collectors.toList()));
                    BigDecimal badnessQuantity = BigDecimalUtil.sum(processData.stream().map(ProcessReportWorkPageVO::getBadnessQuantity).collect(Collectors.toList()));
                    BigDecimal minus = BigDecimalUtil.subtract(processQuantity, badnessQuantity);
                    dataList.add(new TupleDTO<>(name, BigDecimalUtil.divide(minus, processQuantity, 4)));
                } else {
                    dataList.add(new TupleDTO<>(name, null));
                }
            }
            List<ProcessReportWorkPageVO> moData = lineData.stream().filter(v -> v.getProcessName().equals("磨边工序")).collect(Collectors.toList());
            List<ProcessReportWorkPageVO> gangData = lineData.stream().filter(v -> v.getProcessName().equals("钢化工序")).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(moData) && CollUtil.isNotEmpty(gangData)) {
                BigDecimal moProcessQuantity = BigDecimalUtil.sum(moData.stream().map(ProcessReportWorkPageVO::getProcessQuantity).collect(Collectors.toList()));
                BigDecimal gangBadnessQuantity = BigDecimalUtil.sum(gangData.stream().map(ProcessReportWorkPageVO::getBadnessQuantity).collect(Collectors.toList()));
                BigDecimal gangProcessQuantity = BigDecimalUtil.sum(gangData.stream().map(ProcessReportWorkPageVO::getProcessQuantity).collect(Collectors.toList()));
                BigDecimal minus = BigDecimalUtil.subtract(gangProcessQuantity, gangBadnessQuantity);
                //校验是否比1大
                BigDecimal divide = BigDecimalUtil.divide(minus, moProcessQuantity, 4);
                if (divide.compareTo(new BigDecimal(1)) == 1) {
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
    public void linePassRateMonitor(LocalDateTime start, LocalDateTime end, String alarmCode, QtManagerConstant.QtGranularityEnum granularityEnum) {
        //获取早班还是晚班还是不分班
        String classNameOrNot = Strings.EMPTY;
        Integer classNameId = null;
        if (QtManagerConstant.QtGranularityEnum.CLASS.equals(granularityEnum)) {
            classNameId = LocalDateUtil.getClassId(start, Boolean.FALSE);
            classNameOrNot = classNameId == 0 ? "早班" : "晚班";
        }
        //获取监控信息中的产线
        List<DpBusiMonitorDTO> pmNumAlarmList = FeignOptional.of(() -> remoteBusiMonitorFeignApi.getBusiMonitorAlarm(alarmCode, granularityEnum.getCode()), FeignConstant.STECCN_DP_SERVICE_NAME).get();
        for (DpBusiMonitorDTO monitor : pmNumAlarmList) {
            Map<String, BigDecimal> resMap = Maps.newHashMap();
            List<Integer> lineIdList = monitor.getMonitorRange();
            Map<Integer, SysOfficeVO> lineIdMap = FeignOptional.of(() -> remoteOfficeFeign.listById(Sets.newHashSet(lineIdList)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            //从生产订单中获取产线、报工的工序
            List<PmsDefectDayOrderBasicDTO> productBatchList = pmsWorkOrderBatchService.defectDay(lineIdList, start, end);
            //获取产线需要报工的工序集合
            Set<Long> produceOrderIdSet = productBatchList.stream().map(PmsDefectDayOrderBasicDTO::getProduceOrderId).collect(Collectors.toSet());
            //根据生产订单获取报工工序集合
            Map<Long, List<PmsProduceOrderCraftVO>> orderProcessMap = produceOrderService.processList(produceOrderIdSet);
            Map<Long, PmsProduceOrderVO> orderIdMap = produceOrderService.listById(produceOrderIdSet);
            Map<Integer, Set<Long>> orderLineMap = Maps.newHashMap();
            Map<Integer,List<String>> lineProcessMap = Maps.newHashMap();
            orderIdMap.forEach((k, v) -> {
                MapUtil.computeSet(orderLineMap, v.getProductionLineId(), k);
            });
            //根据产线
            lineIdMap.forEach((k, u) -> {
                Set<Long> orderIdSet = orderLineMap.getOrDefault(k,Sets.newHashSet());
                //获取需要报工的工序集合
                List<String> processNameListTmp = orderProcessMap.values().stream().flatMap(Collection::stream).filter(v -> orderIdSet.contains(v.getProduceOrderId()) && MpConstant.YES.equals(v.getReportWorkFlag()))
                        .sorted(Comparator.comparing(PmsProduceOrderCraftVO::getSort))
                        .map(PmsProduceOrderCraftVO::getName).distinct().collect(Collectors.toList());
                lineProcessMap.put(k, processNameListTmp);
            });
            //获取全量数据
            ProcessReportWorkPageDTO dto = new ProcessReportWorkPageDTO();
            dto.setStartDtStart(start.toLocalDate());
            dto.setStartDtEnd(start.toLocalDate());
            dto.setProductLineIds(lineIdList);
            List<ProcessReportWorkPageVO> reportWorkList = pmsReportWorkMapper.processReportDashBoard(dto);
            reportWorkList = GetValueUtil.get(reportWorkList, x -> x, Lists.newArrayList());
            //获取早班&晚班的数据字典
            SysDictVO morningDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.NUM_0, PmManagerConstant.SYS_SHIFT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            SysDictVO nightDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.NUM_1, PmManagerConstant.SYS_SHIFT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Assert.notNull(morningDict, "早班数据字典不存在");
            Assert.notNull(nightDict, "晚班数据字典不存在");
            //开始组数据
            for (SysOfficeVO ele : lineIdMap.values()) {
                List<ProcessReportWorkPageVO> lineData = reportWorkList.stream().filter(v -> v.getProductLineId().equals(ele.getId())).collect(Collectors.toList());
                //对工序进行分组
                for (String name : lineProcessMap.get(ele.getId())) {
                    List<ProcessReportWorkPageVO> processData = lineData.stream().filter(v -> v.getProcessName().equals(name)).collect(Collectors.toList());
                    if (CollUtil.isNotEmpty(processData)) {
                        BigDecimal pqToday = BigDecimalUtil.sum(processData.stream().map(ProcessReportWorkPageVO::getProcessQuantity).collect(Collectors.toList()));
                        resMap.put(ele.getOfficeName() + "_" + name + "_", BigDecimalUtil.setScale(pqToday, 0));
                        BigDecimal pqMorning = BigDecimalUtil.sum(processData.stream().filter(v -> v.getClassesId().equals(morningDict.getId())).map(ProcessReportWorkPageVO::getProcessQuantity).collect(Collectors.toList()));
                        resMap.put(ele.getOfficeName() + "_" + name + "_" + morningDict.getCode(), BigDecimalUtil.setScale(pqMorning, 0));
                        BigDecimal pqNight = BigDecimalUtil.sum(processData.stream().filter(v -> v.getClassesId().equals(nightDict.getId())).map(ProcessReportWorkPageVO::getProcessQuantity).collect(Collectors.toList()));
                        resMap.put(ele.getOfficeName() + "_" + name + "_" + nightDict.getCode(), BigDecimalUtil.setScale(pqNight, 0));
                    }
                }
            }
            StringBuilder contentSb = new StringBuilder();
            BigDecimal upValue = monitor.getUpValue();
            BigDecimal lowValue = monitor.getLowValue();
            for (Integer lineId : lineIdList) {
                SysOfficeVO office = lineIdMap.get(lineId);
                for (String process : lineProcessMap.get(lineId)) {
                    //唯一一种情况，仅仅产线集合告警
                    BigDecimal currentPmNum = resMap.get(office.getOfficeName() + "_" + process + "_" + GetValueUtil.get(classNameId, x -> x, Strings.EMPTY));
                    currentPmNum = GetValueUtil.get(currentPmNum, x -> x, BigDecimal.ZERO);
                    //高于上限
                    String template = SysMsgConstant.MsgTypeEnum.DEEP_REPORT_WORK_FINISH_NUM_ALARM.getTemplate();
                    if (Objects.nonNull(upValue) && BigDecimalUtil.gt(currentPmNum, upValue)) {
                        String ts1 = String.format(template, LocalDateUtil.format(start, "yyyy-MM-dd"), classNameOrNot, office.getOfficeName(), process, BigDecimalUtil.format(currentPmNum), "高", "上", BigDecimalUtil.format(upValue));
                        contentSb.append(ts1).append("\n");
                    } else if (Objects.nonNull(lowValue) && BigDecimalUtil.lt(currentPmNum, lowValue)) {
                        String ts3 = String.format(template, LocalDateUtil.format(start, "yyyy-MM-dd"), classNameOrNot, office.getOfficeName(), process, BigDecimalUtil.format(currentPmNum), "低", "下", BigDecimalUtil.format(lowValue));
                        contentSb.append(ts3).append("\n");
                    }
                }
            }
            //根据阈值前来报警
            if (contentSb.length() > 0) {
                Set<Long> notifyMan = monitor.getNotifyMan();
                SysMsgSendDTO sysMsgDTO = new SysMsgSendDTO();
                sysMsgDTO.setType(SysMsgConstant.MsgTypeEnum.DEEP_REPORT_WORK_FINISH_NUM_ALARM.getCode());
                sysMsgDTO.setTitle(SysMsgConstant.MsgTypeEnum.DEEP_REPORT_WORK_FINISH_NUM_ALARM.getTitle());
                sysMsgDTO.setWarnFlag(SysMsgConstant.MsgTypeEnum.DEEP_REPORT_WORK_FINISH_NUM_ALARM.getWarnFlag());
                sysMsgDTO.setUserList(notifyMan);
                sysMsgDTO.setContent(contentSb.substring(0, contentSb.length() - 1));
                sysMsgDTO.setPushType(WebsocketConstant.WsPushTypeEnum.MSG_SYS_REMIND.getType());
                SysMsgAdditionDTO sysMsgAdditionDTO = new SysMsgAdditionDTO();
                sysMsgAdditionDTO.setCode(LocalDateUtil.format(start, DatePattern.NORM_DATE_PATTERN));
                sysMsgDTO.setAddition(sysMsgAdditionDTO);
                Boolean aBoolean = FeignOptional.of(() -> remoteMsgFeignApi.sendMsg(sysMsgDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                log.info("linePassRateMonitor方法消息发送 -> {}", aBoolean);
            }
        }
    }

    @Override
    public SjgDashboardLineProduceVO linePassRateDetail(Integer workShopId, LocalDate start, LocalDate end) {
        List<SysOfficeVO> wsLineList = pmsOfficeManager.getWsLineList(workShopId);
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
        List<SteccnRow<String, List<BigDecimal>>> rowList = Lists.newArrayList();
        List<String> processNames = processReportWorkPageVOS.stream().map(ProcessReportWorkPageVO::getProcessName).distinct().collect(Collectors.toList());
        //组表头
        wsLineList.forEach(ele -> {
            headList.add(ele.getOfficeName());
        });
        //对工序进行分组
        for (String name : processNames) {
            SteccnRow<String, List<BigDecimal>> row = new SteccnRow<>();
            List<BigDecimal> rowData = Lists.newArrayList();
            for (SysOfficeVO tmp : wsLineList) {
                List<ProcessReportWorkPageVO> processData = processReportWorkPageVOS.stream()
                        .filter(v -> v.getProcessName().equals(name) && v.getProductLineId().equals(tmp.getId())).collect(Collectors.toList());
                if (CollUtil.isNotEmpty(processData)) {
                    BigDecimal processQuantity = BigDecimalUtil.sum(processData.stream().map(ProcessReportWorkPageVO::getProcessQuantity).collect(Collectors.toList()));
                    BigDecimal badnessQuantity = BigDecimalUtil.sum(processData.stream().map(ProcessReportWorkPageVO::getBadnessQuantity).collect(Collectors.toList()));
                    BigDecimal minus = BigDecimalUtil.subtract(processQuantity, badnessQuantity);
                    rowData.add(BigDecimalUtil.divide(minus, processQuantity, 4));
                } else {
                    rowData.add(null);
                }
            }
            row.setFixRow(name);
            row.setDynamicRow(rowData);
            rowList.add(row);
        }
        //算合计
        SteccnRow<String, List<BigDecimal>> row = new SteccnRow<>();
        List<BigDecimal> rowData = Lists.newArrayList();
        for (SysOfficeVO tmp : wsLineList) {
            List<ProcessReportWorkPageVO> processData = processReportWorkPageVOS.stream()
                    .filter(v -> v.getProductLineId().equals(tmp.getId())).collect(Collectors.toList());
            List<ProcessReportWorkPageVO> moData = processData.stream().filter(v -> v.getProcessName().equals("磨边工序")).collect(Collectors.toList());
            List<ProcessReportWorkPageVO> gangData = processData.stream().filter(v -> v.getProcessName().equals("钢化工序")).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(moData) && CollUtil.isNotEmpty(gangData)) {
                BigDecimal moProcessQuantity = BigDecimalUtil.sum(moData.stream().map(ProcessReportWorkPageVO::getProcessQuantity).collect(Collectors.toList()));
                BigDecimal gangBadnessQuantity = BigDecimalUtil.sum(gangData.stream().map(ProcessReportWorkPageVO::getBadnessQuantity).collect(Collectors.toList()));
                BigDecimal gangProcessQuantity = BigDecimalUtil.sum(gangData.stream().map(ProcessReportWorkPageVO::getProcessQuantity).collect(Collectors.toList()));
                BigDecimal minus = BigDecimalUtil.subtract(gangProcessQuantity, gangBadnessQuantity);
                //校验是否比1大
                BigDecimal divide = BigDecimalUtil.divide(minus, moProcessQuantity, 4);
                if (divide.compareTo(new BigDecimal(1)) == 1) {
                    divide = BigDecimal.ONE;
                }
                rowData.add(divide);
            } else {
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
        List<SysOfficeVO> wsLineList = pmsOfficeManager.getWsLineList(workShopId);
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
                List<ProcessReportWorkPageVO> classOrderData = processReportWorkPageVOS.stream().filter(v -> v.getProductLineId().equals(ele.getId()) && v.getClassesId().equals(sysDict.getId())).collect(Collectors.toList());
                //对工序进行分组
                for (String name : processNames) {
                    List<ProcessReportWorkPageVO> processData = classOrderData.stream().filter(v -> v.getProcessName().equals(name)).collect(Collectors.toList());
                    if (CollUtil.isNotEmpty(processData)) {
                        BigDecimal processQuantity = BigDecimalUtil.sum(processData.stream().map(ProcessReportWorkPageVO::getProcessQuantity).collect(Collectors.toList()));
                        BigDecimal badnessQuantity = BigDecimalUtil.sum(processData.stream().map(ProcessReportWorkPageVO::getBadnessQuantity).collect(Collectors.toList()));
                        BigDecimal minus = BigDecimalUtil.subtract(processQuantity, badnessQuantity);
                        dataList.add(new TupleDTO<>(name, BigDecimalUtil.divide(minus, processQuantity, 4)));
                    } else {
                        dataList.add(new TupleDTO<>(name, null));
                    }
                }
                //算合计
                List<ProcessReportWorkPageVO> moData = classOrderData.stream().filter(v -> v.getProcessName().equals("磨边工序")).collect(Collectors.toList());
                List<ProcessReportWorkPageVO> gangData = classOrderData.stream().filter(v -> v.getProcessName().equals("钢化工序")).collect(Collectors.toList());
                if (CollUtil.isNotEmpty(moData) && CollUtil.isNotEmpty(gangData)) {
                    BigDecimal moProcessQuantity = BigDecimalUtil.sum(moData.stream().map(ProcessReportWorkPageVO::getProcessQuantity).collect(Collectors.toList()));
                    BigDecimal gangBadnessQuantity = BigDecimalUtil.sum(gangData.stream().map(ProcessReportWorkPageVO::getBadnessQuantity).collect(Collectors.toList()));
                    BigDecimal gangProcessQuantity = BigDecimalUtil.sum(gangData.stream().map(ProcessReportWorkPageVO::getProcessQuantity).collect(Collectors.toList()));
                    BigDecimal minus = BigDecimalUtil.subtract(gangProcessQuantity, gangBadnessQuantity);
                    //校验是否比1大
                    BigDecimal divide = BigDecimalUtil.divide(minus, moProcessQuantity, 4);
                    if (divide.compareTo(new BigDecimal(1)) == 1) {
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
        List<SysOfficeVO> wsLineList = pmsOfficeManager.getWsLineList(workShopId);
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
        List<SteccnRow<String, List<BigDecimal>>> rowList = Lists.newArrayList();
        List<String> processNames = processReportWorkPageVOS.stream().map(ProcessReportWorkPageVO::getProcessName).distinct().collect(Collectors.toList());
        //组表头
        wsLineList.forEach(ele -> {
            headList.add(ele.getOfficeName());
        });
        //对工序进行分组
        for (String name : processNames) {
            SteccnRow<String, List<BigDecimal>> row = new SteccnRow<>();
            List<BigDecimal> rowData = Lists.newArrayList();
            for (SysOfficeVO tmp : wsLineList) {
                for (SysDictVO dicTmp : sysDictList) {
                    List<ProcessReportWorkPageVO> processData = processReportWorkPageVOS.stream()
                            .filter(v -> v.getProcessName().equals(name)
                                    && v.getProductLineId().equals(tmp.getId())
                                    && v.getClassesId().equals(dicTmp.getId())).collect(Collectors.toList());
                    if (CollUtil.isNotEmpty(processData)) {
                        BigDecimal processQuantity = BigDecimalUtil.sum(processData.stream().map(ProcessReportWorkPageVO::getProcessQuantity).collect(Collectors.toList()));
                        BigDecimal badnessQuantity = BigDecimalUtil.sum(processData.stream().map(ProcessReportWorkPageVO::getBadnessQuantity).collect(Collectors.toList()));
                        BigDecimal minus = BigDecimalUtil.subtract(processQuantity, badnessQuantity);
                        rowData.add(BigDecimalUtil.divide(minus, processQuantity, 4));
                    } else {
                        rowData.add(null);
                    }
                }
            }
            row.setFixRow(name);
            row.setDynamicRow(rowData);
            rowList.add(row);
        }
        //算合计
        SteccnRow<String, List<BigDecimal>> row = new SteccnRow<>();
        List<BigDecimal> rowData = Lists.newArrayList();
        for (SysOfficeVO tmp : wsLineList) {
            for (SysDictVO dicTmp : sysDictList) {
                List<ProcessReportWorkPageVO> processData = processReportWorkPageVOS.stream()
                        .filter(v -> v.getProductLineId().equals(tmp.getId())
                                && v.getClassesId().equals(dicTmp.getId())).collect(Collectors.toList());
                List<ProcessReportWorkPageVO> moData = processData.stream().filter(v -> v.getProcessName().equals("磨边工序")).collect(Collectors.toList());
                List<ProcessReportWorkPageVO> gangData = processData.stream().filter(v -> v.getProcessName().equals("钢化工序")).collect(Collectors.toList());
                if (CollUtil.isNotEmpty(moData) && CollUtil.isNotEmpty(gangData)) {
                    BigDecimal moProcessQuantity = BigDecimalUtil.sum(moData.stream().map(ProcessReportWorkPageVO::getProcessQuantity).collect(Collectors.toList()));
                    BigDecimal gangBadnessQuantity = BigDecimalUtil.sum(gangData.stream().map(ProcessReportWorkPageVO::getBadnessQuantity).collect(Collectors.toList()));
                    BigDecimal gangProcessQuantity = BigDecimalUtil.sum(gangData.stream().map(ProcessReportWorkPageVO::getProcessQuantity).collect(Collectors.toList()));
                    BigDecimal minus = BigDecimalUtil.subtract(gangProcessQuantity, gangBadnessQuantity);
                    //校验是否比1大
                    BigDecimal divide = BigDecimalUtil.divide(minus, moProcessQuantity, 4);
                    if (divide.compareTo(new BigDecimal(1)) == 1) {
                        divide = BigDecimal.ONE;
                    }
                    rowData.add(divide);
                } else {
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
    public SjgCountReportVO countReport(Integer workShopId, LocalDate start, LocalDate end) {
        //构建表头
        LocalDate preEnd = start.minusDays(1);
        long daysBetween = ChronoUnit.DAYS.between(start, end);
        LocalDate preStart = preEnd.minusDays(daysBetween);
        List<TableHeadColumnDTO> columnList1 = setCountReportHead(start, end, preStart, preEnd);
        SjgCountReportVO sjgCountReportVO = new SjgCountReportVO();
        sjgCountReportVO.setColumns(columnList1);
        //查询产线
        List<SysOfficeVO> lineList = pmsOfficeManager.getWsLineList(workShopId);
        SteccnTableAssertUtil.notEmpty(lineList, "车间下暂无产线");
        List<Integer> lineIdList = lineList.stream().map(SysOfficeVO::getId).collect(Collectors.toList());
        //类别：按时间区间内，本车间所有产线订单的报工数据（按工序名）进行分组（仅选择产量大于0的数据）
        List<IdNameDTO> processList = pmsReportWorkMapper.processSet(lineIdList, start, end);
        SteccnTableAssertUtil.notEmpty(processList, "工序集合为空");
        //根据时间和产线获取产量map key - processid_lineid value - (pm(加工数 - 不良数), 加工数)
        Map<String, TupleDTO<BigDecimal, BigDecimal>> currentPmMap = reportWorkService.lineProcessPmMap(lineIdList, start, end);
        Map<String, TupleDTO<BigDecimal, BigDecimal>> prePmMap = reportWorkService.lineProcessPmMap(lineIdList, preStart, preEnd);
        Map<Integer, BigDecimal> currentInStorageMap = reportWorkService.lineInStorage(lineIdList, start, end, PmManagerConstant.MEASURE_UNIT_SM);
        Map<Integer, BigDecimal> preInStorageMap = reportWorkService.lineInStorage(lineIdList, preStart, preEnd, PmManagerConstant.MEASURE_UNIT_SM);
        List<SjgCountReportRowVO> rowList = Lists.newArrayList();
        //构建磨边工序的加工数量
        Map<String, BigDecimal> mbProcessQuantityMap = Maps.newHashMap();
        for (IdNameDTO process : processList) {
            //本周合计，上周合计，产量，成品率
            BigDecimal preWeekProcess = sjgProcessSum(prePmMap, process.getId(), lineList, x -> x.getField1());
            BigDecimal preWeekSum = sjgProcessSum(prePmMap, process.getId(), lineList, x -> x.getField2());
            BigDecimal curWeekProcess = sjgProcessSum(currentPmMap, process.getId(), lineList, x -> x.getField1());
            BigDecimal curWeekSum = sjgProcessSum(currentPmMap, process.getId(), lineList, x -> x.getField2());
            BigDecimal currentValueSumRate = BigDecimalUtil.percent(curWeekProcess, curWeekSum);
            BigDecimal preValueSumRate = BigDecimalUtil.percent(preWeekProcess, preWeekSum);
            BigDecimal valueDiffSum = BigDecimalUtil.subtract(curWeekProcess, preWeekProcess, BigDecimalUtil.SCALE_2);
            BigDecimal valueDiffSumRate = BigDecimalUtil.subtract(currentValueSumRate, preValueSumRate, 2);
            for (SysOfficeVO line : lineList) {
                SjgCountReportRowVO row = new SjgCountReportRowVO();
                row.setLineId(line.getId());
                row.setCategoryName(process.getName());
                row.setLineName(line.getOfficeName());
                row.setCurrentValue(currentPmMap.getOrDefault(process.getId() + "_" + line.getId(), new TupleDTO<>()).getField1());
                BigDecimal currentProcessQuantity = currentPmMap.getOrDefault(process.getId() + "_" + line.getId(), new TupleDTO<>()).getField2();
                row.setCurrentValueRate(BigDecimalUtil.percent(row.getCurrentValue(), currentProcessQuantity));
                row.setPreValue(prePmMap.getOrDefault(process.getId() + "_" + line.getId(), new TupleDTO<>()).getField1());
                BigDecimal preProcessQuantity = prePmMap.getOrDefault(process.getId() + "_" + line.getId(), new TupleDTO<>()).getField2();
                row.setPreValueRate(BigDecimalUtil.percent(row.getPreValue(), preProcessQuantity));
                row.setValueDiff(BigDecimalUtil.subtract(row.getCurrentValue(), row.getPreValue(), 2));
                //产量增长 / 区间产量；保留两位小数；负数则显示为红色，正数为绿色
                row.setValueDiffRate(BigDecimalUtil.subtract(row.getCurrentValueRate(), row.getPreValueRate(), 2));
                row.setCurrentValueSum(curWeekProcess);
                row.setCurrentValueSumRate(currentValueSumRate);
                row.setPreValueSum(preWeekProcess);
                row.setPreValueSumRate(preValueSumRate);
                row.setValueDiffSum(valueDiffSum);
                row.setValueDiffSumRate(valueDiffSumRate);
                if (process.getName().indexOf("磨边") != -1) {
                    MapUtil.add(mbProcessQuantityMap, line.getId() + "_CUR", row.getCurrentValue(), (x, y) -> BigDecimalUtil.add(x, y, BigDecimalUtil.SCALE_2));
                    MapUtil.add(mbProcessQuantityMap, line.getId() + "_PRE", row.getPreValue(), (x, y) -> BigDecimalUtil.add(x, y, BigDecimalUtil.SCALE_2));
                }
                rowList.add(row);
            }
        }
        //入库产量
        //㎡：入库时间介于时间区间内，所有本产线对应订单的入库单（类型为完工入库，排除冲销和删除状态）明细数量汇总；转换为平方米，保留两位小数
        //%：入库产量 / 磨边工序加工数
        BigDecimal preWeekInStorage = sjgInStorageSum(preInStorageMap, lineList, x -> x.getId());
        BigDecimal preWeekMbQuantity = sjgInStorageSum(mbProcessQuantityMap, lineList, x -> x.getId() + "_PRE");
        BigDecimal curWeekInStorage = sjgInStorageSum(currentInStorageMap, lineList, x -> x.getId());
        BigDecimal curWeekMbQuantity = sjgInStorageSum(mbProcessQuantityMap, lineList, x -> x.getId() + "_CUR");
        BigDecimal currentValueSumRate = BigDecimalUtil.percent(curWeekInStorage, curWeekMbQuantity);
        BigDecimal preValueSumRate = BigDecimalUtil.percent(preWeekInStorage, preWeekMbQuantity);
        BigDecimal valueDiffSum = BigDecimalUtil.subtract(curWeekInStorage, preWeekInStorage, BigDecimalUtil.SCALE_2);
        BigDecimal valueDiffSumRate = BigDecimalUtil.subtract(currentValueSumRate, preValueSumRate, 2);
        for (SysOfficeVO line : lineList) {
            SjgCountReportRowVO row = new SjgCountReportRowVO();
            row.setCategoryName("产线入库产量");
            row.setLineId(line.getId());
            row.setLineName(line.getOfficeName());
            row.setCurrentValue(currentInStorageMap.get(line.getId()));
            //入库产量 / 磨边工序加工数
            row.setCurrentValueRate(BigDecimalUtil.percent(row.getCurrentValue(), mbProcessQuantityMap.get(line.getId() + "_CUR")));
            row.setPreValue(preInStorageMap.get(line.getId()));
            row.setPreValueRate(BigDecimalUtil.percent(row.getPreValue(), mbProcessQuantityMap.get(line.getId() + "_PRE")));
            row.setValueDiff(BigDecimalUtil.subtract(row.getCurrentValue(), row.getPreValue(), 2));
            //产量增长 / 区间产量；保留两位小数；负数则显示为红色，正数为绿色
            row.setValueDiffRate(BigDecimalUtil.percent(row.getValueDiff(), row.getCurrentValue()));
            row.setCurrentValueSum(curWeekInStorage);
            row.setCurrentValueSumRate(currentValueSumRate);
            row.setPreValueSum(preWeekInStorage);
            row.setPreValueSumRate(preValueSumRate);
            row.setValueDiffSum(valueDiffSum);
            row.setValueDiffSumRate(valueDiffSumRate);
            rowList.add(row);
        }
        sjgCountReportVO.setRowList(rowList);
        return sjgCountReportVO;
    }

    @Override
    public List<SjgCountReportStopVO> countReportStop(Integer lineId, LocalDate start, LocalDate end) {
        List<SjgCountReportStopVO> resList = Lists.newArrayList();
        //先根据产线，日期区间获取数据
        List<SjgCountReportStopRowVO> dbList = pmsReportWorkHourMapper.countReportStop(lineId, start, end);
        if (CollUtil.isNotEmpty(dbList)) {
            //获取所有的工位
            Set<Integer> stationIdSet = dbList.stream().map(SjgCountReportStopRowVO::getStationId).collect(Collectors.toSet());
            //根据工位和开始日期，结束日期查询标准产能
            List<SysProductionCapacityDetailVO> capacityDetailVOList = FeignOptional.of(() -> sysProductionCapacityFeignApi.capacityStation(new TripleDTO<>(stationIdSet, start, end)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

            Map<String, List<SjgCountReportStopRowVO>> resMap = Maps.newLinkedHashMap();
            //根据map构建返回集合
            dbList.forEach(ele -> {
                MapUtil.compute(resMap, ele.getProcessName(), ele);
            });
            resMap.forEach((k, v) -> {
                SjgCountReportStopVO table = new SjgCountReportStopVO();
                table.setProcessName(k);
                table.setTable(v);
                BigDecimal effectQuantitySum = null;
                BigDecimal manHourSum = null;
                Integer lineNumSum = null;
                for (SjgCountReportStopRowVO ele : v) {
                    ele.setManHour(BigDecimalUtil.divide(ele.getManHour(), new BigDecimal("60"), 1));
                    manHourSum = BigDecimalUtil.add(ele.getManHour(), manHourSum, 1);
                    Optional<SysProductionCapacityDetailVO> first = capacityDetailVOList.stream().filter(aa -> aa.getStationId().equals(ele.getStationId()) && LocalDateUtil.isRange(ele.getClassDate(), aa.getStart(), Objects.nonNull(aa.getEnd()) ? aa.getEnd() : end.plusDays(1), true, false)).findFirst();
                    if (first.isPresent()) {
                        SysProductionCapacityDetailVO capacityDetailVO = first.get();
                        ele.setLineNum(capacityDetailVO.getStationLineNum());
                        lineNumSum = IntegerUtil.add(ele.getLineNum(), lineNumSum);
                        //影响产量/㎡：根据该时间对应的标准产能内，工位产能计算； 工位明细产能  / 24 * 时长,，保留整数
                        BigDecimal multiply = BigDecimalUtil.multiply(ele.getManHour(), capacityDetailVO.getStationStandardCapacity(), BigDecimalUtil.SCALE_8);
                        multiply = BigDecimalUtil.divide(multiply, new BigDecimal("24"), BigDecimalUtil.SCALE_0);
                        ele.setEffectQuantity(multiply);
                        effectQuantitySum = BigDecimalUtil.add(ele.getEffectQuantity(), effectQuantitySum, BigDecimalUtil.SCALE_0);
                    }
                }
                SjgCountReportStopRowVO countRow = new SjgCountReportStopRowVO();
                countRow.setManHour(manHourSum);
                countRow.setEffectQuantity(effectQuantitySum);
                countRow.setLineNum(lineNumSum);
                v.add(countRow);
                resList.add(table);
            });
        }
        return resList;
    }

    @Override
    public SjgMonthCountReportVO monthReport(Integer type, Integer officeId, LocalDate start, LocalDate end) {
        if (type == 1) {
            return monthReportWorkshop(officeId, start, end);
        }
        return monthReportLine(officeId, start, end);
    }

    @Override
    public void monthReportDownload(Integer type, Integer officeId, LocalDate start, LocalDate end, HttpServletResponse response) throws IOException {
        SjgMonthCountReportVO monthReport = monthReport(type, officeId, start, end);
        List<SjgMonthCountReportTransverseVO> leftList = monthReport.getLeftList();
        List<SteccnRow<String, List<BigDecimal>>> rightList = monthReport.getRightList();
        List<String> rightHeadList = monthReport.getRightHeadList();
        //写表头
        List<List<String>> rightHead = new ArrayList<>();
        Arrays.asList("").forEach(ele -> rightHead.add(Lists.newArrayList(ele)));
        rightHeadList.forEach(ele -> rightHead.add(Lists.newArrayList(ele)));
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("深加工月度统计报表", "UTF-8");
        fileName += LocalDateUtil.localDateFormat(LocalDate.now(), "yyyy-MM-dd");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        List<List<Object>> leftOutDataList = Lists.newArrayList();
        //左边的数据
        List<List<String>> leftHead = new ArrayList<>();
        Arrays.asList("线别", "产品类别", "领用/㎡", "入库/㎡", "成品率/%").forEach(ele -> leftHead.add(Lists.newArrayList(ele)));
        leftList.forEach(ele -> {
            List<Object> dataEle = Lists.newArrayList();
            dataEle.add(ele.getOfficeName());
            dataEle.add(ele.getCategoryName());
            dataEle.add(ele.getUseCount());
            dataEle.add(ele.getInStorageCount());
            dataEle.add(ele.getYieldRate());
            leftOutDataList.add(dataEle);
        });

        //右边数据
        List<List<Object>> rightOutDataList = Lists.newArrayList();
        rightList.forEach(ele -> {
            List<Object> dataEle = Lists.newArrayList();
            dataEle.add(ele.getFixRow());
            for (BigDecimal item : ele.getDynamicRow()) {
                dataEle.add(item);
            }
            rightOutDataList.add(dataEle);
        });
        int[] mergeColumnIndex = {0};

        ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream())
                .registerWriteHandler(new RowHeightStyleStrategy(30, 20))
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .registerWriteHandler(new HeadContentStyleStrategy())
                .registerWriteHandler(new ExcelFillCellMergeStrategy(1, mergeColumnIndex))
                .build();

        WriteSheet writeSheet = EasyExcel.writerSheet(0, "深加工月度统计报表").build();

        WriteTable tableOne = EasyExcel.writerTable(0).needHead(Boolean.TRUE).build();
        tableOne.setHead(leftHead);

        WriteTable tableTwo = EasyExcel.writerTable(1).needHead(Boolean.TRUE).build();
        tableTwo.setHead(rightHead);

        excelWriter.write(leftOutDataList, writeSheet, tableOne);
        excelWriter.write(rightOutDataList, writeSheet, tableTwo);

        excelWriter.finish();
        response.flushBuffer();
    }

    /**
     * 深加工月度统计报表 - 产线粒度
     *
     * @param officeId 组织机构主键
     * @param start    开始日期
     * @param end      结束日期
     * @return
     */
    private SjgMonthCountReportVO monthReportWorkshop(Integer officeId, LocalDate start, LocalDate end) {
        SjgMonthCountReportVO vo = new SjgMonthCountReportVO();
        //获取车间的孩子节点-产线
        //如何统一化 产线和车间
        //根据制造部获取车间
        List<SysOfficeVO> workshopList = pmsOfficeManager.getChildren(officeId, SysOfficeConstant.WORKSHOP);
        Assert.notEmpty(workshopList, "制造部下暂无车间");
        List<Integer> workshopIdList = workshopList.stream().map(SysOfficeVO::getId).collect(Collectors.toList());
        Map<Integer, List<Integer>> wsLinkLineMap = pmsOfficeManager.childType(workshopIdList, SysOfficeConstant.PRODUCT_LINE);
        //获取车间下的所有产线
        List<Integer> lineIdList = wsLinkLineMap.values().stream().flatMap(Collection::stream).filter(ele -> Objects.nonNull(ele)).collect(Collectors.toList());
        Assert.notEmpty(lineIdList, "制造部下暂无产线");
        List<Integer> wsIdList = Lists.newArrayList();
        List<String> workshopNameList = Lists.newArrayList();
        workshopList.forEach(ele -> {
            wsIdList.add(ele.getId());
            workshopNameList.add(ele.getOfficeName());
        });
        workshopNameList.add("合计");
        //根据产线获取领用数,区分分类-单位要转换成平方米
        Map<String, BigDecimal> useCountMap = reportWorkService.useCountCategory(lineIdList, start, end, PmManagerConstant.MEASURE_UNIT_SM);
        //入库数：显示平方米，取自完工入库数据，汇总数据累加
        Map<String, BigDecimal> inStorageMap = reportWorkService.inStorageCategory(lineIdList, start, end, PmManagerConstant.MEASURE_UNIT_SM);
        //考虑产线，类别
        Set<String> specSet = Sets.newHashSet(useCountMap.keySet());
        specSet.addAll(inStorageMap.keySet());
        List<String> thickList = getProperty(specSet, 1);
        List<String> productPropertyList = getProperty(specSet, 2);
        List<SjgMonthCountReportTransverseVO> leftList = Lists.newArrayList();
        Map<String, BigDecimal> sumMap1 = Maps.newLinkedHashMap();
        Map<String, BigDecimal> sumMap2 = Maps.newLinkedHashMap();
        for (SysOfficeVO office : workshopList) {
            //根据车间获取产线集合
            List<Integer> lineIdListTmp = wsLinkLineMap.get(office.getId());
            if (Objects.isNull(lineIdListTmp)) {
                lineIdListTmp = Lists.newArrayList();
            }
            boolean flagSum = Boolean.FALSE;
            //组织机构级别的合计
            BigDecimal useCountOfficeSum = null;
            BigDecimal inStorageOfficeSum = null;
            for (String thick : thickList) {
                for (String productProperty : productPropertyList) {
                    BigDecimal useCountSource = null, inStorageSource = null;
                    for (Integer lineId : lineIdListTmp) {
                        useCountSource = BigDecimalUtil.add(useCountSource, useCountMap.get(JointUtil.joint(JointUtil.SEPARATOR_UNDERLINE, lineId, thick, productProperty)));
                        inStorageSource = BigDecimalUtil.add(inStorageSource, inStorageMap.get(JointUtil.joint(JointUtil.SEPARATOR_UNDERLINE, lineId, thick, productProperty)));
                    }
                    BigDecimal useCount = BigDecimalUtil.setScale(useCountSource, 2);
                    BigDecimal inStorage = BigDecimalUtil.setScale(inStorageSource, 2);
                    if (Objects.nonNull(useCount) || Objects.nonNull(inStorage)) {
                        flagSum = Boolean.TRUE;
                        SjgMonthCountReportTransverseVO row = new SjgMonthCountReportTransverseVO();
                        if (!"null".equals(productProperty)) {
                            int productPropertyInt = Integer.parseInt(productProperty);
                            String propertyName = ProductPropertyEnum.property(productPropertyInt).getName();
                            row.setCategoryName(thick + propertyName);
                        } else {
                            row.setCategoryName(thick);
                        }
                        row.setOfficeName(office.getOfficeName());
                        row.setInStorageCount(inStorage);
                        row.setUseCount(useCount);
                        row.setYieldRate(BigDecimalUtil.percent(inStorage, useCount));
                        useCountOfficeSum = BigDecimalUtil.add(useCountOfficeSum, row.getUseCount());
                        inStorageOfficeSum = BigDecimalUtil.add(inStorageOfficeSum, row.getInStorageCount());
                        MapUtil.add(sumMap1, row.getCategoryName(), row.getUseCount(), (x, y) -> BigDecimalUtil.add(x, y, 2));
                        MapUtil.add(sumMap2, row.getCategoryName(), row.getInStorageCount(), (x, y) -> BigDecimalUtil.add(x, y, 2));
                        leftList.add(row);
                    }
                }
            }
            if (flagSum) {
                //添加组织机构级别的合计
                SjgMonthCountReportTransverseVO officeSum = new SjgMonthCountReportTransverseVO();
                officeSum.setOfficeName(office.getOfficeName());
                officeSum.setCategoryName("合计");
                officeSum.setUseCount(useCountOfficeSum);
                officeSum.setInStorageCount(inStorageOfficeSum);
                officeSum.setYieldRate(BigDecimalUtil.percent(inStorageOfficeSum, useCountOfficeSum));
                leftList.add(officeSum);
            }
        }
        //合计
        BigDecimal useCountOfficeTotalSum = null;
        BigDecimal inStorageOfficeTotalSum = null;
        for (Map.Entry<String, BigDecimal> entry : sumMap1.entrySet()) {
            SjgMonthCountReportTransverseVO officeTotal = new SjgMonthCountReportTransverseVO();
            officeTotal.setOfficeName("合计");
            officeTotal.setCategoryName(entry.getKey());
            officeTotal.setUseCount(entry.getValue());
            officeTotal.setInStorageCount(sumMap2.get(entry.getKey()));
            officeTotal.setYieldRate(BigDecimalUtil.percent(officeTotal.getInStorageCount(), officeTotal.getUseCount()));
            useCountOfficeTotalSum = BigDecimalUtil.add(useCountOfficeTotalSum, officeTotal.getUseCount());
            inStorageOfficeTotalSum = BigDecimalUtil.add(inStorageOfficeTotalSum, officeTotal.getInStorageCount());
            leftList.add(officeTotal);
        }
        //添加合计的合计
        //添加组织机构级别的合计
        SjgMonthCountReportTransverseVO officeTotalSum = new SjgMonthCountReportTransverseVO();
        officeTotalSum.setOfficeName("合计");
        officeTotalSum.setCategoryName("合计");
        officeTotalSum.setUseCount(useCountOfficeTotalSum);
        officeTotalSum.setInStorageCount(inStorageOfficeTotalSum);
        officeTotalSum.setYieldRate(BigDecimalUtil.percent(officeTotalSum.getInStorageCount(), officeTotalSum.getUseCount()));
        leftList.add(officeTotalSum);

        vo.setLeftList(leftList);
        vo.setRightHeadList(workshopNameList);
        //获取右边的数据
        CompletableFuture<Map<String, BigDecimal>> elecFuture = CompletableFuture.supplyAsync(() -> {
            return FeignOptional.of(() -> remoteMeterReadFeignApi.energyConsumeOffice(new Tuple5DTO<>(1, wsIdList, start, end, QtManagerConstant.QtGranularityEnum.NONE.getCode())), FeignConstant.STECCN_EMS_SERVICE_NAME).get();
        });
        CompletableFuture<Map<String, BigDecimal>> waterFuture = CompletableFuture.supplyAsync(() -> {
            return FeignOptional.of(() -> remoteMeterReadFeignApi.energyConsumeOffice(new Tuple5DTO<>(2, wsIdList, start, end, QtManagerConstant.QtGranularityEnum.NONE.getCode())), FeignConstant.STECCN_EMS_SERVICE_NAME).get();
        });
        CompletableFuture<Map<String, BigDecimal>> unitConsumeFuture = CompletableFuture.supplyAsync(() -> {
            return FeignOptional.of(() -> remoteMeterReadFeignApi.unitConsumeOffice(new Tuple5DTO<>(1, wsIdList, start, end, QtManagerConstant.QtGranularityEnum.NONE.getCode())), FeignConstant.STECCN_EMS_SERVICE_NAME).get();
        });
        CompletableFuture<List<TupleDTO<String, Map<Integer, BigDecimal>>>> materialConsumeListFuture = CompletableFuture.supplyAsync(() -> {
            return pmsCompleteDeliveryService.materialConsumption(new Tuple5DTO<>(officeId, start, end, QtManagerConstant.QtGranularityEnum.NONE.getCode(), 0));
        });

        CompletableFuture<Map<Integer, BigDecimal>> mbMapFuture = CompletableFuture.supplyAsync(() -> {
            return reportWorkService.processYieldWorkshop(wsIdList, start, end, "MBGX");
        });
        CompletableFuture<Map<Integer, BigDecimal>> dkMapFuture = CompletableFuture.supplyAsync(() -> {
            return reportWorkService.processYieldWorkshop(wsIdList, start, end, "DKGX");
        });
        CompletableFuture<Map<Integer, BigDecimal>> syMapFuture = CompletableFuture.supplyAsync(() -> {
            return reportWorkService.processYieldWorkshop(wsIdList, start, end, "SYGX");
        });
        CompletableFuture<Map<Integer, BigDecimal>> dmMapFuture = CompletableFuture.supplyAsync(() -> {
            return reportWorkService.processYieldWorkshop(wsIdList, start, end, "DMGX");
        });
        CompletableFuture<Map<Integer, BigDecimal>> ghMapFuture = CompletableFuture.supplyAsync(() -> {
            return reportWorkService.processYieldWorkshop(wsIdList, start, end, "GHGX");
        });
        CompletableFuture<Void> completableFuture = CompletableFuture.allOf(elecFuture, waterFuture, unitConsumeFuture, materialConsumeListFuture,
                mbMapFuture, dkMapFuture, syMapFuture, dmMapFuture, ghMapFuture);
        completableFuture.isDone();

        List<SteccnRow<String, List<BigDecimal>>> rightList = Lists.newArrayList();
        String convert = MysqlDateKeyUtil.convert(QtManagerConstant.QtGranularityEnum.NONE, start);
        rightList.add(monthRow("电量/kWh", elecFuture.join(), workshopList, convert));
        rightList.add(monthRow("电单耗/kWh/㎡", unitConsumeFuture.join(), workshopList, convert));
        rightList.add(monthRow("水量/t", waterFuture.join(), workshopList, convert));
        //集成物料消耗
        List<TupleDTO<String, Map<Integer, BigDecimal>>> materialConsumeList = materialConsumeListFuture.join();
        if (CollUtil.isNotEmpty(materialConsumeList)) {
            materialConsumeList.forEach(ele -> {
                rightList.add(monthConsumeRow(ele.getField1(), ele.getField2(), workshopList));
            });
        }
        //工序成品率： 磨边成品率，打孔成品率，丝印成品率，镀膜成品率，钢化成品率
        //合格数/加工数，以百分比展示
        rightList.add(monthConsumeRow("磨边成品率/%", mbMapFuture.join(), workshopList));
        rightList.add(monthConsumeRow("打孔成品率/%", dkMapFuture.join(), workshopList));
        rightList.add(monthConsumeRow("丝印成品率/%", syMapFuture.join(), workshopList));
        rightList.add(monthConsumeRow("镀膜成品率/%", dmMapFuture.join(), workshopList));
        rightList.add(monthConsumeRow("钢化成品率/%", ghMapFuture.join(), workshopList));
        vo.setRightList(rightList);
        return vo;
    }

    /**
     * 深加工月度统计报表 - 产线粒度
     *
     * @param officeId 组织机构主键
     * @param start    开始日期
     * @param end      结束日期
     * @return
     */
    private SjgMonthCountReportVO monthReportLine(Integer officeId, LocalDate start, LocalDate end) {
        SjgMonthCountReportVO vo = new SjgMonthCountReportVO();
        //获取车间的孩子节点-产线
        //如何统一化 产线和车间
        List<SysOfficeVO> wsLineList = pmsOfficeManager.getWsLineList(officeId);
        Assert.notEmpty(wsLineList, "车间下暂无产线");
        List<Integer> lineIdList = Lists.newArrayList();
        List<String> lineNameList = Lists.newArrayList();
        wsLineList.forEach(ele -> {
            lineIdList.add(ele.getId());
            lineNameList.add(ele.getOfficeName());
        });
        lineNameList.add("合计");
        //根据产线获取领用数,区分分类-单位要转换成平方米
        Map<String, BigDecimal> useCountMap = reportWorkService.useCountCategory(lineIdList, start, end, PmManagerConstant.MEASURE_UNIT_SM);
        //入库数：显示平方米，取自完工入库数据，汇总数据累加
        Map<String, BigDecimal> inStorageMap = reportWorkService.inStorageCategory(lineIdList, start, end, PmManagerConstant.MEASURE_UNIT_SM);
        //考虑产线，类别
        Set<String> specSet = Sets.newHashSet(useCountMap.keySet());
        specSet.addAll(inStorageMap.keySet());
        List<String> thickList = getProperty(specSet, 1);
        List<String> productPropertyList = getProperty(specSet, 2);
        List<SjgMonthCountReportTransverseVO> leftList = Lists.newArrayList();
        Map<String, BigDecimal> sumMap1 = Maps.newLinkedHashMap();
        Map<String, BigDecimal> sumMap2 = Maps.newLinkedHashMap();
        for (SysOfficeVO office : wsLineList) {
            boolean flagSum = Boolean.FALSE;
            //组织机构级别的合计
            BigDecimal useCountOfficeSum = null;
            BigDecimal inStorageOfficeSum = null;
            for (String thick : thickList) {
                for (String productProperty : productPropertyList) {
                    BigDecimal useCountSource = useCountMap.get(JointUtil.joint(JointUtil.SEPARATOR_UNDERLINE, office.getId(), thick, productProperty));
                    BigDecimal inStorageSource = inStorageMap.get(JointUtil.joint(JointUtil.SEPARATOR_UNDERLINE, office.getId(), thick, productProperty));
                    BigDecimal useCount = BigDecimalUtil.setScale(useCountSource, 2);
                    BigDecimal inStorage = BigDecimalUtil.setScale(inStorageSource, 2);
                    if (Objects.nonNull(useCount) || Objects.nonNull(inStorage)) {
                        flagSum = Boolean.TRUE;
                        SjgMonthCountReportTransverseVO row = new SjgMonthCountReportTransverseVO();
                        if (!"null".equals(productProperty)) {
                            int productPropertyInt = Integer.parseInt(productProperty);
                            String propertyName = ProductPropertyEnum.property(productPropertyInt).getName();
                            row.setCategoryName(thick + propertyName);
                        } else {
                            row.setCategoryName(thick);
                        }
                        row.setOfficeName(office.getOfficeName());
                        row.setInStorageCount(inStorage);
                        row.setUseCount(useCount);
                        row.setYieldRate(BigDecimalUtil.percent(inStorage, useCount));
                        useCountOfficeSum = BigDecimalUtil.add(useCountOfficeSum, row.getUseCount());
                        inStorageOfficeSum = BigDecimalUtil.add(inStorageOfficeSum, row.getInStorageCount());
                        MapUtil.add(sumMap1, row.getCategoryName(), row.getUseCount(), (x, y) -> BigDecimalUtil.add(x, y, 2));
                        MapUtil.add(sumMap2, row.getCategoryName(), row.getInStorageCount(), (x, y) -> BigDecimalUtil.add(x, y, 2));
                        leftList.add(row);
                    }
                }
            }
            if (flagSum) {
                //添加组织机构级别的合计
                SjgMonthCountReportTransverseVO officeSum = new SjgMonthCountReportTransverseVO();
                officeSum.setOfficeName(office.getOfficeName());
                officeSum.setCategoryName("合计");
                officeSum.setUseCount(useCountOfficeSum);
                officeSum.setInStorageCount(inStorageOfficeSum);
                officeSum.setYieldRate(BigDecimalUtil.percent(inStorageOfficeSum, useCountOfficeSum));
                leftList.add(officeSum);
            }
        }
        //合计
        BigDecimal useCountOfficeTotalSum = null;
        BigDecimal inStorageOfficeTotalSum = null;
        for (Map.Entry<String, BigDecimal> entry : sumMap1.entrySet()) {
            SjgMonthCountReportTransverseVO officeTotal = new SjgMonthCountReportTransverseVO();
            officeTotal.setOfficeName("合计");
            officeTotal.setCategoryName(entry.getKey());
            officeTotal.setUseCount(entry.getValue());
            officeTotal.setInStorageCount(sumMap2.get(entry.getKey()));
            officeTotal.setYieldRate(BigDecimalUtil.percent(officeTotal.getInStorageCount(), officeTotal.getUseCount()));
            useCountOfficeTotalSum = BigDecimalUtil.add(useCountOfficeTotalSum, officeTotal.getUseCount());
            inStorageOfficeTotalSum = BigDecimalUtil.add(inStorageOfficeTotalSum, officeTotal.getInStorageCount());
            leftList.add(officeTotal);
        }
        //添加合计的合计
        //添加组织机构级别的合计
        SjgMonthCountReportTransverseVO officeTotalSum = new SjgMonthCountReportTransverseVO();
        officeTotalSum.setOfficeName("合计");
        officeTotalSum.setCategoryName("合计");
        officeTotalSum.setUseCount(useCountOfficeTotalSum);
        officeTotalSum.setInStorageCount(inStorageOfficeTotalSum);
        officeTotalSum.setYieldRate(BigDecimalUtil.percent(officeTotalSum.getInStorageCount(), officeTotalSum.getUseCount()));
        leftList.add(officeTotalSum);

        vo.setLeftList(leftList);
        vo.setRightHeadList(lineNameList);
        //获取右边的数据
        CompletableFuture<Map<String, BigDecimal>> elecFuture = CompletableFuture.supplyAsync(() -> {
            return FeignOptional.of(() -> remoteMeterReadFeignApi.energyConsumeOffice(new Tuple5DTO<>(1, lineIdList, start, end, QtManagerConstant.QtGranularityEnum.NONE.getCode())), FeignConstant.STECCN_EMS_SERVICE_NAME).get();
        });
        CompletableFuture<Map<String, BigDecimal>> unitConsumeFuture = CompletableFuture.supplyAsync(() -> {
            return FeignOptional.of(() -> remoteMeterReadFeignApi.unitConsumeOffice(new Tuple5DTO<>(1, lineIdList, start, end, QtManagerConstant.QtGranularityEnum.NONE.getCode())), FeignConstant.STECCN_EMS_SERVICE_NAME).get();
        });
        CompletableFuture<List<TupleDTO<String, Map<Integer, BigDecimal>>>> materialConsumeListFuture = CompletableFuture.supplyAsync(() -> {
            return pmsCompleteDeliveryService.materialConsumption(new Tuple5DTO<>(officeId, start, end, QtManagerConstant.QtGranularityEnum.NONE.getCode(), 1));
        });

        CompletableFuture<Map<Integer, BigDecimal>> mbMapFuture = CompletableFuture.supplyAsync(() -> {
            return reportWorkService.processYieldLine(lineIdList, start, end, "MBGX");
        });
        CompletableFuture<Map<Integer, BigDecimal>> dkMapFuture = CompletableFuture.supplyAsync(() -> {
            return reportWorkService.processYieldLine(lineIdList, start, end, "DKGX");
        });
        CompletableFuture<Map<Integer, BigDecimal>> syMapFuture = CompletableFuture.supplyAsync(() -> {
            return reportWorkService.processYieldLine(lineIdList, start, end, "SYGX");
        });
        CompletableFuture<Map<Integer, BigDecimal>> dmMapFuture = CompletableFuture.supplyAsync(() -> {
            return reportWorkService.processYieldLine(lineIdList, start, end, "DMGX");
        });
        CompletableFuture<Map<Integer, BigDecimal>> ghMapFuture = CompletableFuture.supplyAsync(() -> {
            return reportWorkService.processYieldLine(lineIdList, start, end, "GHGX");
        });
        CompletableFuture<Void> completableFuture = CompletableFuture.allOf(elecFuture, unitConsumeFuture, materialConsumeListFuture,
                mbMapFuture, dkMapFuture, syMapFuture, dmMapFuture, ghMapFuture);
        completableFuture.isDone();

        List<SteccnRow<String, List<BigDecimal>>> rightList = Lists.newArrayList();
        String convert = MysqlDateKeyUtil.convert(QtManagerConstant.QtGranularityEnum.NONE, start);
        rightList.add(monthRow("电量/kWh", elecFuture.join(), wsLineList, convert));
        rightList.add(monthRow("电单耗/kWh/㎡", unitConsumeFuture.join(), wsLineList, convert));
        //集成物料消耗
        List<TupleDTO<String, Map<Integer, BigDecimal>>> materialConsumeList = materialConsumeListFuture.join();
        if (CollUtil.isNotEmpty(materialConsumeList)) {
            materialConsumeList.forEach(ele -> {
                rightList.add(monthConsumeRow(ele.getField1(), ele.getField2(), wsLineList));
            });
        }
        //工序成品率： 磨边成品率，打孔成品率，丝印成品率，镀膜成品率，钢化成品率
        //合格数/加工数，以百分比展示
        rightList.add(monthConsumeRow("磨边成品率/%", mbMapFuture.join(), wsLineList));
        rightList.add(monthConsumeRow("打孔成品率/%", dkMapFuture.join(), wsLineList));
        rightList.add(monthConsumeRow("丝印成品率/%", syMapFuture.join(), wsLineList));
        rightList.add(monthConsumeRow("镀膜成品率/%", dmMapFuture.join(), wsLineList));
        rightList.add(monthConsumeRow("钢化成品率/%", ghMapFuture.join(), wsLineList));
        vo.setRightList(rightList);
        return vo;
    }

    /**
     * 异丙醇/镀膜液/油墨
     *
     * @param info       信息对象
     * @param map        数据对象
     * @param wsLineList 产线集合
     * @return
     */
    private SteccnRow<String, List<BigDecimal>> monthConsumeRow(String info, Map<Integer, BigDecimal> map, List<SysOfficeVO> wsLineList) {
        SteccnRow<String, List<BigDecimal>> row = new SteccnRow<>();
        row.setFixRow(info);
        List<BigDecimal> valueList = Lists.newArrayList();
        wsLineList.forEach(ele -> {
            valueList.add(map.get(ele.getId()));
        });
        valueList.add(map.get(-1));
        row.setDynamicRow(valueList);
        return row;
    }


    /**
     * 深加工月度统计报表-转换
     *
     * @param category   类别
     * @param valueMap   值map
     * @param wsLineList 产线主键集合
     * @param convert    粒度转换
     * @return
     */
    private SteccnRow<String, List<BigDecimal>> monthRow(String category, Map<String, BigDecimal> valueMap, List<SysOfficeVO> wsLineList, String convert) {
        SteccnRow<String, List<BigDecimal>> row = new SteccnRow<>();
        row.setFixRow(category);
        //避免空指针异常
        if (Objects.isNull(valueMap)) {
            valueMap = Maps.newHashMap();
        }
        List<BigDecimal> valueList = Lists.newArrayList();
        BigDecimal columnSum = null;
        for (SysOfficeVO office : wsLineList) {
            BigDecimal value = valueMap.get(office.getId() + "_" + convert);
            valueList.add(value);
            columnSum = BigDecimalUtil.add(columnSum, value);
        }
        valueList.add(columnSum);
        row.setDynamicRow(valueList);
        return row;
    }


    /**
     * 从规格字符串中获取厚度||产品属性
     *
     * @param specSet 规格字符串
     * @param idx     索引
     * @return
     */
    private List<String> getProperty(Set<String> specSet, Integer idx) {
        Set<String> propertySet = Sets.newHashSet();
        if (CollUtil.isNotEmpty(specSet)) {
            specSet.forEach(ele -> {
                String[] split = ele.split("_");
                propertySet.add(split[idx]);
            });
        }
        List<String> resList = Lists.newArrayList(propertySet);
        Collections.sort(resList);
        return resList;
    }

    /**
     * 深加工工序汇总
     *
     * @param valueMap  值集合
     * @param processId 工序主键
     * @param lineList  产线集合
     * @param function  函数
     * @return
     */
    private BigDecimal sjgProcessSum(Map<String, TupleDTO<BigDecimal, BigDecimal>> valueMap, Integer processId, List<SysOfficeVO> lineList, Function<TupleDTO<BigDecimal, BigDecimal>, BigDecimal> function) {
        List<BigDecimal> sumList = Lists.newArrayList();
        lineList.forEach(line -> {
            TupleDTO<BigDecimal, BigDecimal> tupleDTO = valueMap.getOrDefault(processId + "_" + line.getId(), new TupleDTO<>());
            sumList.add(function.apply(tupleDTO));
        });
        return BigDecimalUtil.sum(sumList);
    }

    /**
     * 深加工入库数量汇总
     *
     * @param valueMap 值集合
     * @param lineList 产线集合
     * @return
     */
    private BigDecimal sjgInStorageSum(Map<? extends Object, BigDecimal> valueMap, List<SysOfficeVO> lineList, Function<SysOfficeVO, Object> function) {
        List<BigDecimal> sumList = Lists.newArrayList();
        lineList.forEach(line -> {
            sumList.add(valueMap.get(function.apply(line)));
        });
        return BigDecimalUtil.sum(sumList);
    }

    /**
     * 设置深加工统计报表头
     *
     * @param start    开始日期
     * @param end      结束日期
     * @param preStart 前区间开始日期
     * @param preEnd   前区间结束日期
     * @return
     */
    private List<TableHeadColumnDTO> setCountReportHead(LocalDate start, LocalDate end, LocalDate preStart, LocalDate preEnd) {
        String currentStartStr = LocalDateUtil.format(start, "MM.dd");
        String currentEndStr = LocalDateUtil.format(end, "MM.dd");
        String preStartStr = LocalDateUtil.format(preStart, "MM.dd");
        String preEndStr = LocalDateUtil.format(preEnd, "MM.dd");
        List<TableHeadColumnDTO> resList = Lists.newArrayList();
        resList.add(new TableHeadColumnDTO("类别", "categoryName"));
        resList.add(new TableHeadColumnDTO("线别", "lineName"));
        resList.add(new TableHeadColumnDTO("本区间产量" + currentStartStr + "-" + currentEndStr, "currentValueTuple", Arrays.asList(new TableHeadColumnDTO("㎡", "currentValue"), new TableHeadColumnDTO("%", "currentValueRate"))));
        resList.add(new TableHeadColumnDTO("上区间产量" + preStartStr + "-" + preEndStr, "preValueTuple", Arrays.asList(new TableHeadColumnDTO("㎡", "preValue"), new TableHeadColumnDTO("%", "preValueRate"))));
        resList.add(new TableHeadColumnDTO("产量增长", "valueDiffSingle", Arrays.asList(new TableHeadColumnDTO("(+/-)", "valueDiff"))));
        resList.add(new TableHeadColumnDTO("产量增长率", "valueDiffRateSingle", Arrays.asList(new TableHeadColumnDTO("(+/-)", "valueDiffRate"))));
        resList.add(new TableHeadColumnDTO("本区间合计", "currentValueSumTuple", Arrays.asList(new TableHeadColumnDTO("㎡", "currentValueSum"), new TableHeadColumnDTO("%", "currentValueSumRate"))));
        resList.add(new TableHeadColumnDTO("上区间合计", "preValueSumTuple", Arrays.asList(new TableHeadColumnDTO("㎡", "preValueSum"), new TableHeadColumnDTO("%", "preValueSumRate"))));
        resList.add(new TableHeadColumnDTO("汇总产量增长", "valueDiffSumSingle", Arrays.asList(new TableHeadColumnDTO("(+/-)", "valueDiffSum"))));
        resList.add(new TableHeadColumnDTO("汇总产量增长率", "valueDiffSumRateSingle", Arrays.asList(new TableHeadColumnDTO("(+/-)", "valueDiffSumRate"))));
        return resList;
    }

}
