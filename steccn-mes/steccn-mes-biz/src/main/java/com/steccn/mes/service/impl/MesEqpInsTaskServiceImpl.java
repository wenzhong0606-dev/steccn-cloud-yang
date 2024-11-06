package com.steccn.mes.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.*;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.util.StackGraphUtil;
import com.steccn.dp.dto.DpBusiMonitorDTO;
import com.steccn.dp.feign.RemoteBusiMonitorFeignApi;
import com.steccn.mes.constant.MesManagerConstant;
import com.steccn.mes.domain.JsSysOffice;
import com.steccn.mes.domain.MesEqpInsPlanSchedule;
import com.steccn.mes.domain.MesEqpInsTask;
import com.steccn.mes.dto.MesInsTaskCountDTO;
import com.steccn.mes.dto.MesInsTaskParamDTO;
import com.steccn.mes.dto.MesInsTaskTmpDTO;
import com.steccn.mes.dto.MesInsTaskTmpParamDTO;
import com.steccn.mes.mapper.JsSysOfficeMapper;
import com.steccn.mes.mapper.MesEqpInsPlanScheduleMapper;
import com.steccn.mes.service.MesEqpInsTaskService;
import com.steccn.mes.mapper.MesEqpInsTaskMapper;
import com.steccn.mes.vo.MesItemIdNameVO;
import com.steccn.mes.vo.MesTempLineXDataVO;
import com.steccn.oa.constant.OaConstant;
import com.steccn.oa.feign.OaInfoService;
import com.steccn.oa.vo.OaTodayClassVO;
import com.steccn.platform.constant.SysMsgConstant;
import com.steccn.platform.dto.SysMsgSendDTO;
import com.steccn.platform.feign.RemoteMsgFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysMsgAdditionDTO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.vo.BaseReportHeaderVO;
import com.steccn.pms.vo.LineProduceClassGroupVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.websocket.constant.WebsocketConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.http.util.Asserts;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Admin
 * @description 针对表【mes_eqp_ins_task(点检任务表)】的数据库操作Service实现
 * @createDate 2023-09-08 14:56:45
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MesEqpInsTaskServiceImpl extends ServiceImpl<MesEqpInsTaskMapper, MesEqpInsTask>
        implements MesEqpInsTaskService {
    private final MesEqpInsTaskMapper mesEqpInsTaskMapper;
    private final JsSysOfficeMapper jsSysOfficeMapper;
    private final RemoteOfficeFeign remoteOfficeFeign;
    private final OaInfoService oaInfoService;
    private final MesEqpInsPlanScheduleMapper mesEqpInsPlanScheduleMapper;
    private final RemoteBusiMonitorFeignApi remoteBusiMonitorFeignApi;
    private final RemoteMsgFeignApi remoteMsgFeignApi;

    @Override
    public QtLineChartBarDTO taskComplete(LocalDateTime startTime, LocalDateTime endTime, Integer type) {
        //获取数据
        MesInsTaskParamDTO dto = new MesInsTaskParamDTO();
        dto.setStartTime(startTime);
        dto.setEndTime(endTime);
        dto.setStatusNe(OaConstant.NUM_1);
        //所有任务数据
        List<MesInsTaskCountDTO> allTask = mesEqpInsTaskMapper.findTaskCount(dto);
        Assert.notEmpty(allTask, "数据为空");
        Map<String, Long> allTaskMap = allTask.stream().filter(v -> v.getOfficeCode() != null)
                .collect(Collectors.toMap(
                        MesInsTaskCountDTO::getOfficeCode,
                        MesInsTaskCountDTO::getNum
                ));
        List<String> officeCodes = allTask.stream().filter(v -> v.getOfficeCode() != null)
                .map(MesInsTaskCountDTO::getOfficeCode).collect(Collectors.toList());
        dto.setStatusNe(null);
        dto.setStatus(OaConstant.NO);
        //完成任务数据
        List<MesInsTaskCountDTO> finishedTask = mesEqpInsTaskMapper.findTaskCount(dto);
        Map<String, Long> finishedTaskMap = finishedTask.stream().filter(v -> v.getOfficeCode() != null)
                .collect(Collectors.toMap(
                        MesInsTaskCountDTO::getOfficeCode,
                        MesInsTaskCountDTO::getNum
                ));
        //组成图表数据
        List<JsSysOffice> jsSysOffices = jsSysOfficeMapper.selectList(new LambdaQueryWrapper<JsSysOffice>()
                .select(JsSysOffice::getOfficeCode, JsSysOffice::getOfficeName, JsSysOffice::getParentCodes)
                .in(JsSysOffice::getOfficeCode, officeCodes).eq(JsSysOffice::getStatus, QtManagerConstant.NUM_0));
//        Collections.sort(jsSysOffices, Comparator.comparing(JsSysOffice::getOfficeName));
        //type = 0 车间粒度
        List<JsSysOffice> officeList = Lists.newArrayList();
        if (type.equals(OaConstant.NO)) {
            officeList = jsSysOfficeMapper.selectList(new LambdaQueryWrapper<JsSysOffice>()
                    .select(JsSysOffice::getOfficeCode, JsSysOffice::getOfficeName)
                    .eq(JsSysOffice::getOfficeType, QtManagerConstant.NUM_8)
                    .eq(JsSysOffice::getStatus, QtManagerConstant.NUM_0)
                    .orderByAsc(JsSysOffice::getTreeSort));
        } else {
            //根据车间id获取车间
            Map<Integer, SysOfficeVO> integerSysOfficeVOMap = FeignOptional.of(() -> remoteOfficeFeign.listById(Sets.newHashSet(type)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            SysOfficeVO sysOfficeVO = integerSysOfficeVOMap.get(type);
            officeList = jsSysOfficeMapper.selectList(new LambdaQueryWrapper<JsSysOffice>()
                    .select(JsSysOffice::getOfficeCode, JsSysOffice::getOfficeName)
                    .like(JsSysOffice::getParentCodes, sysOfficeVO.getOfficeCode())
                    .eq(JsSysOffice::getStatus, QtManagerConstant.NUM_0)
                    .orderByAsc(JsSysOffice::getTreeSort));
            Assert.notEmpty(officeList, "该车间下没有机构");
            officeList = officeList.stream().filter(v -> officeCodes.contains(v.getOfficeCode())).collect(Collectors.toList());
        }
        Assert.notEmpty(officeList, "数据为空");
        List<StackGraphDTO<String, BigDecimal>> dataList = Lists.newArrayList();
        officeList.forEach(ele -> {
            String officeName = ele.getOfficeName();
            //聚合粒度下所有的数据
            List<JsSysOffice> officeTmp = Lists.newArrayList();
            if (type.equals(OaConstant.NO)) {
                officeTmp = jsSysOffices.stream().filter(v -> v.getParentCodes().contains(ele.getOfficeCode())).collect(Collectors.toList());
            } else {
                officeTmp = jsSysOffices.stream().filter(v -> v.getOfficeCode().equals(ele.getOfficeCode())).collect(Collectors.toList());
            }
            if (CollUtil.isNotEmpty(officeTmp)) {
                //加总产线下所有 1任务总数 2完成数
                Map<Integer, BigDecimal> countMap = Maps.newHashMap();
                officeTmp.forEach(tmp -> {
                    //任务总数
                    BigDecimalUtil.accumulate(countMap, 1, new BigDecimal(allTaskMap.get(tmp.getOfficeCode())));
                    //完成数
                    BigDecimalUtil.accumulate(countMap, 2, new BigDecimal(finishedTaskMap.getOrDefault(tmp.getOfficeCode(), 0L)));
                });
                if (!officeName.equals("原料二车间")) {
                    //组数据
                    StackGraphDTO<String, BigDecimal> stackGraphDTO = new StackGraphDTO<>();
                    stackGraphDTO.setXAxis(officeName);
                    List<TupleDTO<String, BigDecimal>> datList = Lists.newArrayList();
                    BigDecimal all = countMap.get(1);
                    BigDecimal finished = countMap.get(2);
                    datList.add(new TupleDTO<>("任务总数", all));
                    datList.add(new TupleDTO<>("完成数", finished));
                    datList.add(new TupleDTO<>("完成率", BigDecimalUtil.percent(finished, all)));
                    stackGraphDTO.setData(datList);
                    dataList.add(stackGraphDTO);
                }
            }
        });
        return StackGraphUtil.convertData(dataList, Boolean.TRUE);
    }

    @Override
    public void taskCompleteMonitor(LocalDate date, String alarmCode) {
        //根据告警code获取
        List<DpBusiMonitorDTO> passRateList = FeignOptional.of(() -> remoteBusiMonitorFeignApi.getBusiMonitorAlarm(alarmCode), FeignConstant.STECCN_DP_SERVICE_NAME).get();
        if (CollUtil.isEmpty(passRateList)) {
            return;
        }
        //获取数据
        MesInsTaskParamDTO dto = new MesInsTaskParamDTO();
        dto.setEndTime(LocalDateTime.of(date, LocalTime.MIN));
        dto.setStartTime(dto.getEndTime().minusDays(1));
        dto.setEndTime(dto.getEndTime().minusMinutes(1L));
        dto.setStatusNe(OaConstant.NUM_1);
        //所有任务数据
        List<MesInsTaskCountDTO> allTask = mesEqpInsTaskMapper.findTaskCount(dto);
        Assert.notEmpty(allTask, "数据为空");
        Map<String, Long> allTaskMap = allTask.stream().filter(v -> v.getOfficeCode() != null).collect(Collectors.toMap(MesInsTaskCountDTO::getOfficeCode, MesInsTaskCountDTO::getNum));
        List<String> officeCodes = allTask.stream().filter(v -> v.getOfficeCode() != null).map(MesInsTaskCountDTO::getOfficeCode).collect(Collectors.toList());
        dto.setStatusNe(null);
        dto.setStatus(OaConstant.NO);
        //完成任务数据
        List<MesInsTaskCountDTO> finishedTask = mesEqpInsTaskMapper.findTaskCount(dto);
        Map<String, Long> finishedTaskMap = finishedTask.stream().filter(v -> v.getOfficeCode() != null)
                .collect(Collectors.toMap(
                        MesInsTaskCountDTO::getOfficeCode,
                        MesInsTaskCountDTO::getNum
                ));
        //组成图表数据
        List<JsSysOffice> jsSysOffices = jsSysOfficeMapper.selectList(new LambdaQueryWrapper<JsSysOffice>()
                .select(JsSysOffice::getOfficeCode, JsSysOffice::getOfficeName, JsSysOffice::getParentCodes)
                .in(JsSysOffice::getOfficeCode, officeCodes).eq(JsSysOffice::getStatus, QtManagerConstant.NUM_0));
        List<JsSysOffice> officeList = jsSysOfficeMapper.selectList(new LambdaQueryWrapper<JsSysOffice>()
                .select(JsSysOffice::getOfficeCode, JsSysOffice::getOfficeName)
                .eq(JsSysOffice::getOfficeType, QtManagerConstant.NUM_8)
                .eq(JsSysOffice::getStatus, QtManagerConstant.NUM_0)
                .orderByAsc(JsSysOffice::getTreeSort));
        Assert.notEmpty(officeList, "数据为空");
        Map<String, BigDecimal> finishRateMap = Maps.newHashMap();
        officeList.forEach(ele -> {
            String officeName = ele.getOfficeName();
            //聚合粒度下所有的数据
            List<JsSysOffice> officeTmp = jsSysOffices.stream().filter(v -> v.getParentCodes().contains(ele.getOfficeCode())).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(officeTmp)) {
                //加总产线下所有 1任务总数 2完成数
                Map<Integer, BigDecimal> countMap = Maps.newHashMap();
                officeTmp.forEach(tmp -> {
                    //任务总数
                    BigDecimalUtil.accumulate(countMap, 1, new BigDecimal(allTaskMap.get(tmp.getOfficeCode())));
                    //完成数
                    BigDecimalUtil.accumulate(countMap, 2, new BigDecimal(finishedTaskMap.getOrDefault(tmp.getOfficeCode(), 0L)));
                });
                if (!officeName.equals("原料二车间")) {
                    BigDecimal all = countMap.get(1);
                    BigDecimal finished = countMap.get(2);
                    BigDecimal finishRate = BigDecimalUtil.percent(finished, all);
                    finishRateMap.put(ele.getOfficeCode(), finishRate);
                }
            }
        });
        for (DpBusiMonitorDTO monitor : passRateList) {
            if (CollUtil.isNotEmpty(monitor.getMonitorRange())) {
                StringBuilder contentSb = new StringBuilder();
                BigDecimal upValue = monitor.getUpValue();
                BigDecimal lowValue = monitor.getLowValue();
                Map<Integer, SysOfficeVO> officeIdMap = FeignOptional.of(() -> remoteOfficeFeign.listById(Sets.newHashSet(monitor.getMonitorRange())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                for (SysOfficeVO office : officeIdMap.values()) {
                    //唯一情况 车间集合
                    BigDecimal currentPmRate = finishRateMap.get(office.getOfficeCode());
                    //高于上限
                    String template = SysMsgConstant.MsgTypeEnum.SPOT_CHECK_RATE_ALARM.getTemplate();
                    if (Objects.nonNull(currentPmRate) && Objects.nonNull(upValue) && BigDecimalUtil.gt(currentPmRate, upValue)) {
                        String ts1 = String.format(template, LocalDateUtil.format(date, "yyyy-MM-dd"), office.getOfficeName(), BigDecimalUtil.format(currentPmRate), "高", "上", BigDecimalUtil.format(upValue));
                        contentSb.append(ts1).append("\n");
                    } else if (Objects.nonNull(currentPmRate) && Objects.nonNull(lowValue) && BigDecimalUtil.lt(currentPmRate, lowValue)) {
                        String ts3 = String.format(template, LocalDateUtil.format(date, "yyyy-MM-dd"), office.getOfficeName(), BigDecimalUtil.format(currentPmRate), "低", "下", BigDecimalUtil.format(lowValue));
                        contentSb.append(ts3).append("\n");
                    }
                }
                if (contentSb.length() > 0) {
                    Set<Long> notifyMan = monitor.getNotifyMan();
                    SysMsgSendDTO sysMsgDTO = new SysMsgSendDTO();
                    sysMsgDTO.setType(SysMsgConstant.MsgTypeEnum.SPOT_CHECK_RATE_ALARM.getCode());
                    sysMsgDTO.setTitle(SysMsgConstant.MsgTypeEnum.SPOT_CHECK_RATE_ALARM.getTitle());
                    sysMsgDTO.setWarnFlag(SysMsgConstant.MsgTypeEnum.SPOT_CHECK_RATE_ALARM.getWarnFlag());
                    sysMsgDTO.setUserList(notifyMan);
                    sysMsgDTO.setContent(contentSb.substring(0, contentSb.length() - 1));
                    sysMsgDTO.setPushType(WebsocketConstant.WsPushTypeEnum.MSG_SYS_REMIND.getType());
                    SysMsgAdditionDTO sysMsgAdditionDTO = new SysMsgAdditionDTO();
                    sysMsgAdditionDTO.setCode(LocalDateUtil.format(date, DatePattern.NORM_DATE_PATTERN));
                    sysMsgDTO.setAddition(sysMsgAdditionDTO);
                    Boolean aBoolean = FeignOptional.of(() -> remoteMsgFeignApi.sendMsg(sysMsgDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                    log.info("taskCompleteMonitor方法消息发送 -> {}", aBoolean);
                }
            }
        }
    }

    @Override
    public QtLineChartBarDTO itemComplete(LocalDateTime startTime, LocalDateTime endTime, Integer type) {
        //获取数据
        MesInsTaskParamDTO dto = new MesInsTaskParamDTO();
        dto.setStartTime(startTime);
        dto.setEndTime(endTime);
        //所有任务数据
        List<MesInsTaskCountDTO> allTask = mesEqpInsTaskMapper.findInsCount(dto);
        Assert.notEmpty(allTask, "数据为空");
        Map<String, Long> allTaskMap = allTask.stream().filter(v -> v.getOfficeCode() != null)
                .collect(Collectors.toMap(
                        MesInsTaskCountDTO::getOfficeCode,
                        MesInsTaskCountDTO::getNum
                ));
        List<String> officeCodes = allTask.stream().filter(v -> v.getOfficeCode() != null)
                .map(MesInsTaskCountDTO::getOfficeCode).collect(Collectors.toList());
        dto.setStatus(OaConstant.NUM_1);
        //合格数据
        List<MesInsTaskCountDTO> finishedTask = mesEqpInsTaskMapper.findInsCount(dto);
        Map<String, Long> finishedTaskMap = finishedTask.stream().filter(v -> v.getOfficeCode() != null)
                .collect(Collectors.toMap(
                        MesInsTaskCountDTO::getOfficeCode,
                        MesInsTaskCountDTO::getNum
                ));
        //组成图表数据
        List<JsSysOffice> jsSysOffices = jsSysOfficeMapper.selectList(new LambdaQueryWrapper<JsSysOffice>()
                .select(JsSysOffice::getOfficeCode, JsSysOffice::getOfficeName, JsSysOffice::getParentCodes)
                .in(JsSysOffice::getOfficeCode, officeCodes).eq(JsSysOffice::getStatus, QtManagerConstant.NUM_0));
//        Collections.sort(jsSysOffices, Comparator.comparing(JsSysOffice::getOfficeName));

        //车间粒度
        List<JsSysOffice> officeList = Lists.newArrayList();
        if (type.equals(OaConstant.NO)) {
            officeList = jsSysOfficeMapper.selectList(new LambdaQueryWrapper<JsSysOffice>()
                    .select(JsSysOffice::getOfficeCode, JsSysOffice::getOfficeName)
                    .eq(JsSysOffice::getOfficeType, QtManagerConstant.NUM_8)
                    .eq(JsSysOffice::getStatus, QtManagerConstant.NUM_0)
                    .orderByAsc(JsSysOffice::getTreeSort));
        } else {
            //根据车间id获取车间
            Map<Integer, SysOfficeVO> integerSysOfficeVOMap = FeignOptional.of(() -> remoteOfficeFeign.listById(Sets.newHashSet(type)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            SysOfficeVO sysOfficeVO = integerSysOfficeVOMap.get(type);
            officeList = jsSysOfficeMapper.selectList(new LambdaQueryWrapper<JsSysOffice>()
                    .select(JsSysOffice::getOfficeCode, JsSysOffice::getOfficeName)
                    .like(JsSysOffice::getParentCodes, sysOfficeVO.getOfficeCode())
                    .eq(JsSysOffice::getStatus, QtManagerConstant.NUM_0)
                    .orderByAsc(JsSysOffice::getTreeSort));
            Assert.notEmpty(officeList, "该车间下没有机构");
            officeList = officeList.stream().filter(v -> officeCodes.contains(v.getOfficeCode())).collect(Collectors.toList());
        }

        List<StackGraphDTO<String, BigDecimal>> dataList = Lists.newArrayList();
        officeList.forEach(ele -> {
            String officeName = ele.getOfficeName();
            //聚合粒度下所有的数据
            List<JsSysOffice> officeTmp = Lists.newArrayList();
            if (type.equals(OaConstant.NO)) {
                officeTmp = jsSysOffices.stream().filter(v -> v.getParentCodes().contains(ele.getOfficeCode())).collect(Collectors.toList());
            } else {
                officeTmp = jsSysOffices.stream().filter(v -> v.getOfficeCode().equals(ele.getOfficeCode())).collect(Collectors.toList());
            }
            if (CollUtil.isNotEmpty(officeTmp)) {
                //加总产线下所有 1任务总数 2完成数
                Map<Integer, BigDecimal> countMap = Maps.newHashMap();
                officeTmp.forEach(tmp -> {
                    //任务总数
                    BigDecimalUtil.accumulate(countMap, 1, new BigDecimal(allTaskMap.get(tmp.getOfficeCode())));
                    //完成数
                    BigDecimalUtil.accumulate(countMap, 2, new BigDecimal(finishedTaskMap.getOrDefault(tmp.getOfficeCode(), 0L)));
                });
                StackGraphDTO<String, BigDecimal> stackGraphDTO = new StackGraphDTO<>();
                stackGraphDTO.setXAxis(officeName);
                List<TupleDTO<String, BigDecimal>> datList = Lists.newArrayList();
                BigDecimal all = countMap.get(1);
                BigDecimal finished = countMap.get(2);
                datList.add(new TupleDTO<>("点检项总数", all));
                datList.add(new TupleDTO<>("合格数", finished));
                datList.add(new TupleDTO<>("合格率", BigDecimalUtil.percent(finished, all)));
                stackGraphDTO.setData(datList);
                dataList.add(stackGraphDTO);
            }
        });

//        jsSysOffices.forEach(ele->{
//            String officeName = ele.getOfficeName();
//            StackGraphDTO<String, BigDecimal> stackGraphDTO = new StackGraphDTO<>();
//            stackGraphDTO.setXAxis(officeName);
//            List<TupleDTO<String, BigDecimal>> datList = Lists.newArrayList();
//            BigDecimal all = new BigDecimal(allTaskMap.get(ele.getOfficeCode()));
//            BigDecimal finished = new BigDecimal(finishedTaskMap.getOrDefault(ele.getOfficeCode(),0L));
//            datList.add(new TupleDTO<>("点检项总数", all));
//            datList.add(new TupleDTO<>("合格数", finished));
//            datList.add(new TupleDTO<>("合格率", BigDecimalUtil.percent(finished,all,2)));
//            stackGraphDTO.setData(datList);
//            dataList.add(stackGraphDTO);
//        });
        return StackGraphUtil.convertData(dataList, Boolean.TRUE);
    }

    @Override
    public List<MesItemIdNameVO> tmpItemIdAndName() {
        List<MesItemIdNameVO> results = Lists.newArrayList();
        for (MesManagerConstant.TemperatureItemEnum tmp : MesManagerConstant.TemperatureItemEnum.values()) {
            results.add(new MesItemIdNameVO(tmp.getId(), tmp.getItemName(), tmp.getCode()));
        }
        return results;
    }

    @Override
    public SteccnTableDataDTO<Map<String, String>> tempReport(MesInsTaskTmpParamDTO dto) {
        //1、获取温度检验项信息
        dto.setStartTime(dto.getStartDate().atTime(8, 0, 0));
        dto.setEndTime(dto.getEndDate().plusDays(1).atTime(7, 59, 59));
        List<MesInsTaskTmpDTO> tempValue = mesEqpInsTaskMapper.findTmpValue(dto);
        Assert.notEmpty(tempValue, "数据为空");
        //2、生成报表数据
        SteccnTableDataDTO<Map<String, String>> result = new SteccnTableDataDTO();
        Map<String, SysOfficeVO> stringSysOfficeVOMap = FeignOptional.of(() -> remoteOfficeFeign.listByCodeSet(dto.getProductLineCode()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //产线code
        List<SysOfficeVO> officeList = stringSysOfficeVOMap.values().stream().sorted(Comparator.comparing(SysOfficeVO::getTreeSort)).collect(Collectors.toList());
        //温度项
        List<MesItemIdNameVO> tempItems = tmpItemIdAndName();
        tempItems = tempItems.stream().filter(v -> dto.getInsItemId().contains(v.getItemId())).collect(Collectors.toList());
        Map<Long, MesItemIdNameVO> tempItemMap = Maps.newHashMap();
        tempItems.forEach(tmp -> {
            tempItemMap.put(tmp.getItemId(), tmp);
        });
        //2.1、组表头
        List<SteccnTableColumn> columns = Lists.newArrayList();
        columns.add(new SteccnTableColumn("date", "日期", null));
        columns.add(new SteccnTableColumn("classOrder", "班次", null));
        columns.add(new SteccnTableColumn("classGroupName", "班组", null));
        columns.add(new SteccnTableColumn("time", "时间", null));
        //动态表头
        for (SysOfficeVO officeTmp : officeList) {
            //组产线
            SteccnTableColumn columnTmp = new SteccnTableColumn("productLine" + officeTmp.getId(), officeTmp.getOfficeName(), null);
            //组温度项
            List<SteccnTableColumn> columnTmpList = Lists.newArrayList();
            for (MesItemIdNameVO tempItem : tempItems) {
                columnTmpList.add(new SteccnTableColumn(tempItemMap.get(tempItem.getItemId()).getCode() + officeTmp.getId(),
                        tempItemMap.get(tempItem.getItemId()).getItemName(), null));
            }
            columnTmp.setChildren(columnTmpList);
            columns.add(columnTmp);
        }
        result.setColumns(columns);
        //2.2组数据
        List<Map<String, String>> tableList = Lists.newArrayList();
        int dayCount = LocalDateTimeUtil.betweenPeriod(dto.getStartDate(), dto.getEndDate()).getDays();
        for (int i = 0; i <= dayCount; i++) {
            //优先获取基片班组
            LocalDate date = dto.getStartDate().plusDays(i);
            List<OaTodayClassVO> todayClassVOS = FeignOptional.of(() -> oaInfoService.getTodayClassInfo(new TupleDTO<>("基片二车间", date)), FeignConstant.STECCN_OA_SERVICE_NAME).get();
            //早班晚班 j=0是早班 j=1是晚班
            for (int j = 0; j < 2; j++) {
                String classOrderStr;
                if (j == 0) {
                    classOrderStr = "早班";
                } else {
                    classOrderStr = "晚班";
                }
                for (SysOfficeVO officeTmp : officeList) {
                    List<MesInsTaskTmpDTO> taskTmpDTOS = Lists.newArrayList();
                    if (j == 0) {
                        taskTmpDTOS = tempValue.stream().filter(v -> v.getOfficeAuth().equals(officeTmp.getOfficeCode())
                                && LocalDateUtil.isRange(v.getDateTime(), date.atTime(LocalTime.of(8, 0, 0)), date.atTime(LocalTime.of(20, 0, 0)), true, false)).collect(Collectors.toList());
                    } else {
                        taskTmpDTOS = tempValue.stream().filter(v -> v.getOfficeAuth().equals(officeTmp.getOfficeCode())
                                && LocalDateUtil.isRange(v.getDateTime(), date.atTime(LocalTime.of(20, 0, 0)), date.plusDays(1).atTime(LocalTime.of(8, 0, 0)), true, false)).collect(Collectors.toList());
                    }
                    if (CollUtil.isNotEmpty(taskTmpDTOS)) {
                        //根据时间排序
                        taskTmpDTOS.forEach(tmp -> tmp.setDateTime(tmp.getDateTime().withSecond(0)));
                        //按时间分组并排序
                        Map<LocalDateTime, List<MesInsTaskTmpDTO>> localDateTimeListMap = taskTmpDTOS.stream().collect(Collectors.groupingBy(MesInsTaskTmpDTO::getDateTime));
                        TreeMap<LocalDateTime, List<MesInsTaskTmpDTO>> localDateTimeListTreeMap = MapUtil.sort(localDateTimeListMap);
                        for (Map.Entry<LocalDateTime, List<MesInsTaskTmpDTO>> entry : localDateTimeListTreeMap.entrySet()) {
                            Map<String, String> dataTmp = Maps.newHashMap();
                            String time = entry.getKey().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                            String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                            //查找是否有相同日期相同时间的行
                            Boolean isExistColumn = false;
                            if (CollUtil.isNotEmpty(tableList)) {
                                Map<String, String> mapTmp = tableList.stream().filter(map -> map.containsKey("date") && map.get("date").equals(dateStr)
                                        && map.containsKey("time") && map.get("time") != null && map.get("time").equals(time)).findFirst().orElse(null);
                                if (Objects.nonNull(mapTmp)) {
                                    dataTmp = mapTmp;
                                    isExistColumn = true;
                                }
                            }
                            //放入班次班组
                            if (!isExistColumn) {
                                dataTmp.put("date", dateStr);
                                dataTmp.put("classOrder", classOrderStr);
                                if (CollUtil.isNotEmpty(todayClassVOS)) {
                                    OaTodayClassVO oaTodayClassVO = todayClassVOS.stream().filter(v -> v.getClassOrder().equals(classOrderStr)).findFirst().orElse(new OaTodayClassVO());
                                    dataTmp.put("classGroupName", oaTodayClassVO.getClassGroup());
                                }
                                dataTmp.put("time", time);
                            }
                            //温度项数据
                            for (MesItemIdNameVO tempItem : tempItems) {
                                dataTmp.put(tempItemMap.get(tempItem.getItemId()).getCode() + officeTmp.getId(), null);
                            }
                            for (MesInsTaskTmpDTO tmp : entry.getValue()) {
                                String itemValueStr;
                                if (Objects.isNull(tmp.getItemValue())) {
                                    itemValueStr = null;
                                } else {
                                    itemValueStr = tmp.getItemValue().toString();
                                }
                                dataTmp.put(tempItemMap.get(tmp.getInsItemId()).getCode() + officeTmp.getId(), itemValueStr);
                            }
                            if (!isExistColumn) {
                                tableList.add(dataTmp);
                            }
                        }
                    } else {
                        Map<String, String> dataTmp = Maps.newHashMap();
                        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        //查找是否有相同日期相同时间的行
                        Boolean isExistColumn = false;
                        if (CollUtil.isNotEmpty(tableList)) {
                            List<Map<String, String>> mapTmp = tableList.stream().filter(map -> map.containsKey("date") && map.get("date").equals(dateStr)
                            ).collect(Collectors.toList());
                            if (CollUtil.isNotEmpty(mapTmp)) {
                                isExistColumn = true;
                            }
                        }
                        if (!isExistColumn) {
                            //放入班次班组
                            dataTmp.put("date", date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                            dataTmp.put("classOrder", classOrderStr);
                            if (CollUtil.isNotEmpty(todayClassVOS)) {
                                OaTodayClassVO oaTodayClassVO = todayClassVOS.stream().filter(v -> v.getClassOrder().equals(classOrderStr)).findFirst().orElse(new OaTodayClassVO());
                                dataTmp.put("classGroupName", oaTodayClassVO.getClassGroup());
                            }
                            dataTmp.put("time", null);
                            //温度项数据
                            for (MesItemIdNameVO tempItem : tempItems) {
                                dataTmp.put(tempItemMap.get(tempItem.getItemId()).getCode() + officeTmp.getId(), null);
                            }
                            tableList.add(dataTmp);
                        }
                    }

                }
            }
        }
        result.setTableList(tableList);
        return result;
    }

    @Override
    public QtLineChartBarDTO tempLineChart(MesInsTaskTmpParamDTO dto) {
        //1、获取温度检验项信息
        dto.setStartTime(dto.getStartDate().atTime(8, 0, 0));
        dto.setEndTime(dto.getEndDate().plusDays(1).atTime(7, 59, 59));
        List<MesInsTaskTmpDTO> tempValue = mesEqpInsTaskMapper.findTmpValue(dto);
        Assert.notEmpty(tempValue, "数据为空");
        //把时间置为0秒
        tempValue.forEach(tmp -> {
            tmp.setDateTime(tmp.getDateTime().withSecond(0));
        });
        List<StackGraphDTO<String, BigDecimal>> stackGraphDTOList = Lists.newArrayList();
        List<String> xAxisList1 = Lists.newArrayList();
        List<String> xAxisList2 = Lists.newArrayList();
        List<String> xAxisList3 = Lists.newArrayList();
        Map<String, SysOfficeVO> stringSysOfficeVOMap = FeignOptional.of(() -> remoteOfficeFeign.listByCodeSet(dto.getProductLineCode()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //产线code
        List<SysOfficeVO> officeList = stringSysOfficeVOMap.values().stream().sorted(Comparator.comparing(SysOfficeVO::getTreeSort)).collect(Collectors.toList());
        //温度项
        List<MesItemIdNameVO> tempItems = tmpItemIdAndName();
        tempItems = tempItems.stream().filter(v -> dto.getInsItemId().contains(v.getItemId())).collect(Collectors.toList());
        Map<Long, MesItemIdNameVO> tempItemMap = Maps.newHashMap();
        tempItems.forEach(tmp -> {
            tempItemMap.put(tmp.getItemId(), tmp);
        });
        int dayCount = LocalDateTimeUtil.betweenPeriod(dto.getStartDate(), dto.getEndDate()).getDays();
        //获取点检记录的时段
        List<MesEqpInsPlanSchedule> mesEqpInsPlanSchedules = mesEqpInsPlanScheduleMapper.selectList(new LambdaQueryWrapper<MesEqpInsPlanSchedule>()
                .in(MesEqpInsPlanSchedule::getPlanId, Sets.newHashSet(1722156897019785216L)));
        mesEqpInsPlanSchedules = mesEqpInsPlanSchedules.stream()
                .collect(Collectors.toMap(MesEqpInsPlanSchedule::getExecuteTime, Function.identity(), (existing, replacement) -> existing))
                .values()
                .stream()
                .collect(Collectors.toList());
        //根据早班晚班拆分
        List<LocalTime> dayTimes = Lists.newArrayList();
        List<LocalTime> nightTimes = Lists.newArrayList();
        mesEqpInsPlanSchedules.forEach(tmp -> {
            LocalTime timeTmp;
            if (tmp.getExecuteTime().length() == 4) {
                timeTmp = LocalTime.parse(tmp.getExecuteTime(), DateTimeFormatter.ofPattern("H:mm"));
            } else {
                timeTmp = LocalTime.parse(tmp.getExecuteTime(), DateTimeFormatter.ofPattern("HH:mm"));
            }

            if (LocalDateUtil.isRange(LocalDate.now().atTime(timeTmp), LocalDate.now().atTime(8, 0, 0), LocalDate.now().atTime(20, 0, 0), true, false)) {
                dayTimes.add(timeTmp);
            } else {
                nightTimes.add(timeTmp);
            }
        });
        Collections.sort(dayTimes);
        //若时间晚于20点则排最前面
        Collections.sort(nightTimes, Comparator.comparing(time ->
                time.isAfter(LocalTime.parse("20:00")) ? LocalTime.MIN : time
        ));
        for (int i = 0; i <= dayCount; i++) {
            LocalDate date = dto.getStartDate().plusDays(i);
            xAxisList1.add(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            for (int j = 0; j < 2; j++) {
                List<LocalTime> timeList = Lists.newArrayList();
                if (j == 0) {
                    timeList = dayTimes;
                    xAxisList2.add("早班");
                } else {
                    timeList = nightTimes;
                    xAxisList2.add("晚班");
                }
                //组数据
                for (LocalTime tmp : timeList) {
                    xAxisList3.add(tmp.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                    //一个柱子
                    StackGraphDTO<String, BigDecimal> stackGraphDTO = new StackGraphDTO<>();
                    //多个数据
                    List<TupleDTO<String, BigDecimal>> dataList = Lists.newArrayList();
                    for (SysOfficeVO officeTmp : officeList) {
                        for (MesItemIdNameVO tempTmp : tempItems) {
                            //获取该时间点、该产线、该温度项的数据
                            MesInsTaskTmpDTO mesInsTaskTmpDTO = tempValue.stream().filter(v -> v.getOfficeAuth().equals(officeTmp.getOfficeCode())
                                    && v.getInsItemId().equals(tempTmp.getItemId())
                                    && v.getDateTime().equals(date.atTime(tmp))).findFirst().orElse(new MesInsTaskTmpDTO());
                            dataList.add(new TupleDTO<>(officeTmp.getOfficeName() + " " + tempTmp.getItemName(), mesInsTaskTmpDTO.getItemValue()));
                        }
                    }
                    stackGraphDTO.setData(dataList);
                    stackGraphDTOList.add(stackGraphDTO);
                }
            }
        }
        QtLineChartBarDTO qtLineChartBarDTO = StackGraphUtil.convertData(stackGraphDTOList);
        qtLineChartBarDTO.setXAxis(new MesTempLineXDataVO(xAxisList1, xAxisList2, xAxisList3));
        qtLineChartBarDTO.setXData(null);
        return qtLineChartBarDTO;
    }
}




