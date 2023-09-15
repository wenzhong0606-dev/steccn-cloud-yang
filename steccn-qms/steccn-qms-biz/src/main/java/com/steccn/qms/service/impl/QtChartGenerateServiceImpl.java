package com.steccn.qms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.*;
import com.steccn.common.util.*;
import com.steccn.common.vo.*;
import com.steccn.oa.constant.OaConstant;
import com.steccn.oa.dto.OaGroupCountDTO;
import com.steccn.oa.feign.OaInfoService;
import com.steccn.platform.dto.SysOfficeDTO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSysConfigurationFeignApi;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.dto.PmsPullForceEditDTO;
import com.steccn.pms.dto.PmsPullForceQueryDTO;
import com.steccn.pms.dto.ProcessReportWorkHourQmsDTO;
import com.steccn.pms.dto.ProducePassRateDTO;
import com.steccn.pms.feign.RemotePassRateFeignApi;
import com.steccn.pms.feign.RemotePmsFeignApi;
import com.steccn.pms.feign.RemotePullForceFeignApi;
import com.steccn.pms.feign.RemoteReportWorkFeignApi;
import com.steccn.pms.vo.PmsProduceOrderVO;
import com.steccn.pms.vo.PmsPullForceDataVO;
import com.steccn.pms.vo.ProcessReportWorkHourVO;
import com.steccn.pms.vo.ProducePassRateVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.*;
import com.steccn.qms.dto.*;
import com.steccn.qms.service.*;
import com.steccn.qms.util.CollectorsUtil;
import com.steccn.qms.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 生成图片数据
 *
 * @author AndrewLuo
 * @date 2022/11/10
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QtChartGenerateServiceImpl implements QtChartGenerateService {
    private final QtCheckTaskService qtCheckTaskService;

    private final QtCheckTaskDetailService qtCheckTaskDetailService;

    private final QtCheckItemService qtCheckItemService;

    private final QtMaterialService qtMaterialService;

    private final RemotePassRateFeignApi remotePassRateFeignApi;

    private final QtChartTargetValueService qtChartTargetValueService;

    private final RemoteOfficeFeign remoteOfficeFeign;

    private final QtDefectiveItemService qtDefectiveItemService;

    private final QtCheckTaskDestoryInfoService qtCheckTaskDestoryInfoService;

    private final RemotePmsFeignApi remotePmsFeignApi;

    private final QtDefectiveReportService qtDefectiveReportService;

    private final RemotePullForceFeignApi remotePullForceFeignApi;

    private final RemoteDictFeignApi remoteDictFeignApi;

    private final OaInfoService oaInfoService;

    private final RemoteSysConfigurationFeignApi remoteSysConfigurationFeignApi;

    private final RemoteReportWorkFeignApi remoteReportWorkFeignApi;
    /**
     * 生产基片损耗数据
     *
     * @param dto
     * @return
     */
    @Override
    public List<QtChartSubDetailDTO> generateSubstrateLossData(QtChartParamDTO dto) {
        List<QtChartSubDetailDTO> resultList = new ArrayList<>();
        //转时间戳
        LocalDateTime startTime = dto.getDateStart().atTime(0, 0, 1);
        LocalDateTime endTime = dto.getDateEnd().atTime(23, 59, 59);
        //在线检时间戳
        LocalDateTime onlineStartTime = dto.getDateStart().atTime(8, 0, 0);
        LocalDateTime onlineEndTime = dto.getDateEnd().atTime(7, 59, 59);
        onlineEndTime = onlineEndTime.plusDays(1);
        //准备数据
        //1、先根据已知条件查询任务表
        LambdaQueryWrapper<QtCheckTask> taskCondition = new LambdaQueryWrapper<QtCheckTask>()
                //任务完成
                .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())
                //班次时间范围
                .between(QtCheckTask::getClassTime, startTime.toLocalDate(), endTime.toLocalDate())
                //班次
                .eq(dto.getWorkingOrder() != null, QtCheckTask::getClassesId, dto.getWorkingOrder())
                //只要巡检
                .eq(QtCheckTask::getTaskType, QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getCode())
                //只要基片车间
                .like(QtCheckTask::getProductLineName, "基片")
                //车间
                .apply(dto.getProductWorkShopId() != null, "JSON_CONTAINS(product_workshop, JSON_OBJECT('id', {0}))", dto.getProductWorkShopId())
                //产线
                .apply(dto.getProductLineId() != null, "JSON_CONTAINS(product_line, JSON_OBJECT('id', {0}))", dto.getProductLineId())
                .orderByAsc(QtCheckTask::getFinishedTime);
        LambdaQueryWrapper<QtCheckTask> onlineTaskCondition1 = new LambdaQueryWrapper<QtCheckTask>()
                //任务完成
                .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())
                //缺陷记录时段在范围内
                .ge(QtCheckTask::getDefectStartTime, onlineStartTime)
                .le(QtCheckTask::getDefectEndTime, onlineEndTime)
//                .between(QtCheckTask::getDefectStartTime, onlineStartTime, onlineEndTime)
                //只要基片车间
                .like(QtCheckTask::getProductLineName, "基片")
                //班次
//                                    .eq(dto.getWorkingOrder() != null, QtCheckTask::getClassesId, entry2.getKey())
                //只要在线检
                .eq(QtCheckTask::getTaskType, QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode())
                //车间
                .apply(dto.getProductWorkShopId() != null, "JSON_CONTAINS(product_workshop, JSON_OBJECT('id', {0}))", dto.getProductWorkShopId())
                //产线
                .apply(dto.getProductLineId() != null, "JSON_CONTAINS(product_line, JSON_OBJECT('id', {0}))", dto.getProductLineId())
                .orderByAsc(QtCheckTask::getFinishedTime);
        List<QtCheckTask> taskList = qtCheckTaskService.list(taskCondition);
        List<QtCheckTask> onlineTask = qtCheckTaskService.list(onlineTaskCondition1);
        //先拿到开关
        Map<String, List<Integer>> cfMap = FeignOptional.of(() -> remoteSysConfigurationFeignApi.confListInteger(new TripleDTO<>(QtManagerConstant.CONF_QMS_COLLECT_ENABLE, Sets.newHashSet(QtManagerConstant.CONF_QMS_COLLECT_DISABLE_CONTENT), 2)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<Integer> cfContentList = cfMap.get(QtManagerConstant.CONF_QMS_COLLECT_DISABLE_CONTENT);
        //加入在线检的数据
        taskList.addAll(onlineTask);
        if (CollUtil.isNotEmpty(taskList)) {
            Set<Long> taskIdList = Sets.newHashSet();
            for (QtCheckTask tmp : taskList) {
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                //如果是在线检则根据缺陷记录时段算班次日期
                if (tmp.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode())) {
                    QtTaskClassCalculate qtTaskClassCalculate = CalculateDateUtil.calculateClassTime(tmp.getDefectStartTime());
                    tmp.setDate(df.format(qtTaskClassCalculate.getClassDate()));
                } else {
                    tmp.setDate(df.format(tmp.getClassTime()));
                }
                tmp.setProductLineId(tmp.getProductLine().getId());
                taskIdList.add(tmp.getId());
            }
            //一下获取所有详情数据，便于后续计算
            List<QtCheckTaskDetail> allTaskDetails = qtCheckTaskDetailService.list(new LambdaQueryWrapper<QtCheckTaskDetail>()
                    .in(QtCheckTaskDetail::getTaskId,taskIdList));
            List<Integer> allDefectIds = allTaskDetails.stream().filter(tmp->tmp.getDefectId()!=null).map(tmp -> tmp.getDefectId()).collect(Collectors.toList());
            List<QtDefectiveItem> allDefectDetails = Lists.newArrayList();
            if(CollUtil.isNotEmpty(allDefectIds)){
                allDefectDetails = qtDefectiveItemService.list(new LambdaQueryWrapper<QtDefectiveItem>().eq(QtDefectiveItem::getId,allDefectIds));
            }
            //拿到相关检验项数据 把掰边，样品，拉引时速，负载加入
            List<String> bdlTmp = Lists.newArrayList();
            bdlTmp.add("掰边重量");
            bdlTmp.add("样品重量");
            bdlTmp.add("拉引速度");
            bdlTmp.add("负载");
            List<QtCheckItem> bdlItems = qtCheckItemService.list(new LambdaQueryWrapper<QtCheckItem>()
                    .in(QtCheckItem::getName,bdlTmp));
            //查询单片掰边重量(改成了掰边重量)
            List<QtCheckItem> baiItems = bdlItems.stream().filter(v->v.getName().equals("掰边重量"))
                    .collect(Collectors.toList());
            List<Integer> baiItemIds = new ArrayList<>();
            baiItems.forEach(tmp -> {
                baiItemIds.add(tmp.getId());
            });
            //查询单片重量 (生产改成样品重量)
            List<QtCheckItem> danItems = bdlItems.stream().filter(v->v.getName().equals("样品重量"))
                    .collect(Collectors.toList());
            List<Integer> danItemIds = new ArrayList<>();
            danItems.forEach(tmp -> {
                danItemIds.add(tmp.getId());
            });
            //查询拉引时速(改成了拉引速度)
            List<QtCheckItem> laItems = bdlItems.stream().filter(v->v.getName().equals("拉引速度"))
                    .collect(Collectors.toList());
            List<Integer> laItemIds = new ArrayList<>();
            laItems.forEach(tmp -> {
                laItemIds.add(tmp.getId());
            });
            //查询负载
            List<QtCheckItem> fuItems = bdlItems.stream().filter(v->v.getName().equals("负载"))
                    .collect(Collectors.toList());
            List<Integer> fuItemIds = new ArrayList<>();
            fuItems.forEach(tmp -> {
                fuItemIds.add(tmp.getId());
            });
            //获取停工记录
            ProcessReportWorkHourQmsDTO workHourDTO = new ProcessReportWorkHourQmsDTO();
            workHourDTO.setStartDate(startTime.toLocalDate());
            workHourDTO.setEndDate(endTime.toLocalDate());
            workHourDTO.setProductLineIds(taskList.stream().map(QtCheckTask::getProductLineId).collect(Collectors.toList()));
            List<ProcessReportWorkHourVO> workHourVOList = FeignOptional.of(() -> remoteReportWorkFeignApi.getWorkHour(workHourDTO), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
            //根据班次日期进行分组
            Map<String, List<QtCheckTask>> groupByDate = taskList.stream().collect(Collectors.groupingBy(QtCheckTask::getDate));
            //遍历日期map
            for (Map.Entry<String, List<QtCheckTask>> entry : groupByDate.entrySet()) {
                List<QtCheckTask> checkTaskDateList = entry.getValue();
                //再根据班次进行分组
                Map<Integer, List<QtCheckTask>> groupByWorkOrder = checkTaskDateList.stream().collect(Collectors.groupingBy(QtCheckTask::getClassesId));
                //遍历班次分组
                //只统计一次(测点数据用)
                int point = 1;
                for (Map.Entry<Integer, List<QtCheckTask>> entry2 : groupByWorkOrder.entrySet()) {
                    //根据时间排序
                    List<QtCheckTask> checkTaskWorkOrderList = entry2.getValue().stream().sorted(Comparator.comparing(QtCheckTask::getFinishedTime)).collect(Collectors.toList());
                    //过滤巡检
                    List<QtCheckTask> checkTaskPatrolWorkOrderList = entry2.getValue().stream()
                            .filter(v->v.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getCode()))
                            .sorted(Comparator.comparing(QtCheckTask::getFinishedTime)).collect(Collectors.toList());
                    //查看是否是一种物料
                    HashSet<Long> materialIdSet = new HashSet<>();
                    //物料id集合
                    List<Long> materialList = Lists.newArrayList();
                    checkTaskPatrolWorkOrderList.forEach(tmp -> {
                        materialIdSet.add(tmp.getMaterialId());
                        if (!materialList.contains(tmp.getMaterialId())) {
                            materialList.add(tmp.getMaterialId());
                        }
                    });
                    Boolean flag = true;
                    if (materialIdSet.size() > 1) {
                        flag = false;
                    }
                    //计算生产时长
                    Map<Long, BigDecimal> materialProduceTime = calculateProduceTime(materialList,flag,entry2);
                    //一种物料
                    //根据产线进行分组
                    Map<Integer, List<QtCheckTask>> groupByProductLine = checkTaskWorkOrderList.stream().collect(Collectors.groupingBy(QtCheckTask::getProductLineId));
                    //遍历产线
                    //只统计一次(测点数据用)
//                    int point = 1;
                    for (Map.Entry<Integer, List<QtCheckTask>> entry3 : groupByProductLine.entrySet()) {
                        //产线名字
                        String productLineName = remoteOfficeFeign.getNameById(entry3.getKey()).getData();
                        //产线id
                        Integer productLineId = entry3.getKey();
                        //车间名字
                        String productWorkShopName = "";
                        Integer productWorkShopId = -1;
                        R<SysOfficeVO> parent = remoteOfficeFeign.parent(entry3.getKey());
                        if (parent.getData() != null) {
                            productWorkShopName = parent.getData().getOfficeName();
                            productWorkShopId = parent.getData().getId();
                        }
                        //拉引量
                        BigDecimal laQuantity = BigDecimal.ZERO;
                        //【情况1】班次b的12个小时拉引速度没有变化：拉引量 = 负载平均值f * 12
                        //负载平均值f：查询满足下列条件的巡检任务，记为集合T，集合T内所有任务录入的“负载”取平均值即为f
                        //状态为已检验
                        //产线为A
                        //工位为“基片下片工位（南）”
                        //抽检时间属于班次日期D班次b
                        List<QtCheckTask> productLineTask = entry3.getValue();
                        if(CollUtil.isNotEmpty(productLineTask)){
                            //产线下所有任务id
                            List<Long> taskIds = productLineTask.stream().map(QtCheckTask::getId).collect(Collectors.toList());
                            //拉引速度没有变化
                            List<QtCheckTaskDetail> laItemDetails = Lists.newArrayList();
                            Boolean laChangeFlag = true;
                            if (taskIds.size() > 0 && fuItems.size() > 0) {
                                laItemDetails = allTaskDetails.stream().filter(v->taskIds.contains(v.getTaskId())
                                                && laItemIds.contains(v.getItemId()))
                                        .collect(Collectors.toList());
                                //比较所有拉引速度是否有变化
                                if(CollUtil.isNotEmpty(laItemDetails)){
                                    laChangeFlag = laItemDetails.stream().filter(v -> StrUtil.isNotBlank(v.getCheckResult()))
                                            .map(QtCheckTaskDetail::getCheckResult)
                                            .distinct()
                                            .count() == 1;
                                }
                            }
                            //情况一
                            laChangeFlag = true;
                            if(laChangeFlag){
                                //【情况1】班次b的12个小时拉引速度没有变化：拉引量 = 负载平均值f * 12
                                //负载平均值f：查询满足下列条件的巡检任务，记为集合T，集合T内所有任务录入的“负载”取平均值即为f
                                //状态为已检验
                                //产线为A
                                //工位为“基片下片工位（南）”
                                //负载任务id
                                List<Long> fuTaskIds = productLineTask.stream().filter(v -> v.getStation() != null
                                                && v.getStation().getName() != null
                                                && v.getStation().getName().equals(QtManagerConstant.JIPIAN_DOWN_STATION_SOUTH))
                                        .map(QtCheckTask::getId)
                                        .collect(Collectors.toList());
                                //拿到所有负载的值取平均数
                                //获取所有负载
                                List<QtCheckTaskDetail> fuDetails = Lists.newArrayList();
                                if (fuTaskIds.size() > 0 && fuItemIds.size() > 0) {
                                    fuDetails = allTaskDetails.stream().filter(v->fuTaskIds.contains(v.getTaskId())
                                                    && fuItemIds.contains(v.getItemId()))
                                            .collect(Collectors.toList());
                                }
                                if (CollUtil.isNotEmpty(fuDetails)) {
                                    List<QtCheckTaskDetail> fuDetailsTmp = Lists.newArrayList();
                                    //对一个任务下的负载进行分组
                                    Map<Long,List<QtCheckTaskDetail>> fuDetailsMap = fuDetails.stream()
                                            .collect(Collectors.groupingBy(QtCheckTaskDetail::getTaskId));
                                    //算同一个任务下的负载平均值
                                    for(Map.Entry<Long,List<QtCheckTaskDetail>> fuDetailEntry:fuDetailsMap.entrySet()){
                                        List<QtCheckTaskDetail> value = fuDetailEntry.getValue();
                                        BigDecimal totalFuItem = BigDecimal.ZERO;
                                        BigDecimal totalFuCount = BigDecimal.ZERO;
                                        for (QtCheckTaskDetail tmp : value) {
                                            if (StrUtil.isNotBlank(tmp.getCheckResult())) {
                                                BigDecimal decimalTmp = new BigDecimal(tmp.getCheckResult());
                                                totalFuItem = BigDecimalUtil.add(totalFuItem, decimalTmp);
                                                totalFuCount = BigDecimalUtil.add(totalFuCount, new BigDecimal(1));
                                            }
                                        }
                                        QtCheckTaskDetail fu = new QtCheckTaskDetail();
                                        BigDecimal fuDetailRes =BigDecimalUtil.divide(totalFuItem, totalFuCount,4);
                                        if(fuDetailRes.compareTo(BigDecimal.ZERO)!=0){
                                            fu.setCheckResult(fuDetailRes.toString());
                                        }
                                        fuDetailsTmp.add(fu);
                                    }
                                    //算所有的负载平均值
                                    BigDecimal totalFuItem = BigDecimal.ZERO;
                                    BigDecimal totalFuCount = BigDecimal.ZERO;
                                    for (QtCheckTaskDetail tmp : fuDetailsTmp) {
                                        if (StrUtil.isNotBlank(tmp.getCheckResult())) {
                                            BigDecimal decimalTmp = new BigDecimal(tmp.getCheckResult());
                                            totalFuItem = BigDecimalUtil.add(totalFuItem, decimalTmp);
                                            totalFuCount = BigDecimalUtil.add(totalFuCount, new BigDecimal(1));
                                        }
                                    }

                                    //拉引量 = 负载平均值f * 12
                                    BigDecimal avgFuItem = new BigDecimal(0);
                                    if (totalFuItem.intValue() != 0) {
                                        avgFuItem = BigDecimalUtil.divide(totalFuItem, totalFuCount,4);
                                    }
                                    laQuantity = BigDecimalUtil.multiply(avgFuItem,new BigDecimal(12));
                                    //[特殊情况】若基片产线A在班次日期D班次b报工的停机时长>0，情况1、情况2算出的拉引量均需减去停机时长对应的拉引量，才能得到实际的拉引量
                                    //例如情况1算出拉引量为70吨，但9:00~10:00停机，停机期间理论负载为6吨/小时，则实际拉引量为70 - 1*6 = 64吨
                                    //        实际拉引量 = 情况1/情况2算出的拉引量 - 停机时长t1 * 负载f1 - 停机时长t2 * 负载f2 - 停机时长t3 * 负载f3
                                    if(CollUtil.isNotEmpty(workHourVOList)){
                                        //停机时长t1：
                                        BigDecimal t1 = BigDecimal.ZERO;
                                        if(entry2.getKey().equals(OaConstant.NO)){
                                            //早班：产线A报工的所有停机时间段与 [班次日期D 8:00, 班次日期D 12:00] 的重叠部分，单位为h
                                            List<ProcessReportWorkHourVO> reportWorkHourVOS = workHourVOList.stream().filter(v -> v.getClassesId().equals(1587279237069422592L)
                                                    && v.getClassDate().equals(productLineTask.get(0).getClassTime())
                                                    && v.getProductLineId().equals(productLineTask.get(0).getProductLineId())).collect(Collectors.toList());
                                            if(CollUtil.isNotEmpty(reportWorkHourVOS)){
                                                for(ProcessReportWorkHourVO hourTmp:reportWorkHourVOS){
                                                    BigDecimal t1Tmp = LocalDateUtil.calculateIntersectionHours(
                                                            LocalDateTime.of(productLineTask.get(0).getClassTime(), LocalTime.of(8, 0, 0)),
                                                            LocalDateTime.of(productLineTask.get(0).getClassTime(), LocalTime.of(12, 0, 0)),
                                                            hourTmp.getStartTime(), hourTmp.getEndTime());
                                                    t1 = BigDecimalUtil.add(t1,t1Tmp,3);
                                                }
                                            }

                                        }else {
                                            //晚班：产线A报工的所有停机时间段与 [班次日期D 20:00, 班次日期D+1 0:00] 的重叠部分，单位为h
                                            List<ProcessReportWorkHourVO> reportWorkHourVOS = workHourVOList.stream().filter(v -> v.getClassesId().equals(1587279280585326592L)
                                                    && v.getClassDate().equals(productLineTask.get(0).getClassTime())
                                                    && v.getProductLineId().equals(productLineTask.get(0).getProductLineId())).collect(Collectors.toList());
                                            if(CollUtil.isNotEmpty(reportWorkHourVOS)){
                                                for(ProcessReportWorkHourVO hourTmp:reportWorkHourVOS){
                                                    BigDecimal t1Tmp = LocalDateUtil.calculateIntersectionHours(
                                                            LocalDateTime.of(productLineTask.get(0).getClassTime(), LocalTime.of(20, 0, 0)),
                                                            LocalDateTime.of(productLineTask.get(0).getClassTime().plusDays(1), LocalTime.of(0, 0, 0)),
                                                            hourTmp.getStartTime(), hourTmp.getEndTime());
                                                    t1 = BigDecimalUtil.add(t1,t1Tmp,3);
                                                }
                                            }
                                        }
                                        //停机时长t2：
                                        BigDecimal t2 = BigDecimal.ZERO;
                                        if(entry2.getKey().equals(OaConstant.NO)){
                                            //早班：产线A报工的所有停机时间段与 [班次日期D 12:00, 班次日期D 16:00] 的重叠部分，单位为h
                                            List<ProcessReportWorkHourVO> reportWorkHourVOS = workHourVOList.stream().filter(v -> v.getClassesId().equals(1587279237069422592L)
                                                    && v.getClassDate().equals(productLineTask.get(0).getClassTime())
                                                    && v.getProductLineId().equals(productLineTask.get(0).getProductLineId())).collect(Collectors.toList());
                                            if(CollUtil.isNotEmpty(reportWorkHourVOS)){
                                                for(ProcessReportWorkHourVO hourTmp:reportWorkHourVOS){
                                                    BigDecimal t2Tmp = LocalDateUtil.calculateIntersectionHours(
                                                            LocalDateTime.of(productLineTask.get(0).getClassTime(), LocalTime.of(12, 0, 0)),
                                                            LocalDateTime.of(productLineTask.get(0).getClassTime(), LocalTime.of(16, 0, 0)),
                                                            hourTmp.getStartTime(), hourTmp.getEndTime());
                                                    t2 = BigDecimalUtil.add(t2,t2Tmp,3);
                                                }
                                            }

                                        }else {
                                            //晚班：产线A报工的所有停机时间段与 [班次日期D+1 0:00, 班次日期D+1 4:00] 的重叠部分，单位为h
                                            List<ProcessReportWorkHourVO> reportWorkHourVOS = workHourVOList.stream().filter(v -> v.getClassesId().equals(1587279280585326592L)
                                                    && v.getClassDate().equals(productLineTask.get(0).getClassTime())
                                                    && v.getProductLineId().equals(productLineTask.get(0).getProductLineId())).collect(Collectors.toList());
                                            if(CollUtil.isNotEmpty(reportWorkHourVOS)){
                                                for(ProcessReportWorkHourVO hourTmp:reportWorkHourVOS){
                                                    BigDecimal t2Tmp = LocalDateUtil.calculateIntersectionHours(
                                                            LocalDateTime.of(productLineTask.get(0).getClassTime().plusDays(1), LocalTime.of(0, 0, 0)),
                                                            LocalDateTime.of(productLineTask.get(0).getClassTime().plusDays(1), LocalTime.of(4, 0, 0)),
                                                            hourTmp.getStartTime(), hourTmp.getEndTime());
                                                    t2 = BigDecimalUtil.add(t2,t2Tmp,3);
                                                }
                                            }
                                        }
                                    }
                                    //停机时长t3：
                                    BigDecimal t3 = BigDecimal.ZERO;
                                    if(entry2.getKey().equals(OaConstant.NO)){
                                        //早班：产线A报工的所有停机时间段与 [班次日期D 16:00, 班次日期D 20:00] 的重叠部分，单位为h
                                        List<ProcessReportWorkHourVO> reportWorkHourVOS = workHourVOList.stream().filter(v -> v.getClassesId().equals(1587279237069422592L)
                                                && v.getClassDate().equals(productLineTask.get(0).getClassTime())
                                                && v.getProductLineId().equals(productLineTask.get(0).getProductLineId())).collect(Collectors.toList());
                                        if(CollUtil.isNotEmpty(reportWorkHourVOS)){
                                            for(ProcessReportWorkHourVO hourTmp:reportWorkHourVOS){
                                                BigDecimal t3Tmp = LocalDateUtil.calculateIntersectionHours(
                                                        LocalDateTime.of(productLineTask.get(0).getClassTime(), LocalTime.of(16, 0, 0)),
                                                        LocalDateTime.of(productLineTask.get(0).getClassTime(), LocalTime.of(20, 0, 0)),
                                                        hourTmp.getStartTime(), hourTmp.getEndTime());
                                                t3 = BigDecimalUtil.add(t3,t3Tmp,3);
                                            }
                                        }

                                    }else {
                                        //晚班：产线A报工的所有停机时间段与 [班次日期D+1 4:00, 班次日期D+1 8:00] 的重叠部分，单位为h
                                        List<ProcessReportWorkHourVO> reportWorkHourVOS = workHourVOList.stream().filter(v -> v.getClassesId().equals(1587279280585326592L)
                                                && v.getClassDate().equals(productLineTask.get(0).getClassTime())
                                                && v.getProductLineId().equals(productLineTask.get(0).getProductLineId())).collect(Collectors.toList());
                                        if(CollUtil.isNotEmpty(reportWorkHourVOS)){
                                            for(ProcessReportWorkHourVO hourTmp:reportWorkHourVOS){
                                                BigDecimal t3Tmp = LocalDateUtil.calculateIntersectionHours(
                                                        LocalDateTime.of(productLineTask.get(0).getClassTime().plusDays(1), LocalTime.of(4, 0, 0)),
                                                        LocalDateTime.of(productLineTask.get(0).getClassTime().plusDays(1), LocalTime.of(8, 0, 0)),
                                                        hourTmp.getStartTime(), hourTmp.getEndTime());
                                                t3 = BigDecimalUtil.add(t3,t3Tmp,3);
                                            }
                                        }
                                    }
                                    //负载f1：查询满足下列条件的巡检任务，记为集合T1，集合T1内所有任务录入的“负载”取平均值即为f1（每个任务录入的“负载”为该任务所有样本取平均值）
                                    //状态为已检验
                                    //        产线为A
                                    //工位为“基片下片工位（南）”
                                    //抽检时间：
                                    //早班：在区间 [班次日期D 8:00, 班次日期D 12:00) 内
                                    //晚班：在区间 [班次日期D 20:00, 班次日期D+1 0:00) 内
                                    if(entry2.getKey().equals(OaConstant.NO)){
//                                        List<Long> fuTaskIds = productLineTask.stream().filter(v -> v.getStation() != null
//                                                        && v.getStation().getName() != null
//                                                        && v.getStation().getName().equals(QtManagerConstant.JIPIAN_DOWN_STATION_SOUTH))
//                                                .map(QtCheckTask::getId)
//                                                .collect(Collectors.toList());
                                    }
                                    //负载f2：查询满足下列条件的巡检任务，记为集合T2，集合T2内所有任务录入的“负载”取平均值即为f2（每个任务录入的“负载”为该任务所有样本取平均值）
                                    //状态为已检验
                                    //        产线为A
                                    //工位为“基片下片工位（南）”
                                    //抽检时间：
                                    //早班：在区间 [班次日期D 12:00, 班次日期D 16:00) 内
                                    //晚班：在区间 [班次日期D+1 0:00, 班次日期D+1 4:00) 内
                                    //负载f3：查询满足下列条件的巡检任务，记为集合T3，集合T3内所有任务录入的“负载”取平均值即为f3（每个任务录入的“负载”为该任务所有样本取平均值）
                                    //状态为已检验
                                    //       产线为A
                                    //工位为“基片下片工位（南）”
                                    //抽检时间：
                                    //早班：在区间 [班次日期D 16:00, 班次日期D 20:00) 内
                                    //晚班：在区间 [班次日期D+1 4:00, 班次日期D+1 8:00) 内
                                    //若按上述逻辑算出的f1、f2、f3均为空值，则三者均置为0
                                    //若按上述逻辑算出的f1、f2、f3中有2个为空值，1个为非空值ff，则三者均置为ff
                                    //若按上述逻辑算出的f1、f2、f3中有1个为空值
                                    //f1为空值，则f1置为f2的值
                                    //f2为空值，则f2置为f3的值
                                    //f3为空值，则f3置为f2的值
                                }
                            }else {
                                // TODO: 2023/6/27
                            }
                        }
                        //掰边损耗重量
                        BigDecimal baiLossWeight = BigDecimal.ZERO;
                        //产线下所有物料的缺陷
                        Map<String, BigDecimal> defectLossWeightMap = MapUtil.newHashMap();
                        //产线下所有落板的缺陷
                        Map<String, BigDecimal> luoLossWeightMap = MapUtil.newHashMap();
                        //根据物料进行分组
                        Map<Long, List<QtCheckTask>> groupByMaterial = productLineTask.stream().collect(Collectors.groupingBy(QtCheckTask::getMaterialId));
                        //只统计一次(测点数据用)
//                        int point = 1;
                        //遍历物料分组
                        for (Map.Entry<Long, List<QtCheckTask>> materialEntry : groupByMaterial.entrySet()) {
                            BigDecimal totalBaiItem = new BigDecimal(0);
                            BigDecimal totalBaiItemCount = new BigDecimal(0);
                            BigDecimal totalDanItem = new BigDecimal(0);
                            BigDecimal totalDanItemCount = new BigDecimal(0);
                            BigDecimal totalLaItem = new BigDecimal(0);
                            BigDecimal totalLaItemCount = new BigDecimal(0);
                            //准备产线下taskId
                            List<QtCheckTask> checkTaskProductLineList = materialEntry.getValue();
                            List<Long> taskIds = new ArrayList<>();
                            checkTaskProductLineList.forEach(tmp -> {
                                taskIds.add(tmp.getId());
                            });
                            //获取所有单片掰边重量
                            List<QtCheckTaskDetail> baiItemDetails = Lists.newArrayList();
                            if (taskIds.size() > 0 && baiItemIds.size() > 0) {
//                                baiItemDetails = qtCheckTaskDetailService.list(new LambdaQueryWrapper<QtCheckTaskDetail>()
//                                        .in(CollUtil.isNotEmpty(taskIds), QtCheckTaskDetail::getTaskId, taskIds)
//                                        .in(CollUtil.isNotEmpty(baiItemIds), QtCheckTaskDetail::getItemId, baiItemIds));
                                baiItemDetails = allTaskDetails.stream().filter(v->taskIds.contains(v.getTaskId())
                                        && baiItemIds.contains(v.getItemId()))
                                        .collect(Collectors.toList());
                            }
                            if (CollUtil.isNotEmpty(baiItemDetails)) {
                                for (QtCheckTaskDetail tmp : baiItemDetails) {
                                    if (StrUtil.isNotBlank(tmp.getCheckResult())) {
                                        BigDecimal decimalTmp = new BigDecimal(tmp.getCheckResult());
                                        totalBaiItem = BigDecimalUtil.add(totalBaiItem, decimalTmp);
                                        totalBaiItemCount = BigDecimalUtil.add(totalBaiItemCount, new BigDecimal(1));
                                    }
                                }
                            }
                            //获取所有单片重量
                            List<QtCheckTaskDetail> danItemDetails = Lists.newArrayList();
                            if (taskIds.size() > 0 && danItemIds.size() > 0) {
//                                danItemDetails = qtCheckTaskDetailService.list(new LambdaQueryWrapper<QtCheckTaskDetail>()
//                                        .in(CollUtil.isNotEmpty(taskIds), QtCheckTaskDetail::getTaskId, taskIds)
//                                        .in(CollUtil.isNotEmpty(danItemIds), QtCheckTaskDetail::getItemId, danItemIds));
                                danItemDetails = allTaskDetails.stream().filter(v->taskIds.contains(v.getTaskId())
                                                && danItemIds.contains(v.getItemId()))
                                        .collect(Collectors.toList());
                            }
                            if (CollUtil.isNotEmpty(danItemDetails)) {
                                for (QtCheckTaskDetail tmp : danItemDetails) {
                                    if (StrUtil.isNotBlank(tmp.getCheckResult())) {
                                        //判断是否为数字
                                        if(RegularUtil.isNum(tmp.getCheckResult())){
                                            BigDecimal decimalTmp = new BigDecimal(tmp.getCheckResult());
                                            totalDanItem = BigDecimalUtil.add(totalDanItem, decimalTmp);
                                            totalDanItemCount = BigDecimalUtil.add(totalDanItemCount, new BigDecimal(1));
                                        }
                                    }
                                }
                            }
                            //获取所有拉引时速
                            List<QtCheckTaskDetail> laItemDetails = Lists.newArrayList();
                            if (taskIds.size() > 0 && laItemIds.size() > 0) {
//                                laItemDetails = qtCheckTaskDetailService.list(new LambdaQueryWrapper<QtCheckTaskDetail>()
//                                        .in(CollUtil.isNotEmpty(taskIds), QtCheckTaskDetail::getTaskId, taskIds)
//                                        .in(CollUtil.isNotEmpty(laItemIds), QtCheckTaskDetail::getItemId, laItemIds));
                                laItemDetails = allTaskDetails.stream().filter(v->taskIds.contains(v.getTaskId())
                                                && laItemIds.contains(v.getItemId()))
                                        .collect(Collectors.toList());
                            }
                            if (CollUtil.isNotEmpty(laItemDetails)) {
                                for (QtCheckTaskDetail tmp : laItemDetails) {
                                    if (StrUtil.isNotBlank(tmp.getCheckResult())) {
                                        BigDecimal decimalTmp = new BigDecimal(tmp.getCheckResult());
                                        totalLaItem = BigDecimalUtil.add(totalLaItem, decimalTmp);
                                        totalLaItemCount = BigDecimalUtil.add(totalLaItemCount, new BigDecimal(1));
                                    }
                                }
                            }
                            //平均单片掰边重量：产线A在班次日期D的班次b时间段内（例如2021-09-21 8:00:00 ~ 20:00:00）所有巡检任务录入的“单片掰边重量”（需提前配置好“单片掰边重量”检验项）取平均值，单位为kg
                            BigDecimal avgBaiItem = new BigDecimal(0);
                            if (totalBaiItem.intValue() != 0) {
                                avgBaiItem = BigDecimalUtil.divide(totalBaiItem, totalBaiItemCount,4);
                            }
                            //平均单片重量：产线A在班次日期D的班次b时间段内（例如2021-09-21 8:00:00 ~ 20:00:00）所有巡检任务录入的“单片重量”（需提前配置好“单片重量”检验项）取平均值，单位为kg
                            BigDecimal avgDanItem = new BigDecimal(0);
                            if (totalDanItem.intValue() != 0) {
                                avgDanItem = BigDecimalUtil.divide(totalDanItem, totalDanItemCount,4);
                            }
                            //拉引时速：产线A在班次日期D的班次b时间段内（例如2021-09-21 8:00:00 ~ 20:00:00）所有巡检任务录入的“拉引速度”（需提前配置好“拉引速度”检验项）取平均值，单位为m/h
                            BigDecimal avgLaItem = new BigDecimal(0);
                            if (totalDanItem.intValue() != 0) {
                                avgLaItem = BigDecimalUtil.divide(totalLaItem, totalLaItemCount,4);
                            }
                            //玻璃片长度：巡检任务中物料的“玻璃片长度”属性（取自物料主数据），单位为mm
                            BigDecimal glassLength = new BigDecimal(0);
                            QtMaterial material = new QtMaterial();
                            if(CollUtil.isNotEmpty(checkTaskPatrolWorkOrderList)){
                                material = qtMaterialService.getById(checkTaskPatrolWorkOrderList.get(0).getMaterialId());
                            }
                            if (material.getLengthQuantity() != null) {
                                glassLength = BigDecimalUtil.add(glassLength, material.getLengthQuantity());
                            }


                            //掰边损耗重量 = 平均单片掰边重量 * （拉引时速 / 玻璃片长度) * 12 / 1000000
                            //拉引时速/玻璃片长度
                            BigDecimal divideLaGlass = new BigDecimal(0);
                            if (glassLength.compareTo(BigDecimal.ZERO) > 0) {
                                divideLaGlass = BigDecimalUtil.divide(avgLaItem, glassLength,4);
                            }
                            //掰边损耗重量
                            //掰边损耗重量 = 平均单片掰边重量 * （拉引时速 / 玻璃片长度) * 12 / 1000000
                            //平均单片掰边重量 * （拉引时速 / 玻璃片长度)
                            BigDecimal laDivideGlass = BigDecimalUtil.multiply(avgBaiItem, divideLaGlass);
                            if (flag) {
                                BigDecimal laMultiplyGlass12 = BigDecimalUtil.multiply(laDivideGlass, new BigDecimal(12));
                                //产品改需求，不变
                                BigDecimal oneBaiLossWeight = BigDecimalUtil.multiply(laMultiplyGlass12, new BigDecimal(1));
                                baiLossWeight = BigDecimalUtil.add(baiLossWeight, oneBaiLossWeight);
                            } else {
                                BigDecimal produceTime = new BigDecimal(6);
                                if (materialProduceTime.get(materialEntry.getKey()) != null) {
                                    produceTime = materialProduceTime.get(materialEntry.getKey());
                                }
                                BigDecimal laMultiplyGlassTime = BigDecimalUtil.multiply(laDivideGlass, produceTime);
//                                BigDecimal oneBaiLossWeight = BigDecimalUtil.divide(laMultiplyGlassTime, new BigDecimal(1000000),4);
                                //产品改需求，不变
                                BigDecimal oneBaiLossWeight = BigDecimalUtil.multiply(laMultiplyGlassTime, new BigDecimal(1));
                                baiLossWeight = BigDecimalUtil.add(baiLossWeight, oneBaiLossWeight);
                            }

                            //拉引量 = (平均单片掰边重量 + 平均单片重量) * （拉引时速 / 玻璃片长度) * 12 / 1000000
                            //平均单片掰边重量 + 平均单片重量
//                            BigDecimal baiAddDan = BigDecimalUtil.add(avgBaiItem, avgDanItem);
//                            BigDecimal multi = BigDecimalUtil.multiply(baiAddDan, divideLaGlass);
//                            BigDecimal multi12 = BigDecimalUtil.multiply(multi, new BigDecimal(12));

                            //拉引量
//                            if (flag) {
//                                //产品改需求，不变
//                                BigDecimal laQuantityOne = BigDecimalUtil.multiply(multi12, new BigDecimal(1));
//                                laQuantity = BigDecimalUtil.add(laQuantityOne, laQuantity);
//                            } else {
//                                BigDecimal produceTime = new BigDecimal(6);
//                                if (materialProduceTime.get(materialEntry.getKey()) != null) {
//                                    produceTime = materialProduceTime.get(materialEntry.getKey());
//                                }
//                                BigDecimal multiProduceTime = BigDecimalUtil.multiply(multi, produceTime);
////                                BigDecimal laQuantityOne = BigDecimalUtil.divide(multiProduceTime, new BigDecimal(1000000),4);
//                                //产品改需求，乘以2即可
//                                BigDecimal laQuantityOne = BigDecimalUtil.multiply(multiProduceTime, new BigDecimal(1));
//                                laQuantity = BigDecimalUtil.add(laQuantityOne, laQuantity);
//                            }

                            //缺陷项q导致损耗重量 = 缺陷项q的数量 * 平均单片重量 / 1000
                            //缺陷项q的数量：缺陷记录时段在班次日期D班次b的时段内、产线A的所有已完成在线检任务记录的缺陷项q数量加总 + 检验完成时间在班次日期D班次b的时段内产线A的所有巡检任务中人工选择“次”后关联缺陷项q的数量加总 + 在班次日期D班次b的时段内产线A的检验设备上报的缺陷项q总数
                            BigDecimal defectAmount = BigDecimal.ZERO;
                            //班次时间范围(早班)
                            String morningStartStr = entry.getKey() + " 08:00:00";
                            String morningEndStr = entry.getKey() + " 20:00:00";
                            String eveningStartStr = entry.getKey() + " 20:00:00";
                            String eveningEndStr = entry.getKey() + " 08:00:00";
                            LocalDateTime startTimeDate = LocalDateTime.now();
                            LocalDateTime endTimeDate = LocalDateTime.now();
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                            //早班则是当天
                            if (entry2.getKey().equals(0)) {
                                startTimeDate = LocalDateTime.parse(morningStartStr, formatter);
                                endTimeDate = LocalDateTime.parse(morningEndStr, formatter);
                            } else {
                                startTimeDate = LocalDateTime.parse(eveningStartStr, formatter);
                                endTimeDate = LocalDateTime.parse(eveningEndStr, formatter);
                                endTimeDate = endTimeDate.plusDays(1);
                            }
                            LocalDateTime finalStartTimeDate = startTimeDate;
                            LocalDateTime finalEndTimeDate = endTimeDate;
//                            LambdaQueryWrapper<QtCheckTask> onlineTaskCondition = new LambdaQueryWrapper<QtCheckTask>()
//                                    //任务完成
//                                    .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())
//                                    //完成时间范围
//                                    .ge(QtCheckTask::getDefectStartTime, startTimeDate).lt(QtCheckTask::getDefectStartTime, endTimeDate)
//                                    //只要基片车间
//                                    .like(QtCheckTask::getProductLineName, "基片")
//                                    //班次
////                                    .eq(dto.getWorkingOrder() != null, QtCheckTask::getClassesId, entry2.getKey())
//                                    //只要在线检
//                                    .eq(QtCheckTask::getTaskType, QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode())
//                                    //该物料
//                                    .eq(QtCheckTask::getMaterialId, materialEntry.getKey())
//                                    //该产线
//                                    .apply(dto.getProductLineId() != null, "JSON_CONTAINS(product_line, JSON_OBJECT('id', {0}))", entry3.getKey())
//                                    .orderByAsc(QtCheckTask::getFinishedTime);
                            //缺陷项q导致损耗重量 = 缺陷项q的数量 * 平均单片重量 / 1000
                            //缺陷项q的数量：缺陷记录时段在班次日期D班次b的时段内、产线A的所有已完成在线检任务记录的缺陷项q数量加总 + 检验完成时间在班次日期D班次b的时段内产线A的所有巡检任务中人工选择“次”后关联缺陷项q的数量加总 + 在班次日期D班次b的时段内产线A的检验设备上报的缺陷项q总数
                            List<QtCheckTask> onlineTaskList = onlineTask.stream().filter(v-> (v.getDefectStartTime()!=null && LocalDateUtil.isRange(v.getDefectStartTime(), finalStartTimeDate, finalEndTimeDate,true,false))
                                            && v.getMaterialId().equals(materialEntry.getKey())
                                            && (v.getProductLineId()!=null && v.getProductLineId().equals(entry3.getKey())))
                                    .sorted(Comparator.comparing(QtCheckTask::getFinishedTime)).collect(Collectors.toList());
                            List<Long> onlineTaskIds = new ArrayList<>();
                            if (CollUtil.isNotEmpty(onlineTaskList)) {
                                onlineTaskList.forEach(tmp -> {
                                    onlineTaskIds.add(tmp.getId());
                                });
                            }
                            //拿到所有在线检data的数据
                            List<QtCheckTaskDetail> onlineDetails = Lists.newArrayList();
                            if (CollUtil.isNotEmpty(onlineTaskIds)) {
//                                onlineDetails = qtCheckTaskDetailService.list(new LambdaQueryWrapper<QtCheckTaskDetail>().in(CollUtil.isNotEmpty(onlineTaskIds), QtCheckTaskDetail::getTaskId, onlineTaskIds));
                                onlineDetails = allTaskDetails.stream().filter(v->onlineTaskIds.contains(v.getTaskId()))
                                        .collect(Collectors.toList());
                            }
                            Map<String, Integer> defectCounts = MapUtil.newHashMap();
                            if (onlineDetails.size() > 0) {
                                //把在线检中的details数据放入
                                for (QtCheckTaskDetail tmp : onlineDetails) {
                                    if (CollUtil.isNotEmpty(tmp.getDefectNum())) {
                                        //避免转换报错
                                        String s1 = JSONObject.toJSONString(tmp.getDefectNum());
                                        List<QtOnlineCheckDefectDTO> defectNum = JSON.parseArray(s1, QtOnlineCheckDefectDTO.class);
                                        //遍历defectNum
                                        for (QtOnlineCheckDefectDTO numTmp : defectNum) {
                                            if (defectCounts.get(numTmp.getDefectName()) == null) {
                                                Integer res = 1;
                                                if (numTmp.getDefectNum() != null) {
                                                    res = numTmp.getDefectNum();
                                                    defectCounts.put(numTmp.getDefectName(), res);
                                                }
                                            } else {
                                                Integer res = 1;
                                                if (numTmp.getDefectNum() != null) {
                                                    res = numTmp.getDefectNum();
                                                    Integer i = defectCounts.get(numTmp.getDefectName());
                                                    i = i + res;
                                                    defectCounts.put(numTmp.getDefectName(), i);
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            //抽检时间业务
                            //巡检 抽检时间在班次日期D班次b的时段内产线A的所有巡检任务中人工选择“次”后关联缺陷项q的数量加总
//                            LambdaQueryWrapper<QtCheckTask> patrolTaskCondition = new LambdaQueryWrapper<QtCheckTask>()
//                                    //任务完成
//                                    .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())
//                                    //完成时间范围
//                                    .ge(QtCheckTask::getSpotTime,startTimeDate).lt(QtCheckTask::getSpotTime,endTimeDate)
//                                    //只要基片车间
//                                    .like(QtCheckTask::getProductLineName, "基片")
//                                    //班次
////                                    .eq(dto.getWorkingOrder() != null, QtCheckTask::getClassesId, entry2.getKey())
//                                    //只要巡检
//                                    .eq(QtCheckTask::getTaskType, QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getCode())
//                                    //该物料
//                                    .eq(QtCheckTask::getMaterialId, materialEntry.getKey())
//                                    //该产线
//                                    .apply(dto.getProductLineId() != null, "JSON_CONTAINS(product_line, JSON_OBJECT('id', {0}))", entry3.getKey())
//                                    .orderByAsc(QtCheckTask::getFinishedTime);
//                            List<QtCheckTask> patrolTaskList = qtCheckTaskService.list(patrolTaskCondition);
                            List<QtCheckTask> patrolTaskList = taskList.stream().filter(v-> (v.getSpotTime()!=null && LocalDateUtil.isRange(v.getSpotTime(), finalStartTimeDate, finalEndTimeDate,true,false))
                                    && v.getMaterialId().equals(materialEntry.getKey())
                                    && (v.getProductLineId()!=null && v.getProductLineId().equals(entry3.getKey())))
                                    .sorted(Comparator.comparing(QtCheckTask::getFinishedTime)).collect(Collectors.toList());
                            List<Long> xunTaskIds = new ArrayList<>();
                            if (CollUtil.isNotEmpty(patrolTaskList)) {
                                patrolTaskList.forEach(tmp -> {
                                    xunTaskIds.add(tmp.getId());
                                });
                            }
                            if (CollUtil.isNotEmpty(xunTaskIds)) {
                                //拿到巡检data数据下该物料的数据
//                                entry3.getValue().forEach(tmp -> {
//                                    if (tmp.getMaterialId().equals(materialEntry.getKey())) {
//                                        xunTaskIds.add(tmp.getId());
//                                    }
//                                });
                                //把巡检的数据放入
//                                List<QtCheckTaskDetail> xunDetails = qtCheckTaskDetailService.list(new LambdaQueryWrapper<QtCheckTaskDetail>().in(QtCheckTaskDetail::getTaskId, xunTaskIds));
                                List<QtCheckTaskDetail> xunDetails = allTaskDetails.stream().filter(v->xunTaskIds.contains(v.getTaskId())).collect(Collectors.toList());
                                List<Integer> defectIds = xunDetails.stream().map(tmp -> tmp.getDefectId()).collect(Collectors.toList());
                                //去重
                                defectIds = defectIds.stream().distinct().collect(Collectors.toList());
                                if(CollUtil.isNotEmpty(defectIds)){
//                                    List<QtDefectiveItem> defectiveItems = qtDefectiveItemService.list(new LambdaQueryWrapper<QtDefectiveItem>().in(QtDefectiveItem::getId,defectIds));
                                    List<Integer> finalDefectIds = defectIds;
                                    List<QtDefectiveItem> defectiveItems = allDefectDetails.stream().filter(v -> finalDefectIds.contains(v.getId())).collect(Collectors.toList());
                                    Map<Integer, List<QtDefectiveItem>> collectMap = defectiveItems.stream().collect(Collectors.groupingBy(QtDefectiveItem::getId));
                                    xunDetails.forEach(tmp -> {
                                        if (tmp.getDefectId() != null) {
                                            QtDefectiveItem defectiveItem = null;
                                            if(CollUtil.isNotEmpty(collectMap.get(tmp.getDefectId()))){
                                                defectiveItem = collectMap.get(tmp.getDefectId()).get(0);
                                            }
                                            if (defectCounts.get(defectiveItem.getName()) != null) {
                                                Integer i = defectCounts.get(defectiveItem.getName());
                                                i = i + 1;
                                                defectCounts.put(defectiveItem.getName(), i);
                                            }
                                        }
                                    });
                                }
                            }
                            //只统计一次
                            if(point == 1){
                                //仪表数据
                                QtChartParamDTO chartParam = new QtChartParamDTO();
                                chartParam.setDateStart(startTimeDate.toLocalDate());
                                chartParam.setDateEnd(startTimeDate.toLocalDate());
                                chartParam.setCheckType(QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getCode());
                                //对应产线
                                chartParam.setProductLineId(entry3.getKey());
                                List<QtCheckTaskDetailReportVO> qtCheckTaskDetailReportVOS = Lists.newArrayList();
                                //判断是否需要接入数采数据
                                if (CollUtil.isNotEmpty(cfContentList) && cfContentList.get(0) == 1) {
                                    try {
                                        qtCheckTaskDetailReportVOS = qtDefectiveReportService.defectCollect(chartParam);
                                    } catch (Exception e) {
                                        log.error(e.getMessage());
                                    }
                                }
                                //加入测点数据
                                if(CollUtil.isNotEmpty(qtCheckTaskDetailReportVOS)){
                                    //获取所有
                                    List<Integer> defectIds = qtCheckTaskDetailReportVOS.stream().map(tmp -> tmp.getDefectId()).collect(Collectors.toList());
                                    //去重
                                    defectIds = defectIds.stream().distinct().collect(Collectors.toList());
                                    if(CollUtil.isNotEmpty(defectIds)){
                                        List<QtDefectiveItem> defectiveItems = qtDefectiveItemService.list(new LambdaQueryWrapper<QtDefectiveItem>().in(QtDefectiveItem::getId,defectIds));
                                        Map<Integer, List<QtDefectiveItem>> collectMap = defectiveItems.stream().collect(Collectors.groupingBy(QtDefectiveItem::getId));
                                        qtCheckTaskDetailReportVOS.forEach(tmp->{
                                            //是当前班次
                                            if(tmp.getClassId().equals(entry2.getKey())){
                                                QtDefectiveItem defectiveItem = null;
                                                if(CollUtil.isNotEmpty(collectMap.get(tmp.getDefectId()))){
                                                    defectiveItem = collectMap.get(tmp.getDefectId()).get(0);
                                                }
                                                if(defectiveItem != null){
                                                    if (defectCounts.get(defectiveItem.getName()) == null) {
                                                        Integer res = 1;
                                                        if (tmp.getDefectNum() != null) {
                                                            res = tmp.getDefectNum();
                                                        }
                                                        defectCounts.put(defectiveItem.getName(), res);
                                                    } else {
                                                        Integer res = 1;
                                                        if (tmp.getDefectNum() != null) {
                                                            res = tmp.getDefectNum();
                                                        }
                                                        Integer i = defectCounts.get(defectiveItem.getName());
                                                        i = i + res;
                                                        defectCounts.put(defectiveItem.getName(), i);
                                                    }

                                                }
                                            }
                                        });
                                    }
                                }
                                point = 2;
                            }
                            //缺陷项q导致损耗重量 = 缺陷项q的数量 * 平均单片重量 / 1000 /2
                            //缺陷项q的数量：缺陷记录时段在班次日期D班次b的时段内、产线A的所有已完成在线检任务记录的缺陷项q数量加总 + 检验完成时间在班次日期D班次b的时段内产线A的所有巡检任务中人工选择“次”后关联缺陷项q的数量加总 + 在班次日期D班次b的时段内产线A的检验设备上报的缺陷项q总数
                            //计算缺陷项Q的损耗重量
                            for (Map.Entry<String, Integer> defectEntry : defectCounts.entrySet()) {
                                //缺陷项q导致损耗重量 = 缺陷项q的数量 * 平均单片重量 / 1000
                                //乘法
                                BigDecimal multiplyRes = BigDecimalUtil.multiply(BigDecimal.valueOf(defectEntry.getValue()), avgDanItem);
                                //缺陷项q导致损耗重量
                                BigDecimal defectResultData = BigDecimalUtil.divide(multiplyRes, new BigDecimal(1000),4);
                                //产品需求多除以2
                                defectResultData = BigDecimalUtil.divide(defectResultData,new BigDecimal(2),4);
                                //把缺陷项q的损耗重量放入全量数据中
                                if (defectLossWeightMap.get(defectEntry.getKey()) == null) {
                                    defectLossWeightMap.put(defectEntry.getKey(), defectResultData);
                                } else {
                                    BigDecimal tmp = defectLossWeightMap.get(defectEntry.getKey());
                                    BigDecimal addRes = BigDecimalUtil.add(tmp, defectResultData);
                                    defectLossWeightMap.put(defectEntry.getKey(), addRes);
                                }
                            }

                            //班次日期D、基片产线A、班次b的各类落板原因导致损耗重量计算公式
                            //班次b的12个小时只生产一种物料（即12个小时内已完成巡检任务的物料相同）
                            //任意落板原因r导致损耗重量 = r导致的落板时长 * 拉引时速 / 玻璃片长度 * 平均单片重量 / 1000000
                            //r导致的落板时长：检验完成时间在班次日期D班次b的时段内、产线A的所有巡检任务记录的r导致的落板时长（落板结束时间 - 落板开始时间）加总 + 缺陷记录时段在班次日期D班次b的时段内、产线A的所有已完成在线检任务记录的r导致的落板时长（落板结束时间 - 落板开始时间）加总，单位为h
                            List<Long> allIds = new ArrayList<>();
                            allIds.addAll(xunTaskIds);
                            allIds.addAll(onlineTaskIds);
                            List<QtCheckTaskDestoryInfo> qtCheckTaskDestoryInfos = Lists.newArrayList();
                            if(CollUtil.isNotEmpty(allIds)){
                                qtCheckTaskDestoryInfos = qtCheckTaskDestoryInfoService.list(new LambdaQueryWrapper<QtCheckTaskDestoryInfo>().in(QtCheckTaskDestoryInfo::getTaskId, allIds));
                            }
                            if (CollUtil.isNotEmpty(qtCheckTaskDestoryInfos)) {
                                BigDecimal luoDateRange = BigDecimal.ZERO;
                                Duration dr = Duration.between(LocalDateTime.now().plusHours(3), LocalDateTime.now());
                                for (QtCheckTaskDestoryInfo info : qtCheckTaskDestoryInfos) {
                                    if (info.getDestoryEndTime() == null && info.getDestoryStartTime() == null) {
                                        log.info("落版时长为空,默认三个小时");
                                    } else {
                                        dr = Duration.between(info.getDestoryStartTime(), info.getDestoryEndTime());
                                    }
                                    //强制变成正数
                                    luoDateRange = BigDecimalUtil.divide(new BigDecimal(Math.abs(dr.toMinutes())), new BigDecimal(60),4);
                                    //如果选了南工位和北工位则时长乘以2
                                    if(info.getDestoryWorkStation().equals(QtManagerConstant.NUM_3)){
                                        luoDateRange = BigDecimalUtil.multiply(luoDateRange,new BigDecimal(2));
                                    }
                                    //任意落板原因r导致损耗重量 = r导致的落板时长 * 拉引时速 / 玻璃片长度 * 平均单片重量 / 1000000
                                    //r导致的落板时长 * 拉引时速
                                    BigDecimal firstStep = BigDecimalUtil.multiply(luoDateRange, avgLaItem);
                                    //r导致的落板时长 * 拉引时速 / 玻璃片长度
                                    BigDecimal secondStep = BigDecimalUtil.divide(firstStep, glassLength,4);
                                    //r导致的落板时长 * 拉引时速 / 玻璃片长度 * 平均单片重量
                                    BigDecimal thirdStep = BigDecimalUtil.multiply(secondStep, avgDanItem);
                                    //r导致的落板时长 * 拉引时速 / 玻璃片长度 * 平均单片重量 / 1000000
//                                    BigDecimal reasonResult = BigDecimalUtil.divide(thirdStep, new BigDecimal(1000000),4);
                                    //落版原因，产品改需求除以二
                                    BigDecimal reasonResult = BigDecimalUtil.divide(thirdStep, new BigDecimal(2),4);
                                    //如果选了南工位和北工位则乘以二
//                                    if(info.getDestoryWorkStation().equals(QtManagerConstant.NUM_3)){
//                                        reasonResult = BigDecimalUtil.multiply(reasonResult,new BigDecimal(2));
//                                    }
                                    if (StrUtil.isBlank(info.getDestoryReason())) {
                                        info.setDestoryReason("");
                                    }
                                    if (luoLossWeightMap.containsKey(info.getDestoryReason())) {
                                        BigDecimal tmp = luoLossWeightMap.get(info.getDestoryReason());
                                        luoLossWeightMap.put(info.getDestoryReason(), BigDecimalUtil.add(tmp, reasonResult));
                                    } else {
                                        luoLossWeightMap.put(info.getDestoryReason(), reasonResult);
                                    }
                                }
                            }
                        }
                        QtChartSubDetailDTO baiResult = new QtChartSubDetailDTO();
                        baiResult.setPullAmount(laQuantity);
                        baiResult.setLossWeight(baiLossWeight);
                        baiResult.setReason("掰边损耗");
                        baiResult.setWorkOrderTime(entry.getKey());
                        if (entry2.getKey().equals(0)) {
                            baiResult.setWorkOrder(QtManagerConstant.QtClassesEnum.DAY.getName());
                        } else {
                            baiResult.setWorkOrder(QtManagerConstant.QtClassesEnum.NIGHT.getName());
                        }
                        baiResult.setProductLineName(productLineName);
                        baiResult.setProductLineId(productLineId);
                        baiResult.setProductWorkShopName(productWorkShopName);
                        baiResult.setProductWorkShopId(productWorkShopId);
                        resultList.add(baiResult);

                        //缺陷总数
                        //放入缺陷项的值
                        for (Map.Entry<String, BigDecimal> defectEntry : defectLossWeightMap.entrySet()) {
                            QtChartSubDetailDTO defectResult = new QtChartSubDetailDTO();
                            defectResult.setPullAmount(laQuantity);
                            defectResult.setLossWeight(defectEntry.getValue());
                            defectResult.setReason(defectEntry.getKey());
                            defectResult.setWorkOrderTime(entry.getKey());
                            if (entry2.getKey().equals(0)) {
                                defectResult.setWorkOrder(QtManagerConstant.QtClassesEnum.DAY.getName());
                            } else {
                                defectResult.setWorkOrder(QtManagerConstant.QtClassesEnum.NIGHT.getName());
                            }
                            defectResult.setProductLineName(productLineName);
                            defectResult.setProductLineId(productLineId);
                            defectResult.setProductWorkShopName(productWorkShopName);
                            defectResult.setProductWorkShopId(productWorkShopId);
                            resultList.add(defectResult);
                        }

                        //落板
                        for (Map.Entry<String, BigDecimal> luoEntry : luoLossWeightMap.entrySet()) {
                            QtChartSubDetailDTO luoResult = new QtChartSubDetailDTO();
                            luoResult.setPullAmount(laQuantity);
                            luoResult.setLossWeight(luoEntry.getValue());
                            luoResult.setReason(luoEntry.getKey());
                            luoResult.setWorkOrderTime(entry.getKey());
                            if (entry2.getKey().equals(0)) {
                                luoResult.setWorkOrder(QtManagerConstant.QtClassesEnum.DAY.getName());
                            } else {
                                luoResult.setWorkOrder(QtManagerConstant.QtClassesEnum.NIGHT.getName());
                            }
                            luoResult.setProductLineName(productLineName);
                            luoResult.setProductLineId(productLineId);
                            luoResult.setProductWorkShopName(productWorkShopName);
                            luoResult.setProductWorkShopId(productWorkShopId);
                            resultList.add(luoResult);
                        }
                    }
                }

            }
        } else {
//            Assert.notEmpty(resultList, "数据为空，暂无报表");
            return resultList;
        }
        TupleDTO<LocalDate,LocalDate> dateDTO = new TupleDTO<>(LocalDate.parse(resultList.get(resultList.size() - 1).getWorkOrderTime()), LocalDate.parse(resultList.get(0).getWorkOrderTime()));
        Map<String, OaGroupCountDTO> classGroupMap = FeignOptional.of(() -> oaInfoService.getGroupInfo(dateDTO), FeignConstant.STECCN_OA_SERVICE_NAME).get();
        resultList.forEach(tmp->{
            OaGroupCountDTO oaGroupCountDTO = classGroupMap.get(tmp.getWorkOrderTime() + ":" + tmp.getWorkOrder());
            if(Objects.nonNull(oaGroupCountDTO)){
                tmp.setClassGroupId(oaGroupCountDTO.getGroupId());
                tmp.setClassGroupName(oaGroupCountDTO.getGroupName());
            }
        });
        return resultList;
    }


    /**
     * 成品率概览
     *
     * @param dto
     * @return
     */
    @Override
    public QtDefectChartVO generateSubstrateOverview(QtTimeDTO dto) {
        QtDefectChartVO<String, BigDecimal, BigDecimal> vo = new QtDefectChartVO();
        //获取生产系统数据
        ProducePassRateDTO pmsDTO = new ProducePassRateDTO();
        R<List<ProducePassRateVO>> rResult;
        LocalDate pmsEndDate;
        //最晚数据完整日期
        LocalDate fullDate = calculateOrder(LocalDateTime.now()).toLocalDate();
        if (dto != null) {
            fullDate = dto.getStartTime();
        }
        LocalDate pmsStartDate;

        //塞入产线list
        SysOfficeDTO officeDTO = new SysOfficeDTO();
        officeDTO.setOfficeName(QtManagerConstant.SUB_OFFICE_NAME);
        officeDTO.setOfficeType(QtManagerConstant.OFFICE_PRODUCE_LINE_TYPE);
        R<List<SysOfficeVO>> byTypeAndLine = remoteOfficeFeign.getByTypeAndLine(officeDTO);
        List<Integer> lineIds = Lists.newArrayList();
        byTypeAndLine.getData().forEach(tmp -> {
            lineIds.add(tmp.getId());
        });
        pmsDTO.setProductLineIdList(lineIds);

        //需要拿到7天的数据
        pmsEndDate = fullDate;
        pmsStartDate = pmsEndDate.minusDays(6);
        pmsDTO.setBeginDate(pmsStartDate);
        pmsDTO.setEndDate(pmsEndDate.plusDays(1));
        rResult = remotePassRateFeignApi.producePassRateGroup(pmsDTO);
        vo.setKey(1);
        vo.setTitle("基片成品率");
        vo.setTooltip(String.valueOf(pmsEndDate.getMonthValue()) + "-" + String.valueOf(pmsEndDate.getDayOfMonth()) + "所有基片车间的总成品率");

        //获取所有基片车间数量
        SysOfficeDTO sysOfficeDTO = new SysOfficeDTO();
        sysOfficeDTO.setOfficeName(QtManagerConstant.SUB_OFFICE_NAME);
        sysOfficeDTO.setOfficeType(QtManagerConstant.OFFICE_WORKSHOP_TYPE);
        R<List<SysOfficeVO>> subWorkShops = remoteOfficeFeign.getByTypeAndLine(sysOfficeDTO);
        int workShopCount = subWorkShops.getData().size();
        List<String> xData = new ArrayList<>();
        List<BigDecimal> yData = new ArrayList<>();
        if (CollUtil.isNotEmpty(rResult.getData())) {
            List<ProducePassRateVO> rList = rResult.getData();
            //根据日期分组
            Map<LocalDate, List<ProducePassRateVO>> resultMap = rList.stream().collect(Collectors.groupingBy(ProducePassRateVO::getDate));
            //根据时间范围算值
            Long dateRange = pmsEndDate.toEpochDay() - pmsStartDate.toEpochDay();
            for (int i = 0; i < dateRange + 1; i++) {
                LocalDate tmpDate = pmsStartDate.plusDays(i);
                xData.add(tmpDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                if (resultMap.get(tmpDate) == null) {
                    yData.add(BigDecimal.ZERO);
                } else {
                    if (resultMap.get(tmpDate).get(0).getDayPassRate() != null) {
                        yData.add(resultMap.get(tmpDate).get(0).getDayPassRate());
                    } else {
                        yData.add(BigDecimal.ZERO);
                    }
                }

            }
            //算日环比
            BigDecimal dayRate = BigDecimalUtil.subtract(resultMap.get(fullDate).get(0).getDayPassRate(), resultMap.get(fullDate.minusDays(1)).get(0).getDayPassRate(), 4);
            if (dayRate.compareTo(BigDecimal.ZERO) < 0) {
                vo.setDayFlag(0);
            } else if (dayRate.compareTo(BigDecimal.ZERO) > 0) {
                vo.setDayFlag(1);
            } else {
                vo.setDayFlag(2);
            }
            //如果为0，则显示为-
            if (dayRate.compareTo(BigDecimal.ZERO) == 0) {
                vo.setDayValue(BigDecimal.ZERO);
            } else {
                vo.setDayValue(dayRate.abs().multiply(BigDecimalUtil.HUNDRED));
            }
            //算周同比
            pmsDTO.setBeginDate(fullDate.minusDays(6));
            pmsDTO.setEndDate(fullDate.minusDays(6).plusDays(1));
            R<List<ProducePassRateVO>> rResult2 = remotePassRateFeignApi.producePassRateGroup(pmsDTO);
            BigDecimal weekRate = new BigDecimal(0);
            if (CollUtil.isNotEmpty(rResult2.getData())) {
                if (rResult2.getData().get(0).getDayPassRate() != null) {
                    weekRate = BigDecimalUtil.subtract(resultMap.get(fullDate).get(0).getDayPassRate(), rResult2.getData().get(0).getDayPassRate());
                }
            }
            if (weekRate.compareTo(BigDecimal.ZERO) == 0) {
                vo.setWeekValue(BigDecimal.ZERO);
            } else {
                vo.setWeekValue(weekRate.multiply(BigDecimalUtil.HUNDRED));
            }
            if (weekRate.compareTo(BigDecimal.ZERO) < 0) {
                vo.setWeekFlag(0);
            } else if (weekRate.compareTo(BigDecimal.ZERO) > 0) {
                vo.setWeekFlag(1);
            } else {
                vo.setWeekFlag(2);
            }
        } else {
            SteccnTableAssertUtil.notEmpty(new ArrayList<>(), "数据为空，暂无报表");
        }
        vo.setXData(xData);
        vo.setYData(yData);
        if (yData.size() == 0) {
            vo.setValue(BigDecimal.ZERO);
        } else {
            vo.setValue(yData.get(yData.size() - 1).multiply(BigDecimalUtil.HUNDRED));
        }
        QtChartTargetValue qtChartTargetValue = qtChartTargetValueService.getOne(new LambdaQueryWrapper<QtChartTargetValue>().eq(QtChartTargetValue::getTarget, 1));
        if (Objects.nonNull(qtChartTargetValue)) {
            if (qtChartTargetValue.getValue() != null) {
                vo.setTargetValue(qtChartTargetValue.getValue());
            } else {
                vo.setTargetValue(null);
            }
        } else {
            vo.setTargetValue(null);
        }
        return vo;
    }

    /**
     * 生成成品率趋势图
     *
     * @param dto
     * @return
     */
    @Override
    public QtChartBarVO generateSubstrateBarTend(QtChartParamDTO dto) {
        QtChartBarVO vo = new QtChartBarVO();
        List<String> xAis = new ArrayList<>();
        //折线图
        QtChartBarDecimalDataVO markLine = new QtChartBarDecimalDataVO();
        List<QtChartBarSeriesVO> series = new ArrayList<>();
        List<BigDecimal> markLineDataList = new ArrayList<>();
        ProducePassRateDTO pmsDTO = new ProducePassRateDTO();
        pmsDTO.setBeginDate(dto.getDateStart());
        pmsDTO.setEndDate(dto.getDateEnd().plusDays(1));
        pmsDTO.setCompare(dto.getCompare());
        pmsDTO.setFilters(dto.getFilters());
        R<List<ProducePassRateVO>> rResult = new R<>();
        //调用数据生成全量数据表
        rResult = remotePassRateFeignApi.producePassRateGroup(pmsDTO);
        if (CollUtil.isNotEmpty(rResult.getData())) {
            List<ProducePassRateVO> rList = rResult.getData();
            List<ProducePassRateVO> finalRList = rList;
            TupleDTO<LocalDate,LocalDate> dateDTO = new TupleDTO<>(finalRList.get(0).getDate(), finalRList.get(finalRList.size()-1).getDate());
            Map<String, OaGroupCountDTO> classGroupMap = FeignOptional.of(() -> oaInfoService.getGroupInfo(dateDTO), FeignConstant.STECCN_OA_SERVICE_NAME).get();
            //加入班组
            rList.forEach(tmp->{
                OaGroupCountDTO oaGroupCountDTO = classGroupMap.get(tmp.getDate() + ":" + tmp.getClassesName());
                if(Objects.nonNull(oaGroupCountDTO)){
                    tmp.setClassGroupId(oaGroupCountDTO.getGroupId());
                    tmp.setClassGroupName(oaGroupCountDTO.getGroupName());
                }
            });
            //先过滤数据
            rList = this.rListFilter(rList, dto);
            //根据日期分组
            Map<LocalDate, List<ProducePassRateVO>> groupByDate = rList.stream().collect(Collectors.groupingBy(ProducePassRateVO::getDate));
            //根据班次、产线、车间分bar类型
            Map<String, QtChartBarSeriesVO> barMap = Maps.newLinkedHashMap();
            //若按照班次分则只有两个
            if (dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.WORK.getCode())) {
                QtChartBarSeriesVO<String> bar1 = new QtChartBarSeriesVO<>();
                List<String> bar1Data = Lists.newArrayList();
                bar1.setData(bar1Data);
                bar1.setName(QtManagerConstant.QtClassesEnum.DAY.getName());
                QtChartBarSeriesVO<String> bar2 = new QtChartBarSeriesVO<>();
                List<String> bar2Data = Lists.newArrayList();
                bar2.setData(bar2Data);
                bar2.setName(QtManagerConstant.QtClassesEnum.NIGHT.getName());
                barMap.put(QtManagerConstant.QtClassesEnum.DAY.getName(), bar1);
                barMap.put(QtManagerConstant.QtClassesEnum.NIGHT.getName(), bar2);
            } else if(dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.CLASSGROUP.getCode())){
                QtChartBarSeriesVO<String> bar1 = new QtChartBarSeriesVO<>();
                List<String> bar1Data = Lists.newArrayList();
                bar1.setData(bar1Data);
                bar1.setName("甲班");
                QtChartBarSeriesVO<String> bar2 = new QtChartBarSeriesVO<>();
                List<String> bar2Data = Lists.newArrayList();
                bar2.setData(bar2Data);
                bar2.setName("乙班");
                QtChartBarSeriesVO<String> bar3 = new QtChartBarSeriesVO<>();
                List<String> bar3Data = Lists.newArrayList();
                bar3.setData(bar3Data);
                bar3.setName("丙班");
                barMap.put("甲班", bar1);
                barMap.put("乙班", bar2);
                barMap.put("丙班", bar3);

            } else if (dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.ALL.getCode())) {
                //不对比则是全部
                QtChartBarSeriesVO<String> bar1 = new QtChartBarSeriesVO<>();
                List<String> bar1Data = Lists.newArrayList();
                bar1.setData(bar1Data);
                bar1.setName("默认");
                barMap.put("默认", bar1);
            } else {
                SysOfficeDTO officeDTO = new SysOfficeDTO();
                officeDTO.setOfficeName(QtManagerConstant.SUB_OFFICE_NAME);
                if (dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.PRODUCTLINE.getCode())) {
                    //产线
                    officeDTO.setOfficeType(String.valueOf(9));
                } else {
                    //车间
                    officeDTO.setOfficeType(String.valueOf(8));
                }
                R<List<SysOfficeVO>> rSysOfficeResult = remoteOfficeFeign.getByTypeAndLine(officeDTO);
                if (CollUtil.isNotEmpty(rSysOfficeResult.getData())) {
                    for (SysOfficeVO tmp : rSysOfficeResult.getData()) {
                        QtChartBarSeriesVO<String> bar1 = new QtChartBarSeriesVO<>();
                        List<String> bar1Data = Lists.newArrayList();
                        bar1.setData(bar1Data);
                        bar1.setName(tmp.getOfficeName());
                        barMap.put(tmp.getOfficeName(), bar1);
                    }
                }
            }
            //先放入x轴的数据和y轴数据
            Long dateRange = dto.getDateEnd().toEpochDay() - dto.getDateStart().toEpochDay();
            for (int i = 0; i < dateRange + 1; i++) {
                LocalDate tmpDate = dto.getDateStart().plusDays(i);
                xAis.add(tmpDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                List<ProducePassRateVO> groupByDateVO = groupByDate.get(tmpDate);
                Map<String, BigDecimal> dataMap = MapUtil.newHashMap();

                //不对比的dateMap
                Map<LocalDate, BigDecimal> noCompareData = MapUtil.newHashMap();
                //若VO不为空
                if (CollUtil.isNotEmpty(groupByDateVO)) {
                    BigDecimal markLineData = BigDecimal.ZERO;
                    if (groupByDateVO.get(0).getDayPassRate() != null) {
                        markLineData = groupByDateVO.get(0).getDayPassRate();
                    }
                    markLineDataList.add(markLineData);
                    //对比维度为班次
                    if (dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.WORK.getCode())) {
                        groupByDateVO.stream().filter(v->v.getPassRate() == null).forEach(v -> {v.setPassRate(BigDecimal.ZERO);});
                        dataMap = groupByDateVO.stream().collect(Collectors.groupingBy(ProducePassRateVO::getClassesName, CollectorsUtil.summingBigDecimal(ProducePassRateVO::getPassRate)));
                    }
                    //对比维度为班组
                    if (dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.CLASSGROUP.getCode())) {
                        groupByDateVO.stream().filter(v->v.getPassRate() == null).forEach(v -> {v.setPassRate(BigDecimal.ZERO);});
                        dataMap = groupByDateVO.stream().collect(Collectors.groupingBy(ProducePassRateVO::getClassGroupName, CollectorsUtil.summingBigDecimal(ProducePassRateVO::getPassRate)));
                    }

                    //对比维度为产线
                    if (dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.PRODUCTLINE.getCode())) {
                        Map<String, List<ProducePassRateVO>> groupByProductLine = groupByDateVO.stream().collect(Collectors.groupingBy(ProducePassRateVO::getProductLineName));
                        if (groupByProductLine.size() > 0) {
                            for (Map.Entry<String, List<ProducePassRateVO>> lineEntry : groupByProductLine.entrySet()) {
                                //拉引量
                                BigDecimal laAmount = BigDecimal.ZERO;
                                //产量
                                BigDecimal yield = BigDecimal.ZERO;
                                for (ProducePassRateVO tmp : lineEntry.getValue()) {
                                    if (tmp.getLaAmount() != null) {
                                        laAmount = BigDecimalUtil.add(laAmount, tmp.getLaAmount());
                                    }
                                    if (tmp.getYield() != null) {
                                        yield = BigDecimalUtil.add(yield, tmp.getYield());
                                    }
                                }
                                dataMap.put(lineEntry.getKey(), BigDecimalUtil.divide(yield, laAmount,4));
                            }
                        }
                    }
                    //对比维度为车间
                    if (dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.WORKSHOP.getCode())) {
                        Map<String, List<ProducePassRateVO>> groupByWorkshopName = groupByDateVO.stream().collect(Collectors.groupingBy(ProducePassRateVO::getWorkshopName));
                        if (groupByWorkshopName.size() > 0) {
                            for (Map.Entry<String, List<ProducePassRateVO>> lineEntry : groupByWorkshopName.entrySet()) {
                                //拉引量
                                BigDecimal laAmount = BigDecimal.ZERO;
                                //产量
                                BigDecimal yield = BigDecimal.ZERO;
                                for (ProducePassRateVO tmp : lineEntry.getValue()) {
                                    if (tmp.getLaAmount() != null) {
                                        laAmount = BigDecimalUtil.add(laAmount, tmp.getLaAmount());
                                    }
                                    if (tmp.getYield() != null) {
                                        yield = BigDecimalUtil.add(yield, tmp.getYield());
                                    }
                                }
                                dataMap.put(lineEntry.getKey(), BigDecimalUtil.divide(yield, laAmount,4));
                            }
                        }
                    }

                    //不对比则直接对日期求和
                    if (dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.ALL.getCode())) {
                        noCompareData = groupByDateVO.stream().collect(Collectors.groupingBy(ProducePassRateVO::getDate, CollectorsUtil.summingBigDecimal(ProducePassRateVO::getPassRate)));
                    }

                } else {
                    markLineDataList.add(BigDecimal.ZERO);
                }
                //不对比的情况
                if (dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.ALL.getCode())) {
                    for (Map.Entry<String, QtChartBarSeriesVO> entry : barMap.entrySet()) {
                        if (noCompareData.get(tmpDate) == null) {
                            entry.getValue().getData().add(new BigDecimal(0));
                        } else {
                            entry.getValue().getData().add(noCompareData.get(tmpDate).setScale(4, BigDecimal.ROUND_HALF_DOWN));
                        }
                    }
                } else {
                    //对key值进行排序
                    if(dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.CLASSGROUP.getCode())){
                        barMap.entrySet()
                                .stream()
                                .sorted(Comparator.comparing(QtChartGenerateServiceImpl::getClassGroupOrder2))
                                .forEachOrdered(e -> {
                                    barMap.put(e.getKey(), e.getValue());
                                });
                    }else {
                        barMap.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEachOrdered(e -> {
                            barMap.put(e.getKey(), e.getValue());
                        });
                    }
                    for (Map.Entry<String, QtChartBarSeriesVO> entry : barMap.entrySet()) {
                        if (dataMap.get(entry.getKey()) == null) {
                            entry.getValue().getData().add(new BigDecimal(0));
                        } else {
                            entry.getValue().getData().add(dataMap.get(entry.getKey()).setScale(4, BigDecimal.ROUND_HALF_DOWN));
                        }
                    }
                }

            }
            for (Map.Entry<String, QtChartBarSeriesVO> entry : barMap.entrySet()) {
                series.add(entry.getValue());
            }
            BaseScreenVO baseScreenVO = new BaseScreenVO();
            //产线
            List<BaseSelectVO<Integer>> productLineSelectList = rList.stream()
                    .collect(Collectors.toMap(
                            ProducePassRateVO::getProductLineId,
                            tmp -> new BaseSelectVO<>(tmp.getProductLineId(), tmp.getProductLineName()),
                            (existing, replacement) -> existing // 处理重复键的情况
                    ))
                    .values()
                    .stream()
                    .collect(Collectors.toList());
            baseScreenVO.setProductLineScreen(productLineSelectList);
            //车间
            List<BaseSelectVO<Integer>> productWorkShopSelectList = rList.stream()
                    .collect(Collectors.toMap(
                            ProducePassRateVO::getWorkshopId,
                            tmp -> new BaseSelectVO<>(tmp.getWorkshopId(), tmp.getWorkshopName()),
                            (existing, replacement) -> existing // 处理重复键的情况
                    ))
                    .values()
                    .stream()
                    .collect(Collectors.toList());
            baseScreenVO.setProductWorkShopScreen(productWorkShopSelectList);
            //班次
            List<BaseSelectVO<Integer>> workingOrderSelectList = rList.stream()
                    .collect(Collectors.toMap(
                            ProducePassRateVO::getClassesName,
                            tmp -> new BaseSelectVO<>(QtManagerConstant.QtClassesEnum.name(tmp.getClassesName()).getCode(),tmp.getClassesName()),
                            (existing, replacement) -> existing // 处理重复键的情况
                    ))
                    .values()
                    .stream()
                    .collect(Collectors.toList());
            baseScreenVO.setWorkingOrderScreen(workingOrderSelectList);
            //班组
            List<BaseSelectVO<Integer>> classGroupSelectList = rList.stream()
                    .collect(Collectors.toMap(
                            ProducePassRateVO::getClassGroupId,
                            tmp -> new BaseSelectVO<>(tmp.getClassGroupId(),tmp.getClassGroupName()),
                            (existing, replacement) -> existing // 处理重复键的情况
                    ))
                    .values()
                    .stream()
                    .collect(Collectors.toList());
            baseScreenVO.setClassGroupScreen(classGroupSelectList);
            vo.setOther(baseScreenVO);
        }
        markLine.setData(markLineDataList);
        vo.setXAis(xAis);
        vo.setMarkLine(markLine);
        vo.setSeries(series);
        return vo;
    }

    /**
     * 基片趋势饼状图
     *
     * @param dto
     * @return
     */
    @Override
    public QtChartPieVO generateSubstratePieTend(QtChartParamDTO dto) {
        //准备变量
        QtChartPieVO<BigDecimal> vo = new QtChartPieVO();
        List<QtChartPieValueVO<BigDecimal>> data = Lists.newArrayList();
        //调用数据生成全量数据表
        ProducePassRateDTO pmsDTO = new ProducePassRateDTO();
        pmsDTO.setBeginDate(dto.getDateStart());
        pmsDTO.setEndDate(dto.getDateEnd().plusDays(1));
        R<List<ProducePassRateVO>> rResult = remotePassRateFeignApi.producePassRateGroup(pmsDTO);
        Map<LocalDate, BigDecimal> groupByDate = MapUtil.newHashMap();
        if (CollUtil.isNotEmpty(rResult.getData())) {
            List<ProducePassRateVO> rList = rResult.getData();
            //先过滤数据
            rList = this.rListFilter(rResult.getData(), dto);
            //根据日期分组
            groupByDate = rList.stream().collect(Collectors.groupingBy(ProducePassRateVO::getDate, CollectorsUtil.summingBigDecimal(ProducePassRateVO::getPassRate)));
            for (Map.Entry<LocalDate, BigDecimal> entry : groupByDate.entrySet()) {
                QtChartPieValueVO<BigDecimal> pieValue = new QtChartPieValueVO<>();
                pieValue.setName(entry.getKey().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                pieValue.setValue(entry.getValue().setScale(2, BigDecimal.ROUND_HALF_DOWN));
                data.add(pieValue);
            }
        }
        vo.setData(data);
        return vo;
    }

    /**
     * 计算最晚数据完整日期
     *
     * @return
     */
    public LocalDateTime calculateOrder(LocalDateTime now) {
        //计算最晚数据完整日期 在区间 (当前日期（T）的8:00:00, 次日（T+1）的8:00:00]内访问系统，最晚数据完整日期为T-1
        LocalDateTime classTimeStart = now.withHour(8).withMinute(0).withSecond(0);
        LocalDateTime classTimeEnd = now.withHour(8).withMinute(0).withSecond(0).plusDays(1);
        //最晚数据完整日期
        LocalDateTime fullDate = now;
        if (now.isAfter(classTimeStart) && now.isBefore(classTimeEnd)) {
            fullDate = now.minusDays(1);
        }
        return fullDate;
    }

    /**
     * 过滤rList
     *
     * @param rList
     * @return
     */
    @Override
    public List<ProducePassRateVO> rListFilter(List<ProducePassRateVO> rList, QtChartParamDTO dto) {
        rList.forEach(tmp -> {
            if (tmp.getWorkshopName() == null) {
                tmp.setWorkshopName("");
            }
            if (tmp.getProductLineId() == null) {
                tmp.setProductLineId(-1);
            }
            if (tmp.getWorkshopId() == null) {
                tmp.setWorkshopId(-1);
            }
            if (tmp.getClassesName() == null) {
                tmp.setClassesName("");
            }
        });
        //过滤基片车间
        rList = rList.stream().filter(s -> s.getWorkshopName().contains("基片")).collect(Collectors.toList());
        //根据接收的值过滤
        //车间
        if (dto.getProductWorkShopId() != null) {
            rList = rList.stream().filter(s -> s.getWorkshopId().equals(dto.getProductWorkShopId())).collect(Collectors.toList());
        }
        //产线
        if (dto.getProductLineId() != null) {
            rList = rList.stream().filter(s -> s.getProductLineId().equals(dto.getProductLineId())).collect(Collectors.toList());
        }
        //班次
        if (dto.getWorkingOrder() != null) {
            //早班
            if (dto.getWorkingOrder().equals(QtManagerConstant.QtClassesEnum.DAY.getCode())) {
                rList = rList.stream().filter(s -> s.getClassesName().equals(QtManagerConstant.QtClassesEnum.DAY.getName())).collect(Collectors.toList());
            } else {
                //晚班
                rList = rList.stream().filter(s -> s.getClassesName().equals(QtManagerConstant.QtClassesEnum.NIGHT.getName())).collect(Collectors.toList());
            }

        }
        //班组
        if(dto.getClassGroupId() != null){
            rList = rList.stream().filter(s->s.getClassGroupId()!=null&&s.getClassGroupId().equals(dto.getClassGroupId())).collect(Collectors.toList());
        }
        return rList;
    }

    /**
     * 生成基片损耗柱状图
     *
     * @param dto
     * @return
     */
    @Override
    public QtLineChartBarDTO generateSubstrateBarLoss(QtChartParamDTO dto) {
        List<QtChartSubDetailDTO> data = this.generateSubstrateLossData(dto);
        //班组为后期加上，所以需要过滤班组
        if(dto.getClassGroupId()!=null){
            data = data.stream().filter(v->v.getClassGroupId()!=null && v.getClassGroupId().equals(dto.getClassGroupId())).collect(Collectors.toList());
        }
        //过滤班次
        if (CollUtil.isNotEmpty(data)&&CollUtil.isNotEmpty(dto.getFilters())&&dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.WORK.getCode())){
            data = data.stream().filter(v->dto.getFilters().contains(v.getWorkOrder())).collect(Collectors.toList());
        }
        //过滤车间
        if (CollUtil.isNotEmpty(data)&&CollUtil.isNotEmpty(dto.getFilters())&&dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.WORKSHOP.getCode())){
            data = data.stream().filter(v->dto.getFilters().contains(v.getProductWorkShopName())).collect(Collectors.toList());
        }
        //过滤产线
        if (CollUtil.isNotEmpty(data)&&CollUtil.isNotEmpty(dto.getFilters())&&dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.PRODUCTLINE.getCode())){
            data = data.stream().filter(v->dto.getFilters().contains(v.getProductLineName())).collect(Collectors.toList());
        }
        //过滤班组
        if (CollUtil.isNotEmpty(data)&&CollUtil.isNotEmpty(dto.getFilters())&&dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.CLASSGROUP.getCode())){
            data = data.stream().filter(v->StrUtil.isNotBlank(v.getClassGroupName())&&dto.getFilters().contains(v.getClassGroupName())).collect(Collectors.toList());
        }
        SteccnTableAssertUtil.notEmpty(data, "数据为空，暂无报表");
        //stream流合并所有名字相同值的拉引量
//        Map<String, BigDecimal> laAmountMap = data.stream().collect(Collectors.groupingBy(
//                QtChartSubDetailDTO::getReason,
//                Collectors.reducing(BigDecimal.ZERO, QtChartSubDetailDTO::getPullAmount, BigDecimal::add)
//        ));
        //累加所有损耗
        BigDecimal totalSun = data.stream()
                .map(QtChartSubDetailDTO::getLossWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (CollUtil.isNotEmpty(data)) {
            //若对比维度为空
            if (dto.getCompare() == null) {
                Map<String, BigDecimal> groupByReason = data.stream().collect(Collectors.groupingBy(QtChartSubDetailDTO::getReason, CollectorsUtil.summingBigDecimal(QtChartSubDetailDTO::getLossWeight)));
                Map<String, BigDecimal> orderByLossWeight = MapUtil.sortByValue(groupByReason, false);
                List<StackGraphDTO<String, BigDecimal>> stackGraphDTOS = new ArrayList<>();
                List<String> legend = Lists.newArrayList();
                for (Map.Entry<String, BigDecimal> reasonEntry : orderByLossWeight.entrySet()) {
                    //一根柱子
                    StackGraphDTO graphDTO = new StackGraphDTO();
                    graphDTO.setXAxis(reasonEntry.getKey());
                    //柱子里的堆叠图
                    TupleDTO<String, BigDecimal> tupleDTO = new TupleDTO();
                    tupleDTO.setField1(reasonEntry.getKey());
                    tupleDTO.setField2(reasonEntry.getValue());
                    List<TupleDTO<String, BigDecimal>> tupleDTOList = new ArrayList<>();
                    //只堆叠一次
                    tupleDTOList.add(tupleDTO);
                    graphDTO.setData(tupleDTOList);
                    stackGraphDTOS.add(graphDTO);
                    //算折线图
                    BigDecimal sunRate = BigDecimal.ZERO;
//                    sunRate = BigDecimalUtil.divide(reasonEntry.getValue(), laAmountMap.get(reasonEntry.getValue()),6);
                    sunRate = BigDecimalUtil.divide(reasonEntry.getValue(), totalSun,6);
                    legend.add(sunRate.toString());
                }
                QtLineChartBarDTO resultDTO = StackGraphUtil.convertData(stackGraphDTOS);
                resultDTO.setLegend(legend);
                return resultDTO;
            } else {
                //所有数据按照先按照损耗原因进行分组
                Map<String, List<QtChartSubDetailDTO>> groupByReason = data.stream().collect(Collectors.groupingBy(QtChartSubDetailDTO::getReason));

                Map<String, BigDecimal> sumByReason = data.stream().collect(Collectors.groupingBy(QtChartSubDetailDTO::getReason, CollectorsUtil.summingBigDecimal(QtChartSubDetailDTO::getLossWeight)));

                //根据损耗原因进行排序
                Map<String, BigDecimal> orderByLossWeight = MapUtil.sortByValue(sumByReason, false);
                //所有的柱子
                List<StackGraphDTO<String, BigDecimal>> stackGraphDTOS = new ArrayList<>();
                //折线图
                List<String> legend = Lists.newArrayList();
                //根据损耗重量分组
                for (Map.Entry<String, BigDecimal> reasonEntry : orderByLossWeight.entrySet()) {
                    //拿到损耗原因的所有值
                    List<QtChartSubDetailDTO> reasonList = groupByReason.get(reasonEntry.getKey());
                    Map<String, List<QtChartSubDetailDTO>> groupByCompare = MapUtil.newHashMap();
                    Map<String, List<QtChartSubDetailDTO>> orderCompare = MapUtil.newHashMap();
                    if (dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.WORK.getCode())) {
                        groupByCompare = reasonList.stream().collect(Collectors.groupingBy(QtChartSubDetailDTO::getWorkOrder));
                        //根据key排序
                        groupByCompare.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByKey())).forEachOrdered(x -> orderCompare.put(x.getKey(), x.getValue()));
                    }
                    if (dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.PRODUCTLINE.getCode())) {
                        groupByCompare = reasonList.stream().collect(Collectors.groupingBy(QtChartSubDetailDTO::getProductLineName));
                        //根据key排序
                        groupByCompare.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByKey())).forEachOrdered(x -> orderCompare.put(x.getKey(), x.getValue()));
                    }
                    if (dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.WORKSHOP.getCode())) {
                        groupByCompare = reasonList.stream().collect(Collectors.groupingBy(QtChartSubDetailDTO::getProductWorkShopName));
                        //根据key排序
                        groupByCompare.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByKey())).forEachOrdered(x -> orderCompare.put(x.getKey(), x.getValue()));
                    }
                    if (dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.CLASSGROUP.getCode())) {
                        groupByCompare = reasonList.stream().collect(Collectors.groupingBy(QtChartSubDetailDTO::getProductWorkShopName));
                        //根据key排序,甲班乙班丙班排序
                        groupByCompare.entrySet().stream().sorted(Comparator.comparingInt(QtChartGenerateServiceImpl::getClassGroupOrder)).forEachOrdered(x -> orderCompare.put(x.getKey(), x.getValue()));
                    }
                    //一根柱子
                    StackGraphDTO graphDTO = new StackGraphDTO();
                    graphDTO.setXAxis(reasonEntry.getKey());

                    //若按照班次堆叠
                    if (dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.WORK.getCode())) {
                        List<TupleDTO<String, BigDecimal>> tupleDTOList = new ArrayList<>();
                        TupleDTO<String, BigDecimal> tupleDTOZAO = new TupleDTO();
                        tupleDTOZAO.setField1("早班");
                        //早班损耗
                        BigDecimal zaoAmount = BigDecimal.ZERO;
                        //晚班损耗
                        BigDecimal wanAmount = BigDecimal.ZERO;
                        //总共损耗
                        BigDecimal sunAmount = BigDecimal.ZERO;
                        if (orderCompare.get("早班") != null) {
                            for (QtChartSubDetailDTO tmp : orderCompare.get("早班")) {
                                zaoAmount = BigDecimalUtil.add(zaoAmount, tmp.getLossWeight());
                                sunAmount = BigDecimalUtil.add(sunAmount, tmp.getLossWeight());
                            }
                        }
                        tupleDTOZAO.setField2(zaoAmount);
                        tupleDTOList.add(tupleDTOZAO);

                        //晚班损耗

                        TupleDTO<String, BigDecimal> tupleDTOWAN = new TupleDTO();
                        tupleDTOWAN.setField1("晚班");
                        if (orderCompare.get("晚班") != null) {
                            for (QtChartSubDetailDTO tmp : orderCompare.get("晚班")) {
                                wanAmount = BigDecimalUtil.add(wanAmount, tmp.getLossWeight());
                                sunAmount = BigDecimalUtil.add(sunAmount, tmp.getLossWeight());
                            }
                        }
                        tupleDTOWAN.setField2(wanAmount);
                        tupleDTOList.add(tupleDTOWAN);
                        graphDTO.setData(tupleDTOList);
                        //算折线图
//                        BigDecimal sunRate = BigDecimalUtil.divide(sunAmount,laAmountMap.get(reasonEntry.getKey()),6);
                        BigDecimal sunRate = BigDecimalUtil.divide(sunAmount,totalSun,6);
                        legend.add(sunRate.toString());
                    }

                    //若按照班组堆叠
                    if (dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.CLASSGROUP.getCode())) {
                        List<TupleDTO<String, BigDecimal>> tupleDTOList = new ArrayList<>();
                        TupleDTO<String, BigDecimal> tupleDTOJIA = new TupleDTO();
                        tupleDTOJIA.setField1("甲班");
                        //甲班损耗
                        BigDecimal jiaAmount = BigDecimal.ZERO;
                        //乙班损耗
                        BigDecimal yiAmount = BigDecimal.ZERO;
                        //丙班损耗
                        BigDecimal binAmount = BigDecimal.ZERO;
                        //总共损耗
                        BigDecimal sunAmount = BigDecimal.ZERO;

                        //甲班损耗
                        if (orderCompare.get("甲班") != null) {
                            for (QtChartSubDetailDTO tmp : orderCompare.get("甲班")) {
                                jiaAmount = BigDecimalUtil.add(jiaAmount, tmp.getLossWeight());
                                sunAmount = BigDecimalUtil.add(sunAmount, tmp.getLossWeight());
                            }
                        }
                        tupleDTOJIA.setField2(jiaAmount);
                        tupleDTOList.add(tupleDTOJIA);

                        //乙班损耗
                        TupleDTO<String, BigDecimal> tupleDTOYI = new TupleDTO();
                        tupleDTOYI.setField1("乙班");
                        if (orderCompare.get("乙班") != null) {
                            for (QtChartSubDetailDTO tmp : orderCompare.get("乙班")) {
                                yiAmount = BigDecimalUtil.add(yiAmount, tmp.getLossWeight());
                                sunAmount = BigDecimalUtil.add(sunAmount, tmp.getLossWeight());
                            }
                        }
                        tupleDTOYI.setField2(yiAmount);
                        tupleDTOList.add(tupleDTOYI);
                        graphDTO.setData(tupleDTOList);

                        //丙班损耗
                        TupleDTO<String, BigDecimal> tupleDTOBING = new TupleDTO();
                        tupleDTOBING.setField1("丙班");
                        if (orderCompare.get("丙班") != null) {
                            for (QtChartSubDetailDTO tmp : orderCompare.get("丙班")) {
                                binAmount = BigDecimalUtil.add(binAmount, tmp.getLossWeight());
                                sunAmount = BigDecimalUtil.add(sunAmount, tmp.getLossWeight());
                            }
                        }
                        tupleDTOBING.setField2(binAmount);
                        tupleDTOList.add(tupleDTOBING);
                        graphDTO.setData(tupleDTOList);
                        //算折线图
//                        BigDecimal sunRate = BigDecimalUtil.divide(sunAmount,laAmountMap.get(reasonEntry.getKey()),6);
                        BigDecimal sunRate = BigDecimalUtil.divide(sunAmount,totalSun,6);
                        legend.add(sunRate.toString());
                    }

                    //按照车间堆叠
                    if (dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.WORKSHOP.getCode())) {
                        //总共损耗
                        BigDecimal sunAmount = BigDecimal.ZERO;
                        List<TupleDTO<String, BigDecimal>> tupleDTOList = new ArrayList<>();
                        SysOfficeDTO officeDTO = new SysOfficeDTO();
                        officeDTO.setOfficeName("基片");
                        officeDTO.setOfficeType("8");
                        R<List<SysOfficeVO>> byTypeAndLine = remoteOfficeFeign.getByTypeAndLine(officeDTO);
                        for (SysOfficeVO sysVo : byTypeAndLine.getData()) {
                            //车间损耗
                            BigDecimal cheAmount = BigDecimal.ZERO;
                            TupleDTO<String, BigDecimal> tupleDTOShop = new TupleDTO();
                            tupleDTOShop.setField1(sysVo.getOfficeName());
                            if (orderCompare.get(sysVo.getOfficeName()) != null) {
                                for (QtChartSubDetailDTO tmp : orderCompare.get(sysVo.getOfficeName())) {
                                    cheAmount = BigDecimalUtil.add(cheAmount, tmp.getLossWeight());
                                    sunAmount = BigDecimalUtil.add(sunAmount, tmp.getLossWeight());
                                }
                            }
                            tupleDTOShop.setField2(cheAmount);
                            tupleDTOList.add(tupleDTOShop);
                        }
                        graphDTO.setData(tupleDTOList);
                        //算折线图
//                        BigDecimal sunRate = BigDecimalUtil.divide(sunAmount,laAmountMap.get(reasonEntry.getKey()),6);
                        BigDecimal sunRate = BigDecimalUtil.divide(sunAmount,totalSun,6);
                        legend.add(sunRate.toString());
                    }

                    //按照产线堆叠
                    if (dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.PRODUCTLINE.getCode())) {
                        //总共损耗
                        BigDecimal sunAmount = BigDecimal.ZERO;
                        List<TupleDTO<String, BigDecimal>> tupleDTOList = new ArrayList<>();
                        SysOfficeDTO officeDTO = new SysOfficeDTO();
                        officeDTO.setOfficeName("基片");
                        officeDTO.setOfficeType("9");
                        R<List<SysOfficeVO>> byTypeAndLine = remoteOfficeFeign.getByTypeAndLine(officeDTO);
                        for (SysOfficeVO sysVo : byTypeAndLine.getData()) {
                            //产线损耗
                            BigDecimal chanAmount = BigDecimal.ZERO;
                            TupleDTO<String, BigDecimal> tupleDTOShop = new TupleDTO();
                            tupleDTOShop.setField1(sysVo.getOfficeName());
                            if (orderCompare.get(sysVo.getOfficeName()) != null) {
                                for (QtChartSubDetailDTO tmp : orderCompare.get(sysVo.getOfficeName())) {
                                    chanAmount = BigDecimalUtil.add(chanAmount, tmp.getLossWeight());
                                    sunAmount = BigDecimalUtil.add(sunAmount, tmp.getLossWeight());
                                }
                            }
                            tupleDTOShop.setField2(chanAmount);
                            tupleDTOList.add(tupleDTOShop);
                            graphDTO.setData(tupleDTOList);
                        }
                        //算折线图
//                        BigDecimal sunRate = BigDecimalUtil.divide(sunAmount,laAmountMap.get(reasonEntry.getKey()),6);
                        BigDecimal sunRate = BigDecimalUtil.divide(sunAmount,totalSun,6);
                        legend.add(sunRate.toString());
                    }
                    stackGraphDTOS.add(graphDTO);

                }
                QtLineChartBarDTO resultDTO = StackGraphUtil.convertData(stackGraphDTOS);
//                legend = Lists.reverse(legend);
                resultDTO.setLegend(legend);
                if(CollUtil.isNotEmpty(data)){
                    BaseScreenVO baseScreenVO = new BaseScreenVO();
                    //产线
                    List<BaseSelectVO<Integer>> productLineSelectList = data.stream()
                            .collect(Collectors.toMap(
                                    QtChartSubDetailDTO::getProductLineId,
                                    tmp -> new BaseSelectVO<>(tmp.getProductLineId(), tmp.getProductLineName()),
                                    (existing, replacement) -> existing // 处理重复键的情况
                            ))
                            .values()
                            .stream()
                            .collect(Collectors.toList());
                    baseScreenVO.setProductLineScreen(productLineSelectList);
                    //车间
                    List<BaseSelectVO<Integer>> productWorkShopSelectList = data.stream()
                            .collect(Collectors.toMap(
                                    QtChartSubDetailDTO::getProductWorkShopId,
                                    tmp -> new BaseSelectVO<>(tmp.getProductWorkShopId(), tmp.getProductWorkShopName()),
                                    (existing, replacement) -> existing // 处理重复键的情况
                            ))
                            .values()
                            .stream()
                            .collect(Collectors.toList());
                    baseScreenVO.setProductWorkShopScreen(productWorkShopSelectList);
                    //班次
                    List<BaseSelectVO<Integer>> workingOrderSelectList = data.stream()
                            .collect(Collectors.toMap(
                                    QtChartSubDetailDTO::getWorkOrder,
                                    tmp -> new BaseSelectVO<>(QtManagerConstant.QtClassesEnum.name(tmp.getWorkOrder()).getCode(), tmp.getWorkOrder()),
                                    (existing, replacement) -> existing // 处理重复键的情况
                            ))
                            .values()
                            .stream()
                            .collect(Collectors.toList());
                    baseScreenVO.setWorkingOrderScreen(workingOrderSelectList);
                    //班组
                    List<BaseSelectVO<Integer>> classGroupSelectList = data.stream()
                            .collect(Collectors.toMap(
                                    QtChartSubDetailDTO::getClassGroupId,
                                    tmp -> new BaseSelectVO<>(tmp.getClassGroupId(),tmp.getClassGroupName()),
                                    (existing, replacement) -> existing // 处理重复键的情况
                            ))
                            .values()
                            .stream()
                            .collect(Collectors.toList());
                    baseScreenVO.setClassGroupScreen(classGroupSelectList);
                    resultDTO.setOther(baseScreenVO);
                }
                return resultDTO;
            }

        }
        return null;
    }

    /**
     * 生成基片损耗饼图
     *
     * @param dto
     * @return
     */
    @Override
    public QtChartPieVO generateSubstratePieLoss(QtChartParamDTO dto) {
        List<QtChartSubDetailDTO> data = this.generateSubstrateLossData(dto);
        //过滤班次
        if (CollUtil.isNotEmpty(data)&&CollUtil.isNotEmpty(dto.getFilters())&&dto.getCompare()!=null&&dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.WORK.getCode())){
            data = data.stream().filter(v->dto.getFilters().contains(v.getWorkOrder())).collect(Collectors.toList());
        }
        //过滤车间
        if (CollUtil.isNotEmpty(data)&&CollUtil.isNotEmpty(dto.getFilters())&&dto.getCompare()!=null&&dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.WORKSHOP.getCode())){
            data = data.stream().filter(v->dto.getFilters().contains(v.getProductWorkShopName())).collect(Collectors.toList());
        }
        //过滤产线
        if (CollUtil.isNotEmpty(data)&&CollUtil.isNotEmpty(dto.getFilters())&&dto.getCompare()!=null&&dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.PRODUCTLINE.getCode())){
            data = data.stream().filter(v->dto.getFilters().contains(v.getProductLineName())).collect(Collectors.toList());
        }
        SteccnTableAssertUtil.notEmpty(data, "数据为空，暂无报表");
        QtChartPieVO result = new QtChartPieVO();
        List<QtChartPieValueVO<BigDecimal>> pieList = new ArrayList<>();
        if (CollUtil.isNotEmpty(data)) {
            //拉引量
            BigDecimal laAmount = BigDecimal.ZERO;
            //损耗总重量
            BigDecimal sunAmount = BigDecimal.ZERO;
            //算所有的拉引量
            for (QtChartSubDetailDTO tmp : data) {
                laAmount = BigDecimalUtil.add(laAmount, tmp.getPullAmount());
                sunAmount = BigDecimalUtil.add(sunAmount, tmp.getLossWeight());
            }
            //若拉引量为0，饼状图显示为暂无数据图片
            if (laAmount.compareTo(BigDecimal.ZERO) == 0) {
                SteccnTableAssertUtil.notEmpty(new ArrayList<>(), "数据为空，暂无报表");
            }
            //对所有损耗数据原因进行加总分析
            Map<String, BigDecimal> reasonData = data.stream().collect(Collectors.groupingBy(QtChartSubDetailDTO::getReason, CollectorsUtil.summingBigDecimal(QtChartSubDetailDTO::getLossWeight)));

            //损耗原因按损耗重量降序排序，成品排在所有损耗原因之后，显示格式：#损耗原因#: #损耗重量#吨(#损耗率#)、成品: #成品重量#吨(#成品率#)
            Map<String, BigDecimal> orderByReason = MapUtil.sortByValue(reasonData, false);
            //成品重量 = 拉引量 - 损耗总重量，单位为吨
            BigDecimal chengWeight = BigDecimalUtil.subtract(laAmount, sunAmount);
            //成品率 = 成品重量 / 拉引量 * 100%，小数点后最多2位
            BigDecimal chengRate = BigDecimalUtil.divide(chengWeight, laAmount,4);
            QtChartPieValueVO<BigDecimal> onePie = new QtChartPieValueVO();
            onePie.setName("成品");
            onePie.setValue(chengRate.abs());

            //遍历损耗原因分组
            for (Map.Entry<String, BigDecimal> reasonEntry : orderByReason.entrySet()) {
                QtChartPieValueVO<BigDecimal> tmp = new QtChartPieValueVO();
                BigDecimal value = BigDecimalUtil.divide(reasonEntry.getValue(), laAmount,4);
                //为0则不放入
                if (value.compareTo(BigDecimal.ZERO) != 0) {
                    tmp.setName(reasonEntry.getKey());
                    tmp.setValue(value);
                    pieList.add(tmp);
                }
            }
            SteccnTableAssertUtil.notEmpty(pieList, "饼图暂无数据");
            pieList.sort(Comparator.comparing(QtChartPieValueVO<BigDecimal>::getValue));
            pieList.add(onePie);
            result.setData(pieList);
            return result;
        } else {
            return result;
        }
    }

    /**
     * 计算拉引量
     * @param classTimeStart
     * @param classTimeEnd
     * @return
     */
    @Override
    public List<QtPullAmountDTO> calculatePullAmount(LocalDate classTimeStart,LocalDate classTimeEnd) {
        List<QtPullAmountDTO> result = Lists.newArrayList();
        //转时间戳
        LocalDateTime startTime = classTimeStart.atTime(0, 0, 1);
        LocalDateTime endTime = classTimeEnd.atTime(23, 59, 59);
        //查到所有满足的巡检任务
        LambdaQueryWrapper<QtCheckTask> taskCondition = new LambdaQueryWrapper<QtCheckTask>()
                //任务完成
                .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())
                //班次时间范围
                .between(QtCheckTask::getClassTime, startTime.toLocalDate(), endTime.toLocalDate())
                //只要巡检
                .eq(QtCheckTask::getTaskType, QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getCode())
                //只要基片车间
                .like(QtCheckTask::getProductLineName, "基片")
                .orderByAsc(QtCheckTask::getFinishedTime);
        List<QtCheckTask> taskList = qtCheckTaskService.list(taskCondition);
        if (CollUtil.isNotEmpty(taskList)) {
            Set<Long> taskIdList = Sets.newHashSet();
            for (QtCheckTask tmp : taskList) {
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                tmp.setDate(df.format(tmp.getClassTime()));
                tmp.setProductLineId(tmp.getProductLine().getId());
                taskIdList.add(tmp.getId());
            }
            //一下获取所有详情数据，便于后续计算
            List<QtCheckTaskDetail> allTaskDetails = qtCheckTaskDetailService.list(new LambdaQueryWrapper<QtCheckTaskDetail>()
                    .in(QtCheckTaskDetail::getTaskId, taskIdList));
            //拿到相关检验项数据 把掰边，样品，拉引时速，负载加入
            List<String> bdlTmp = Lists.newArrayList();
            bdlTmp.add("拉引速度");
            bdlTmp.add("负载");
            List<QtCheckItem> bdlItems = qtCheckItemService.list(new LambdaQueryWrapper<QtCheckItem>()
                    .in(QtCheckItem::getName,bdlTmp));
            //查询负载
            List<QtCheckItem> fuItems = bdlItems.stream().filter(v->v.getName().equals("负载"))
                    .collect(Collectors.toList());
            List<Integer> fuItemIds = new ArrayList<>();
            fuItems.forEach(tmp -> {
                fuItemIds.add(tmp.getId());
            });
            //查询拉引时速(改成了拉引速度)
            List<QtCheckItem> laItems = bdlItems.stream().filter(v -> v.getName().equals("拉引速度"))
                    .collect(Collectors.toList());
            List<Integer> laItemIds = new ArrayList<>();
            laItems.forEach(tmp -> {
                laItemIds.add(tmp.getId());
            });
            //根据班次日期进行分组
            Map<String, List<QtCheckTask>> groupByDate = taskList.stream().collect(Collectors.groupingBy(QtCheckTask::getDate));
            //遍历班次日期
            for (Map.Entry<String, List<QtCheckTask>> entry : groupByDate.entrySet()) {
                List<QtCheckTask> checkTaskDateList = entry.getValue();
                //再根据班次进行分组
                Map<Integer, List<QtCheckTask>> groupByWorkOrder = checkTaskDateList.stream().collect(Collectors.groupingBy(QtCheckTask::getClassesId));
                //遍历班次分组
                for (Map.Entry<Integer, List<QtCheckTask>> entry2 : groupByWorkOrder.entrySet()) {
                    //根据时间排序
                    List<QtCheckTask> checkTaskWorkOrderList = entry2.getValue().stream().sorted(Comparator.comparing(QtCheckTask::getFinishedTime)).collect(Collectors.toList());
                    //根据产线进行分组
                    Map<Integer, List<QtCheckTask>> groupByProductLine = checkTaskWorkOrderList.stream().collect(Collectors.groupingBy(QtCheckTask::getProductLineId));
                    for (Map.Entry<Integer, List<QtCheckTask>> entry3 : groupByProductLine.entrySet()) {
//                        //查看是否是一种物料
//                        HashSet<Long> materialIdSet = new HashSet<>();
//                        //物料id集合
//                        List<Long> materialList = Lists.newArrayList();
//                        if(CollUtil.isNotEmpty(entry3.getValue())){
//                            entry3.getValue().forEach(tmp -> {
//                                materialIdSet.add(tmp.getMaterialId());
//                                if (!materialList.contains(tmp.getMaterialId())) {
//                                    materialList.add(tmp.getMaterialId());
//                                }
//                            });
//                        }
//                        Boolean flag = true;
//                        if (materialIdSet.size() > 1) {
//                            flag = false;
//                        }
//                        //计算生产时长
//                        Map<Long, BigDecimal> materialProduceTime = calculateProduceTime(materialList, flag, entry2);

                        QtPullAmountDTO resultTmp = new QtPullAmountDTO();
                        //产线名字
                        String productLineName = remoteOfficeFeign.getNameById(entry3.getKey()).getData();
                        resultTmp.setProductLineId(entry3.getKey());
                        resultTmp.setProductLineName(productLineName);
                        //车间名字
                        String productWorkShopName = "";
                        R<SysOfficeVO> parent = remoteOfficeFeign.parent(entry3.getKey());
                        if (parent.getData() != null) {
                            productWorkShopName = parent.getData().getOfficeName();
                            resultTmp.setProductWorkShopId(parent.getData().getId());
                            resultTmp.setProductWorkShopName(productWorkShopName);
                        }
                        List<QtCheckTask> productLineTask = entry3.getValue();
                        //拉引量
                        BigDecimal laQuantity = BigDecimal.ZERO;
                        if(CollUtil.isNotEmpty(productLineTask)){
                            //产线下所有任务id
                            List<Long> taskIds = productLineTask.stream().map(QtCheckTask::getId).collect(Collectors.toList());
                            //拉引速度没有变化
                            List<QtCheckTaskDetail> laItemDetails = Lists.newArrayList();
                            Boolean laChangeFlag = true;
                            if (taskIds.size() > 0 && fuItems.size() > 0) {
                                laItemDetails = allTaskDetails.stream().filter(v->taskIds.contains(v.getTaskId())
                                                && laItemIds.contains(v.getItemId()))
                                        .collect(Collectors.toList());
                                //比较所有拉引速度是否有变化
                                if(CollUtil.isNotEmpty(laItemDetails)){
                                    laChangeFlag = laItemDetails.stream().filter(v -> StrUtil.isNotBlank(v.getCheckResult()))
                                            .map(QtCheckTaskDetail::getCheckResult)
                                            .distinct()
                                            .count() == 1;
                                }
                            }
                            //情况一
                            laChangeFlag = true;
                            if(laChangeFlag){
                                //【情况1】班次b的12个小时拉引速度没有变化：拉引量 = 负载平均值f * 12
                                //负载平均值f：查询满足下列条件的巡检任务，记为集合T，集合T内所有任务录入的“负载”取平均值即为f
                                //状态为已检验
                                //产线为A
                                //工位为“基片下片工位（南）”
                                //负载任务id
                                List<Long> fuTaskIds = productLineTask.stream().filter(v -> v.getStation() != null
                                                && v.getStation().getName() != null
                                                && v.getStation().getName().equals(QtManagerConstant.JIPIAN_DOWN_STATION_SOUTH))
                                        .map(QtCheckTask::getId)
                                        .collect(Collectors.toList());
                                //拿到所有负载的值取平均数
                                //获取所有负载
                                List<QtCheckTaskDetail> fuDetails = Lists.newArrayList();
                                if (fuTaskIds.size() > 0 && fuItemIds.size() > 0) {
                                    fuDetails = allTaskDetails.stream().filter(v->fuTaskIds.contains(v.getTaskId())
                                                    && fuItemIds.contains(v.getItemId()))
                                            .collect(Collectors.toList());
                                }
                                if (CollUtil.isNotEmpty(fuDetails)) {
                                    List<QtCheckTaskDetail> fuDetailsTmp = Lists.newArrayList();
                                    //对一个任务下的负载进行分组
                                    Map<Long,List<QtCheckTaskDetail>> fuDetailsMap = fuDetails.stream()
                                            .collect(Collectors.groupingBy(QtCheckTaskDetail::getTaskId));
                                    //算同一个任务下的负载平均值
                                    for(Map.Entry<Long,List<QtCheckTaskDetail>> fuDetailEntry:fuDetailsMap.entrySet()){
                                        List<QtCheckTaskDetail> value = fuDetailEntry.getValue();
                                        BigDecimal totalFuItem = BigDecimal.ZERO;
                                        BigDecimal totalFuCount = BigDecimal.ZERO;
                                        for (QtCheckTaskDetail tmp : value) {
                                            if (StrUtil.isNotBlank(tmp.getCheckResult())) {
                                                BigDecimal decimalTmp = new BigDecimal(tmp.getCheckResult());
                                                totalFuItem = BigDecimalUtil.add(totalFuItem, decimalTmp);
                                                totalFuCount = BigDecimalUtil.add(totalFuCount, new BigDecimal(1));
                                            }
                                        }
                                        QtCheckTaskDetail fu = new QtCheckTaskDetail();
                                        BigDecimal fuDetailRes =BigDecimalUtil.divide(totalFuItem, totalFuCount,4);
                                        if(fuDetailRes.compareTo(BigDecimal.ZERO)!=0){
                                            fu.setCheckResult(fuDetailRes.toString());
                                        }
                                        fuDetailsTmp.add(fu);
                                    }
                                    //算所有的负载平均值
                                    BigDecimal totalFuItem = BigDecimal.ZERO;
                                    BigDecimal totalFuCount = BigDecimal.ZERO;
                                    for (QtCheckTaskDetail tmp : fuDetailsTmp) {
                                        if (StrUtil.isNotBlank(tmp.getCheckResult())) {
                                            BigDecimal decimalTmp = new BigDecimal(tmp.getCheckResult());
                                            totalFuItem = BigDecimalUtil.add(totalFuItem, decimalTmp);
                                            totalFuCount = BigDecimalUtil.add(totalFuCount, new BigDecimal(1));
                                        }
                                    }

                                    //拉引量 = 负载平均值f * 12
                                    BigDecimal avgFuItem = new BigDecimal(0);
                                    if (totalFuItem.intValue() != 0) {
                                        avgFuItem = BigDecimalUtil.divide(totalFuItem, totalFuCount,4);
                                    }
                                    laQuantity = BigDecimalUtil.multiply(avgFuItem,new BigDecimal(12));
                                }
                            }else {
                                // TODO: 2023/6/27
                            }
                        }
                        resultTmp.setPullAmount(laQuantity);
                        resultTmp.setClassId(entry2.getKey());
                        resultTmp.setClassDate(LocalDate.parse(entry.getKey(),DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                        result.add(resultTmp);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public Boolean calculatePullAmountTask() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate date = LocalDate.now();
        List<QtPullAmountDTO> qtPullAmountDTOS = Lists.newArrayList();
        //如果是早8点则查班次日期为昨天的拉引量
        if(QtManagerConstant.NUM_8 == now.getHour()){
            qtPullAmountDTOS = calculatePullAmount(now.toLocalDate().minusDays(1), now.toLocalDate().minusDays(1));
            date = date.minusDays(1);
            //拿到晚班数据
            qtPullAmountDTOS = qtPullAmountDTOS.stream().filter(v->v.getClassId().equals(QtManagerConstant.QtClassesEnum.NIGHT.getCode())).collect(Collectors.toList());
        }else {
            qtPullAmountDTOS = calculatePullAmount(now.toLocalDate(), now.toLocalDate());
            //拿到早班数据
            qtPullAmountDTOS = qtPullAmountDTOS.stream().filter(v->v.getClassId().equals(QtManagerConstant.QtClassesEnum.DAY.getCode())).collect(Collectors.toList());
        }
        if(CollUtil.isNotEmpty(qtPullAmountDTOS)){
            ValidList<PmsPullForceEditDTO> dtoList = new ValidList<>();
            //计算班次
            String className;
            if(qtPullAmountDTOS.get(0).getClassId().equals(QtManagerConstant.QtClassesEnum.DAY.getCode())){
                className = QtManagerConstant.QtClassesEnum.DAY.getName();
            }else {
                className = QtManagerConstant.QtClassesEnum.NIGHT.getName();
            }
            Set<String> nameList = Sets.newHashSet(className);
            Map<String, Long> classMap = FeignOptional.of(() -> remoteDictFeignApi.listByNameCh(nameList), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
            Long classId = classMap.get(className);
            //输入数据
            LocalDate finalDate = date;
            qtPullAmountDTOS.forEach(tmp->{
                PmsPullForceEditDTO dto = new PmsPullForceEditDTO();
                dto.setProductLineId(tmp.getProductLineId());
                dto.setClassesId(classId);
                dto.setPullForceDate(finalDate);
                dto.setWorkshopId(tmp.getProductWorkShopId());
                dto.setQuantity(tmp.getPullAmount());
                dtoList.add(dto);
            });
            Boolean result = FeignOptional.of(() -> remotePullForceFeignApi.edit(dtoList), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
            if(result){
                log.info("载入成功,时间为"+now);
            }
        }else {
            log.info("没有拉引量数据");
        }
//                return true;
//        now = now.of(2023,5,15,8,0);
//        while(now.getMonthValue()!=8||now.getDayOfMonth()!=18){
//            for(int i = 0;i<2;i++){
//                if(i==0){
//                    now = now.withHour(8);
//                }else {
//                    now = now.withHour(20);
//                }
//                date = now.toLocalDate();
//
//            }
//            now = now.plusDays(1);
//        }
        return true;
    }

    @Override
    public List<QtEmsPullAmountDTO> calculateEmsPullAmount(ValidList<QtEmsPullAmountDTO> dto) {
        List<QtEmsPullAmountDTO> resultList = dto.getList();
        //拿到所有班次日期
        resultList = resultList.stream().sorted(Comparator.comparing(QtEmsPullAmountDTO::getClassDate)).collect(Collectors.toList());
        LocalDate startDate = resultList.get(0).getClassDate();
        LocalDate endDate = resultList.get(resultList.size()-1).getClassDate();
        List<Integer> productLineIds = resultList.stream()
                .map(QtEmsPullAmountDTO::getProductLineId)
                .collect(Collectors.toList());
        //去重id
        productLineIds = productLineIds.stream().distinct().collect(Collectors.toList());
        PmsPullForceQueryDTO pmsPullForceQueryDTO = new PmsPullForceQueryDTO();
        pmsPullForceQueryDTO.setStartDate(startDate);
        pmsPullForceQueryDTO.setEndDate(endDate);
        pmsPullForceQueryDTO.setProductLineIds(new HashSet<>(productLineIds));
        List<QtPullAmountDTO> qtPullAmountDTOS = Lists.newArrayList();
        //拿到算好的拉引量
        List<PmsPullForceDataVO> pmsPullForceDataVOS = FeignOptional.of(() -> remotePullForceFeignApi.getDataFromPeriods(pmsPullForceQueryDTO), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
        //查看今天是否在日期范围内,若在范围内则计算当天数据
        LocalDate today = LocalDate.now();
        if (today.isAfter(startDate) && today.isBefore(endDate) || today.isEqual(startDate) || today.isEqual(endDate)) {
            qtPullAmountDTOS = calculatePullAmount(startDate, endDate);
            pmsPullForceDataVOS = pmsPullForceDataVOS.stream().filter(v->!v.getPullForceDate().isEqual(today)).collect(Collectors.toList());
        }
        //处理班次
        Set<String> classNames = Sets.newHashSet(PmManagerConstant.FORE_SHIFT, PmManagerConstant.NIGHT_SHIFT);
        Map<String, Long> classNameMap = FeignOptional.of(() -> remoteDictFeignApi.listByNameCh(classNames), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //转成0,1表示班次的形式
        Map<Long, Integer> classIdCodeMap = classNameMap.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getValue,
                        entry -> entry.getKey().equals("早班") ? 0 : 1));
        //把已经算好的拉引量数据放入
        if(CollUtil.isNotEmpty(pmsPullForceDataVOS)){
            for(PmsPullForceDataVO tmp:pmsPullForceDataVOS){
                QtPullAmountDTO pullAmount = new QtPullAmountDTO();
                pullAmount.setPullAmount(tmp.getQuantity());
                pullAmount.setClassDate(tmp.getPullForceDate());
                pullAmount.setProductLineId(tmp.getProductLineId());
                pullAmount.setClassId(classIdCodeMap.get(tmp.getClassesId()));
                qtPullAmountDTOS.add(pullAmount);
            }
        }
        //处理数据
        for(QtEmsPullAmountDTO tmp:resultList){
            BigDecimal pullAmount = BigDecimal.ZERO;
            if(tmp.getEndTime().isAfter(tmp.getStartTime())){
                //12小时做比较
                BigDecimal hour12 = new BigDecimal(43200);
                Duration dr = Duration.between(tmp.getStartTime(),tmp.getEndTime());
                BigDecimal period = new BigDecimal(dr.getSeconds());
                if(hour12.compareTo(period)==1 || hour12.compareTo(period)==0){
                    //找到满足条件的产线
                    QtPullAmountDTO qtPullAmountDTO = qtPullAmountDTOS.stream().filter(v -> v.getClassDate().equals(tmp.getClassDate())
                            && v.getProductLineId().equals(tmp.getProductLineId())
                            && v.getClassId().equals(tmp.getClassId())).findFirst().orElse(null);
                    if(Objects.nonNull(qtPullAmountDTO)){
                        BigDecimal percent = BigDecimalUtil.divide(period,hour12);
                        pullAmount = BigDecimalUtil.multiply(qtPullAmountDTO.getPullAmount(),percent);
                    }
                }
            }
            tmp.setPullAmount(pullAmount);
        }
        return resultList;
    }

    /**
     * 抽取计算生产时长
     * @return
     */
    private Map<Long, BigDecimal> calculateProduceTime(List<Long> materialList,Boolean flag,Map.Entry<Integer, List<QtCheckTask>> entry2){
        List<QtCheckTask> checkTaskWorkOrderList = entry2.getValue().stream().filter(v->v.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getCode())).sorted(Comparator.comparing(QtCheckTask::getFinishedTime)).collect(Collectors.toList());
        //生产时长map
        Map<Long, BigDecimal> materialProduceTime = MapUtil.newHashMap();
        if (!flag) {
            //保存上一个物料的完成时间
            LocalDateTime preMaterialTime = LocalDateTime.now().withYear(1997);
            //算生产时长
            for(int i = 0; i < materialList.size(); i++){
                Long materialId = materialList.get(i);
                //物料n
                List<QtCheckTask> materialN = checkTaskWorkOrderList.stream().filter(v -> v.getMaterialId().equals(materialId)
                                &&v.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getCode()))
                        .sorted(Comparator.comparing(QtCheckTask::getSpotTime)).collect(Collectors.toList());
                if(i!=materialList.size()-1){
                    Set<Long> pmsParams = new HashSet<>();
                    pmsParams.add(materialId);
                    pmsParams.add(materialList.get(i+1));
                    //物料n+1的最早巡检时间
                    int finalI1 = i;
                    List<QtCheckTask> materialN1 = checkTaskWorkOrderList.stream().filter(v -> v.getMaterialId().equals(materialList.get(finalI1 +1))
                                    &&v.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getCode()))
                            .sorted(Comparator.comparing(QtCheckTask::getFinishedTime)).collect(Collectors.toList());
                    R<Map<Long, PmsProduceOrderVO>> mapR = remotePmsFeignApi.listByMaterialIds(pmsParams);
                    if (mapR.getData() != null) {
                        Map<Long, PmsProduceOrderVO> mapRData = mapR.getData();
                        PmsProduceOrderVO pmsProduceOrderVO1 = mapRData.get(materialId);
                        //若物料n关联的生产订单开始、结束时间均非空，且物料n关联的最晚生产订单结束时间 <= 物料n+1的最早巡检时间
                        if (pmsProduceOrderVO1 != null
                                && pmsProduceOrderVO1.getActualStartTime()!=null
                                && pmsProduceOrderVO1.getActualEndTime()!=null
                                && CollUtil.isNotEmpty(materialN1)
                                && pmsProduceOrderVO1.getActualEndTime().isBefore(materialN1.get(0).getSpotTime())
                                && pmsProduceOrderVO1.getActualEndTime().isAfter(materialN.get(materialN.size()-1).getSpotTime())) {
                            LocalDateTime time = LocalDateTime.now();
                            if(entry2.getKey().equals(QtManagerConstant.QtClassesEnum.DAY.getCode())){
                                //早班
                                time = materialN.get(0).getClassTime().atStartOfDay().withHour(8).withMinute(0).withSecond(0);
                            }else {
                                //晚班
                                time = materialN.get(0).getClassTime().atStartOfDay().withHour(20).withMinute(0).withSecond(0);
                            }
                            //1997则说明前一个物料没有设置时间
                            if(preMaterialTime.getYear() == 1997){
                                Duration dr = Duration.between(time,pmsProduceOrderVO1.getActualEndTime());
                                preMaterialTime = pmsProduceOrderVO1.getActualEndTime();
                                double hours = dr.toMinutes() / 60.0;
                                materialProduceTime.put(materialId, new BigDecimal(Math.abs(hours)));
                            }else {
                                Duration dr = Duration.between(preMaterialTime,pmsProduceOrderVO1.getActualEndTime());
                                preMaterialTime = pmsProduceOrderVO1.getActualEndTime();
                                double hours = dr.toMinutes() / 60.0;
                                materialProduceTime.put(materialId, new BigDecimal(Math.abs(hours)));
                            }

                        } else {
                            //否则，物料n+1的最早巡检时间即为物料n的生产结束时间，统计班次b的时段内各物料的生产时长，单位为h。例如某白班生产2种物料，其关联的生产订单开始、结束时间均为空，14:34:23开始巡检物料2，之前一直巡检物料1，则14:34:23 - 8:00:00（减法）为物料1的生产时长，20:00:00 - 14:34:23（减法）为物料2的生产时长
                            QtCheckTask materialNTmp = materialN.get(0);
                            LocalDateTime time = LocalDateTime.now();
                            if(entry2.getKey().equals(QtManagerConstant.QtClassesEnum.DAY.getCode())){
                                //早班
                                time = materialNTmp.getClassTime().atStartOfDay().withHour(8).withMinute(0).withSecond(0);
                            }else {
                                //晚班
                                time = materialNTmp.getClassTime().atStartOfDay().withHour(20).withMinute(0).withSecond(0);
                            }
                            Duration dr = Duration.between(time,materialN1.get(0).getSpotTime());
                            preMaterialTime = materialN1.get(0).getSpotTime();
                            double hours = dr.toMinutes() / 60.0;
                            materialProduceTime.put(materialId, new BigDecimal(Math.abs(hours)));
                        }
                    }
                }else {
                    LocalDateTime time = LocalDateTime.now();
                    if(entry2.getKey().equals(QtManagerConstant.QtClassesEnum.DAY.getCode())){
                        //早班结束时间
                        time = materialN.get(0).getClassTime().atStartOfDay().withHour(20).withMinute(0).withSecond(0);
                    }else {
                        //晚班结束时间
                        time = materialN.get(0).getClassTime().atStartOfDay().withHour(8).withMinute(0).withSecond(0).plusDays(1);
                    }
                    Duration dr = Duration.between(preMaterialTime,time);
                    double hours = dr.toMinutes() / 60.0;
                    materialProduceTime.put(materialId, new BigDecimal(Math.abs(hours)));
                }
            }
        }
        return materialProduceTime;
    }

    //自定义甲乙丙班的排序
    private static int getClassGroupOrder(Map.Entry<String,List<QtChartSubDetailDTO>> entry){
        List<String> order = Lists.newArrayList("甲班","乙班","丙班");
        return order.indexOf(entry.getKey());
    }

    //成品率自定义
    private static int getClassGroupOrder2(Map.Entry<String, QtChartBarSeriesVO> entry) {
        String key = entry.getKey();
        if (key.equals("甲班")) {
            return 0;
        } else if (key.equals("乙班")) {
            return 1;
        } else if (key.equals("丙班")) {
            return 2;
        } else {
            return Integer.MAX_VALUE;
        }
    }
}
