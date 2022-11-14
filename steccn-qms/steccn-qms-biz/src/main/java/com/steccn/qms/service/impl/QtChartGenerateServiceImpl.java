package com.steccn.qms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.SysOfficeDTO;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.dto.ProducePassRateDTO;
import com.steccn.pms.feign.RemotePassRateFeignApi;
import com.steccn.pms.vo.ProducePassRateVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.*;
import com.steccn.qms.dto.QtChartDateRangeDTO;
import com.steccn.qms.dto.QtChartParamDTO;
import com.steccn.qms.dto.QtChartSubDetailDTO;
import com.steccn.qms.service.*;
import com.steccn.qms.util.CollectorsUtil;
import com.steccn.qms.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.rmi.MarshalledObject;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * 生成图片数据
 * @author AndrewLuo
 * @date 2022/11/10
 */
@Service
@RequiredArgsConstructor
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
    /**
     * 生产基片损耗数据
     * @param dto
     * @return
     */
    @Override
    public List<QtChartSubDetailDTO> generateSubstrateLossData(QtChartParamDTO dto) {
        List<QtChartSubDetailDTO> resultList = new ArrayList<>();
        //转时间戳
        LocalDateTime startTime = dto.getDateStart().atTime(0,0,1);
        LocalDateTime endTime = dto.getDateEnd().atTime(23, 59,59);
        //准备数据
        //1、先根据已知条件查询任务表
        LambdaQueryWrapper<QtCheckTask> taskCondition = new LambdaQueryWrapper<QtCheckTask>()
                //任务完成
                .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED)
                //完成时间范围
                .between(QtCheckTask::getFinishedTime,startTime,endTime)
                //班次
                .eq(dto.getWorkOrderId()!=null,QtCheckTask::getClassesId,dto.getWorkOrderId())
                //只要巡检
                .eq(QtCheckTask::getTaskType,QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getCode())
                //车间
                .apply(dto.getProductWorkShopId() != null, "JSON_CONTAINS(product_workshop, JSON_OBJECT('id', {0}))", dto.getProductWorkShopId())
                //产线
                .apply(dto.getProductLineId() != null, "JSON_CONTAINS(product_line, JSON_OBJECT('id', {0}))", dto.getProductLineId())
                .orderByAsc(QtCheckTask::getFinishedTime);
        List<QtCheckTask> taskList = qtCheckTaskService.list(taskCondition);
        if(CollUtil.isNotEmpty(taskList)){
            for(QtCheckTask tmp:taskList){
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                tmp.setDate(df.format(tmp.getFinishedTime()));
                tmp.setProductLineId(tmp.getProductLine().getId());
            }
            //根据日期进行分组
            Map<String,List<QtCheckTask>> groupByDate = taskList.stream().collect(Collectors.groupingBy(QtCheckTask::getDate));
            //遍历map
            for(Map.Entry<String,List<QtCheckTask>> entry:groupByDate.entrySet()){
                List<QtCheckTask> checkTaskDateList = entry.getValue();
                //再根据班次进行分组
                Map<Integer,List<QtCheckTask>> groupByWorkOrder = checkTaskDateList.stream().collect(Collectors.groupingBy(QtCheckTask::getClassesId));
                //遍历班次分组
                for(Map.Entry<Integer,List<QtCheckTask>> entry2:groupByWorkOrder.entrySet()){
                    List<QtCheckTask> checkTaskWorkOrderList = entry2.getValue();
                    //查看是否是一种物料
                    HashSet<Long> materialIdSet = new HashSet<>();
                    checkTaskWorkOrderList.forEach(tmp->{
                        materialIdSet.add(tmp.getMaterialId());
                    });
                    Boolean flag = true;
                    if(materialIdSet.size() > 0){
                        flag = false;
                    }
                    //查询单片掰边重量
                    List<QtCheckItem> baiItems =  qtCheckItemService.list(new LambdaQueryWrapper<QtCheckItem>()
                            .eq(QtCheckItem::getName,"单片掰边重量"));
                    List<Integer> baiItemIds = new ArrayList<>();
                    baiItems.forEach(tmp->{
                        baiItemIds.add(tmp.getId());
                    });
                    //查询单片重量
                    List<QtCheckItem> danItems =  qtCheckItemService.list(new LambdaQueryWrapper<QtCheckItem>()
                            .eq(QtCheckItem::getName,"单片重量"));
                    List<Integer> danItemIds = new ArrayList<>();
                    danItems.forEach(tmp->{
                        danItemIds.add(tmp.getId());
                    });
                    //查询拉引时速
                    List<QtCheckItem> laItems =  qtCheckItemService.list(new LambdaQueryWrapper<QtCheckItem>()
                            .eq(QtCheckItem::getName,"拉引时速"));
                    List<Integer> laItemIds = new ArrayList<>();
                    laItems.forEach(tmp->{
                        laItemIds.add(tmp.getId());
                    });
                    //一种物料
                    //根据产线进行分组
                    Map<Integer, List<QtCheckTask>> groupByProductLine = checkTaskWorkOrderList.stream().collect(Collectors.groupingBy(QtCheckTask::getProductLineId));
                    //遍历产线
                    for(Map.Entry<Integer,List<QtCheckTask>> entry3:groupByProductLine.entrySet()){
                        //产线名字
                        String productLineName = remoteOfficeFeign.getNameById(entry3.getKey()).getData();
                        //车间名字
                        String productWorkShopName = remoteOfficeFeign.parent(entry3.getKey()).getData().getOfficeName();
                        //拉引量
                        BigDecimal laQuantity = BigDecimal.ZERO;
                        //掰边损耗重量
                        BigDecimal baiLossWeight = BigDecimal.ZERO;
                        //产线下所有物料的缺陷
                        Map<String, BigDecimal> defectLossWeightMap = MapUtil.newHashMap();
                        //产线下所有落板的缺陷
                        Map<String, BigDecimal> luoLossWeightMap = MapUtil.newHashMap();
                        //根据物料进行分组
                        Map<Long,List<QtCheckTask>> groupByMaterial = entry3.getValue().stream().collect(Collectors.groupingBy(QtCheckTask::getMaterialId));
                        //遍历物料分组
                        for(Map.Entry<Long,List<QtCheckTask>> materialEntry:groupByMaterial.entrySet()){
                            BigDecimal totalBaiItem = new BigDecimal(0);
                            BigDecimal totalBaiItemCount = new BigDecimal(0);
                            BigDecimal totalDanItem = new BigDecimal(0);
                            BigDecimal totalDanItemCount = new BigDecimal(0);
                            BigDecimal totalLaItem = new BigDecimal(0);
                            BigDecimal totalLaItemCount = new BigDecimal(0);
                            //准备产线下taskId
                            List<QtCheckTask> checkTaskProductLineList = materialEntry.getValue();
                            List<Long> taskIds = new ArrayList<>();
                            checkTaskProductLineList.forEach(tmp->{
                                taskIds.add(tmp.getId());
                            });
                            //获取所有单片掰边重量
                            List<QtCheckTaskDetail> baiItemDetails = qtCheckTaskDetailService.list(new LambdaQueryWrapper<QtCheckTaskDetail>()
                                    .in(QtCheckTaskDetail::getTaskId,taskIds)
                                    .in(QtCheckTaskDetail::getItemId,baiItemIds));
                            if(CollUtil.isNotEmpty(baiItemDetails)){
                                for(QtCheckTaskDetail tmp:baiItemDetails){
                                    if(tmp.getCheckResult()!=null){
                                        BigDecimal decimalTmp = new BigDecimal(tmp.getCheckResult());
                                        totalBaiItem.add(decimalTmp);
                                        totalBaiItemCount.add(new BigDecimal(1));
                                    }
                                }
                            }
                            //获取所有单片重量
                            List<QtCheckTaskDetail> danItemDetails = qtCheckTaskDetailService.list(new LambdaQueryWrapper<QtCheckTaskDetail>()
                                    .in(QtCheckTaskDetail::getTaskId,taskIds)
                                    .in(QtCheckTaskDetail::getItemId,danItemIds));
                            if(CollUtil.isNotEmpty(danItemDetails)){
                                for(QtCheckTaskDetail tmp:danItemDetails){
                                    if(tmp.getCheckResult()!=null){
                                        BigDecimal decimalTmp = new BigDecimal(tmp.getCheckResult());
                                        totalDanItem.add(decimalTmp);
                                        totalDanItemCount.add(new BigDecimal(1));
                                    }
                                }
                            }
                            //获取所有拉引时速
                            List<QtCheckTaskDetail> laItemDetails = qtCheckTaskDetailService.list(new LambdaQueryWrapper<QtCheckTaskDetail>()
                                    .in(QtCheckTaskDetail::getTaskId,taskIds)
                                    .in(QtCheckTaskDetail::getItemId,laItemIds));
                            if(CollUtil.isNotEmpty(laItemDetails)){
                                for(QtCheckTaskDetail tmp:laItemDetails){
                                    if(tmp.getCheckResult()!=null){
                                        BigDecimal decimalTmp = new BigDecimal(tmp.getCheckResult());
                                        totalLaItem.add(decimalTmp);
                                        totalLaItemCount.add(new BigDecimal(1));
                                    }
                                }
                            }
                            //平均单片掰边重量：产线A在班次日期D的班次b时间段内（例如2021-09-21 8:00:00 ~ 20:00:00）所有巡检任务录入的“单片掰边重量”（需提前配置好“单片掰边重量”检验项）取平均值，单位为kg
                            BigDecimal avgBaiItem = new BigDecimal(0);
                            if(totalBaiItem.intValue()!=0){
                                avgBaiItem = BigDecimalUtil.divide(totalBaiItem,totalBaiItemCount);
                            }
                            //平均单片重量：产线A在班次日期D的班次b时间段内（例如2021-09-21 8:00:00 ~ 20:00:00）所有巡检任务录入的“单片重量”（需提前配置好“单片重量”检验项）取平均值，单位为kg
                            BigDecimal avgDanItem = new BigDecimal(0);
                            if(totalDanItem.intValue()!=0){
                                avgDanItem = BigDecimalUtil.divide(totalDanItem,totalDanItemCount);
                            }
                            //拉引时速：产线A在班次日期D的班次b时间段内（例如2021-09-21 8:00:00 ~ 20:00:00）所有巡检任务录入的“拉引速度”（需提前配置好“拉引速度”检验项）取平均值，单位为m/h
                            BigDecimal avgLaItem = new BigDecimal(0);
                            if(totalDanItem.intValue()!=0){
                                avgLaItem = BigDecimalUtil.divide(totalLaItem,totalLaItemCount);
                            }
                            //玻璃片长度：巡检任务中物料的“玻璃片长度”属性（取自物料主数据），单位为mm
                            BigDecimal glassLength = new BigDecimal(0);
                            if(flag){
                                QtMaterial material = qtMaterialService.getById(checkTaskWorkOrderList.get(0).getMaterialId());
                                if(material.getLengthQuantity()!=null){
                                    glassLength.add(material.getLengthQuantity());
                                }
                            }
                            //掰边损耗重量 = 平均单片掰边重量 * （拉引时速 / 玻璃片长度) * 12 / 1000000
                            //拉引时速/玻璃片长度
                            BigDecimal divideLaGlass = new BigDecimal(0);
                            if(glassLength.compareTo(BigDecimal.ZERO) > 0){
                                divideLaGlass = BigDecimalUtil.divide(avgLaItem,glassLength);
                            }
                            //掰边损耗重量
                            //掰边损耗重量 = 平均单片掰边重量 * （拉引时速 / 玻璃片长度) * 12 / 1000000
                            //平均单片掰边重量 * （拉引时速 / 玻璃片长度)
                            BigDecimal laDivideGlass = BigDecimalUtil.multiply(avgBaiItem,divideLaGlass);
                            BigDecimal laDivideGlass12 = BigDecimalUtil.multiply(laDivideGlass,new BigDecimal(12));
                            BigDecimal oneBaiLossWeight = BigDecimalUtil.divide(laDivideGlass12,new BigDecimal(1000000));
                            baiLossWeight = BigDecimalUtil.add(baiLossWeight,oneBaiLossWeight);

                            //拉引量 = (平均单片掰边重量 + 平均单片重量) * （拉引时速 / 玻璃片长度) * 12 / 1000000
                            //平均单片掰边重量 + 平均单片重量
                            BigDecimal baiAddDan = BigDecimalUtil.add(avgBaiItem,avgDanItem);
                            BigDecimal multi = BigDecimalUtil.multiply(baiAddDan,divideLaGlass);
                            BigDecimal multi12 = BigDecimalUtil.multiply(multi,new BigDecimal(12));

                            //拉引量
                            if(flag){
                                BigDecimal laQuantityOne = BigDecimalUtil.divide(multi12,new BigDecimal(1000000));
                                laQuantity = BigDecimalUtil.add(laQuantityOne,laQuantity);
                            }else {

                            }

                            //缺陷项q导致损耗重量 = 缺陷项q的数量 * 平均单片重量 / 1000
                            //缺陷项q的数量：缺陷记录时段在班次日期D班次b的时段内、产线A的所有已完成在线检任务记录的缺陷项q数量加总 + 检验完成时间在班次日期D班次b的时段内产线A的所有巡检任务中人工选择“次”后关联缺陷项q的数量加总 + 在班次日期D班次b的时段内产线A的检验设备上报的缺陷项q总数
                            BigDecimal defectAmount = BigDecimal.ZERO;
                            //班次时间范围(早班)
                            String morningStr = entry.getKey() + " 08:00:00";
                            String eveningStr = entry.getKey() + " 20:00:00";
                            LocalDateTime startTimeDate = LocalDateTime.now();
                            LocalDateTime endTimeDate = LocalDateTime.now();
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                            //早班则是当天
                            if(entry2.getKey().equals(0)){
                                startTimeDate = LocalDateTime.parse(morningStr,formatter);
                                endTimeDate = LocalDateTime.parse(eveningStr,formatter);
                            }else {
                                startTimeDate = LocalDateTime.parse(eveningStr,formatter);
                                endTimeDate = LocalDateTime.parse(morningStr,formatter);
                                endTimeDate = endTimeDate.plusDays(1);
                            }
                            LambdaQueryWrapper<QtCheckTask> onlineTaskCondition = new LambdaQueryWrapper<QtCheckTask>()
                                    //任务完成
                                    .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED)
                                    //完成时间范围
                                    .between(QtCheckTask::getFinishedTime,startTimeDate,endTimeDate)
                                    //班次
                                    .eq(dto.getWorkOrderId()!=null,QtCheckTask::getClassesId,entry2.getKey())
                                    //只要在线检
                                    .eq(QtCheckTask::getTaskType,QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode())
                                    //该物料
                                    .eq(QtCheckTask::getMaterialId,materialEntry.getKey())
                                    //该产线
                                    .apply(dto.getProductLineId() != null, "JSON_CONTAINS(product_line, JSON_OBJECT('id', {0}))",entry3.getKey())
                                    .orderByAsc(QtCheckTask::getFinishedTime);
                            //缺陷项q导致损耗重量 = 缺陷项q的数量 * 平均单片重量 / 1000
                            //缺陷项q的数量：缺陷记录时段在班次日期D班次b的时段内、产线A的所有已完成在线检任务记录的缺陷项q数量加总 + 检验完成时间在班次日期D班次b的时段内产线A的所有巡检任务中人工选择“次”后关联缺陷项q的数量加总 + 在班次日期D班次b的时段内产线A的检验设备上报的缺陷项q总数
                            List<QtCheckTask> onlineTaskList = qtCheckTaskService.list(onlineTaskCondition);
                            List<Long> onlineTaskIds = new ArrayList<>();
                            if(CollUtil.isNotEmpty(onlineTaskList)){
                                onlineTaskList.forEach(tmp->{
                                    onlineTaskIds.add(tmp.getId());
                                });
                            }
                            //拿到所有在线检data的数据
                            List<QtCheckTaskDetail> onlineDetails = qtCheckTaskDetailService.list(new LambdaQueryWrapper<QtCheckTaskDetail>().in(QtCheckTaskDetail::getTaskId,onlineTaskList));
                            Map<String,Integer> defectCounts = MapUtil.newHashMap();
                            //把在线检中的details数据放入
                            onlineDetails.forEach(tmp->{
                                if(CollUtil.isNotEmpty(tmp.getDefectNum())){
                                    tmp.getDefectNum().forEach(numTmp->{
                                        if(defectCounts.get(numTmp.getDefectName())!=null){
                                            defectCounts.put(numTmp.getDefectName(),numTmp.getDefectNum());
                                        }else {
                                            Integer i = defectCounts.get(numTmp.getDefectName());
                                            i = i + numTmp.getDefectNum();
                                            defectCounts.put(numTmp.getDefectName(),i);
                                        }
                                    });
                                }
                            });

                            //拿到巡检data数据下该物料的数据
                            List<Long> xunTaskIds = new ArrayList<>();
                            entry3.getValue().forEach(tmp->{
                                if(tmp.getMaterialId().equals(materialEntry.getKey())){
                                    xunTaskIds.add(tmp.getId());
                                }
                            });
                            //把巡检的数据放入
                            List<QtCheckTaskDetail> xunDetails = qtCheckTaskDetailService.list(new LambdaQueryWrapper<QtCheckTaskDetail>().in(QtCheckTaskDetail::getTaskId,xunTaskIds));
                            xunDetails.forEach(tmp -> {
                                if(tmp.getDefectId()!=null){
                                    QtDefectiveItem defectiveItem = qtDefectiveItemService.getById(tmp.getDefectId());
                                    if(defectCounts.get(defectiveItem.getName())!=null){
                                        Integer i = defectCounts.get(defectiveItem.getName());
                                        defectCounts.put(defectiveItem.getName(),i++);
                                    }
                                }
                            });
                            //缺陷项q导致损耗重量 = 缺陷项q的数量 * 平均单片重量 / 1000
                            //缺陷项q的数量：缺陷记录时段在班次日期D班次b的时段内、产线A的所有已完成在线检任务记录的缺陷项q数量加总 + 检验完成时间在班次日期D班次b的时段内产线A的所有巡检任务中人工选择“次”后关联缺陷项q的数量加总 + 在班次日期D班次b的时段内产线A的检验设备上报的缺陷项q总数
                            //计算缺陷项Q的损耗重量
                            for(Map.Entry<String,Integer> defectEntry:defectCounts.entrySet()){
                                //缺陷项q导致损耗重量 = 缺陷项q的数量 * 平均单片重量 / 1000
                                //乘法
                                BigDecimal multiplyRes = BigDecimalUtil.multiply(BigDecimal.valueOf(defectEntry.getValue()),avgDanItem);
                                //缺陷项q导致损耗重量
                                BigDecimal defectResultData = BigDecimalUtil.divide(multiplyRes,new BigDecimal(1000));

                                //把缺陷项q的损耗重量放入全量数据中
                                if(defectLossWeightMap.get(defectEntry.getKey())!=null){
                                    defectLossWeightMap.put(defectEntry.getKey(),defectResultData);
                                }else {
                                    BigDecimal tmp = defectLossWeightMap.get(defectEntry.getKey());
                                    BigDecimal addRes = BigDecimalUtil.add(tmp,defectResultData);
                                    defectLossWeightMap.put(defectEntry.getKey(),addRes);
                                }
                            }

                            //班次日期D、基片产线A、班次b的各类落板原因导致损耗重量计算公式
                            //班次b的12个小时只生产一种物料（即12个小时内已完成巡检任务的物料相同）
                            //任意落板原因r导致损耗重量 = r导致的落板时长 * 拉引时速 / 玻璃片长度 * 平均单片重量 / 1000000
                            //r导致的落板时长：检验完成时间在班次日期D班次b的时段内、产线A的所有巡检任务记录的r导致的落板时长（落板结束时间 - 落板开始时间）加总 + 缺陷记录时段在班次日期D班次b的时段内、产线A的所有已完成在线检任务记录的r导致的落板时长（落板结束时间 - 落板开始时间）加总，单位为h
                            List<Long> allIds = new ArrayList<>();
                            allIds.addAll(xunTaskIds);
                            allIds.addAll(onlineTaskIds);
                            List<QtCheckTaskDestoryInfo> qtCheckTaskDestoryInfos = qtCheckTaskDestoryInfoService.list(new LambdaQueryWrapper<QtCheckTaskDestoryInfo>().in(QtCheckTaskDestoryInfo::getTaskId, allIds));
                            if(CollUtil.isNotEmpty(qtCheckTaskDestoryInfos)){
                                BigDecimal luoDateRange = BigDecimal.ZERO;
                                for(QtCheckTaskDestoryInfo info:qtCheckTaskDestoryInfos){
                                    Duration dr = Duration.between(info.getDestoryEndTime(),info.getDestoryStartTime());
                                    luoDateRange = BigDecimalUtil.divide(new BigDecimal(dr.toMinutes()),new BigDecimal(60));
                                    //任意落板原因r导致损耗重量 = r导致的落板时长 * 拉引时速 / 玻璃片长度 * 平均单片重量 / 1000000
                                    //r导致的落板时长 * 拉引时速
                                    BigDecimal firstStep = BigDecimalUtil.multiply(luoDateRange,avgLaItem);
                                    //r导致的落板时长 * 拉引时速 / 玻璃片长度
                                    BigDecimal secondStep = BigDecimalUtil.divide(firstStep,glassLength);
                                    //r导致的落板时长 * 拉引时速 / 玻璃片长度 * 平均单片重量
                                    BigDecimal thirdStep = BigDecimalUtil.multiply(secondStep,avgDanItem);
                                    //r导致的落板时长 * 拉引时速 / 玻璃片长度 * 平均单片重量 / 1000000
                                    BigDecimal reasonResult = BigDecimalUtil.divide(thirdStep,new BigDecimal(1000000));
                                    if(StrUtil.isBlank(info.getDestoryReason())){
                                        info.setDestoryReason("");
                                    }
                                    if(luoLossWeightMap.containsKey(info.getDestoryReason())){
                                        BigDecimal tmp = luoLossWeightMap.get(info.getDestoryReason());
                                        luoLossWeightMap.put(info.getDestoryReason(),BigDecimalUtil.add(tmp,reasonResult));
                                    }else {
                                        luoLossWeightMap.put(info.getDestoryReason(),reasonResult);
                                    }
                                }
                            }
                        }
                        QtChartSubDetailDTO baiResult = new QtChartSubDetailDTO();
                        baiResult.setPullAmount(new DecimalFormat("0.00").format(laQuantity));
                        baiResult.setLossWeight(new DecimalFormat("0.00").format(baiLossWeight));
                        baiResult.setReason("掰边损耗");
                        baiResult.setWorkOrderTime(entry.getKey());
                        if(entry2.getKey().equals(0)){
                            baiResult.setWorkOrder(QtManagerConstant.QtClassesEnum.DAY.getName());
                        }else {
                            baiResult.setWorkOrder(QtManagerConstant.QtClassesEnum.NIGHT.getName());
                        }
                        baiResult.setProductLineName(productLineName);
                        baiResult.setProductWorkShopName(productWorkShopName);
                        resultList.add(baiResult);

                        //缺陷总数
                        //放入缺陷项的值
                        for(Map.Entry<String,BigDecimal> defectEntry :defectLossWeightMap.entrySet()){
                            QtChartSubDetailDTO defectResult = new QtChartSubDetailDTO();
                            defectResult.setPullAmount(new DecimalFormat("0.00").format(laQuantity));
                            defectResult.setLossWeight(new DecimalFormat("0.00").format(defectEntry.getValue()));
                            defectResult.setReason(defectEntry.getKey());
                            defectResult.setWorkOrderTime(entry.getKey());
                            if(entry2.getKey().equals(0)){
                                defectResult.setWorkOrder(QtManagerConstant.QtClassesEnum.DAY.getName());
                            }else {
                                defectResult.setWorkOrder(QtManagerConstant.QtClassesEnum.NIGHT.getName());
                            }
                            defectResult.setProductLineName(productLineName);
                            defectResult.setProductWorkShopName(productWorkShopName);
                            resultList.add(defectResult);
                        }

                        //落板
                        for(Map.Entry<String,BigDecimal> luoEntry : luoLossWeightMap.entrySet()){
                            QtChartSubDetailDTO luoResult = new QtChartSubDetailDTO();
                            luoResult.setPullAmount(new DecimalFormat("0.00").format(laQuantity));
                            luoResult.setLossWeight(new DecimalFormat("0.00").format(luoEntry.getValue()));
                            luoResult.setReason(luoEntry.getKey());
                            luoResult.setWorkOrderTime(entry.getKey());
                            if(entry2.getKey().equals(0)){
                                luoResult.setWorkOrder(QtManagerConstant.QtClassesEnum.DAY.getName());
                            }else {
                                luoResult.setWorkOrder(QtManagerConstant.QtClassesEnum.NIGHT.getName());
                            }
                            luoResult.setProductLineName(productLineName);
                            luoResult.setProductWorkShopName(productWorkShopName);
                            resultList.add(luoResult);
                        }
                    }
                }

            }
        }else {
            return resultList;
        }
        return resultList;
    }

    /**
     * 成品率概览
     * @param dto
     * @return
     */
    @Override
    public QtDefectChartVO generateSubstrateOverview(QtChartDateRangeDTO dto) {
        QtDefectChartVO<String,String,String> vo = new QtDefectChartVO();
        //获取生产系统数据
        ProducePassRateDTO pmsDTO = new ProducePassRateDTO();
        R<List<ProducePassRateVO>> rResult;
        LocalDate pmsEndDate;
        //最晚数据完整日期
        LocalDateTime fullDate = calculateOrder(LocalDateTime.now());
        LocalDate pmsStartDate;
        //没传时间范围则是部门分析
        if(dto == null){
            //需要拿到7天的数据
            pmsEndDate = fullDate.toLocalDate();
            pmsStartDate = pmsEndDate.minusDays(6);
            pmsDTO.setBeginDate(pmsStartDate);
            pmsDTO.setEndDate(pmsEndDate);
            rResult = remotePassRateFeignApi.producePassRateGroup(pmsDTO);
        }else {
            pmsEndDate = dto.getEndTime();
            pmsStartDate =dto.getStartTime();
            pmsDTO.setBeginDate(dto.getStartTime());
            pmsDTO.setEndDate(dto.getEndTime());
            rResult = remotePassRateFeignApi.producePassRateGroup(pmsDTO);
        }
        vo.setKey(1);
        vo.setTitle("基片成品率");
        if(dto == null){
            vo.setTooltip(String.valueOf(pmsStartDate.getMonthValue()) + "-" + String.valueOf(pmsStartDate.getDayOfMonth()) + "所有基片车间的总成品率");
        }else {
            vo.setTooltip(String.valueOf(dto.getStartTime().getMonthValue()) + "-" + String.valueOf(dto.getEndTime().getDayOfMonth()) + "所有基片车间的总成品率");
        }
        List<String> xData = new ArrayList<>();
        List<String> yData = new ArrayList<>();
        if(CollUtil.isNotEmpty(rResult.getData())){
            List<ProducePassRateVO> rList = rResult.getData();
            //过滤基片车间
            rList = rList.stream().filter(s->s.getWorkshopName().contains("基片")).collect(Collectors.toList());
            //根据日期分组
            Map<LocalDate,BigDecimal> resultMap = rList.stream().collect(Collectors.groupingBy(ProducePassRateVO::getDate,CollectorsUtil.summingBigDecimal(ProducePassRateVO::getPassRate)));
            //根据时间范围算值
            Long dateRange = pmsEndDate.toEpochDay() - pmsStartDate.toEpochDay();
            for(int i = 0; i < dateRange; i++){
                LocalDate tmpDate = pmsStartDate.plusDays(i);
                xData.add(tmpDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                if(resultMap.get(tmpDate) == null){
                    yData.add(new DecimalFormat("0.00%").format(BigDecimal.ZERO));
                }else {
                    yData.add(new DecimalFormat("0.00%").format(resultMap.get(tmpDate)));
                }

            }
            //算日环比
            BigDecimal dayRate = resultMap.get(fullDate.toLocalDate()).subtract(resultMap.get(fullDate.toLocalDate().minusDays(1)));
            if(dayRate.compareTo(BigDecimal.ZERO) < 0){
                vo.setDayFlag(0);
            }else {
                vo.setDayFlag(1);
            }
            //如果为0，则显示为-
            if(dayRate.compareTo(BigDecimal.ZERO) == 0){
                vo.setDayValue("-");
            }else {
                vo.setDayValue(new DecimalFormat("0.00%").format(dayRate));
            }
            //算周同比
            pmsDTO.setBeginDate(fullDate.minusDays(6).toLocalDate());
            pmsDTO.setEndDate(fullDate.minusDays(6).toLocalDate());
            R<List<ProducePassRateVO>> rResult2 = remotePassRateFeignApi.producePassRateGroup(pmsDTO);
            BigDecimal weekRate = new BigDecimal(0);
            if(CollUtil.isNotEmpty(rResult2.getData())){
                if(rResult2.getData().get(0).getPassRate()!=null){
                    weekRate = resultMap.get(fullDate.toLocalDate()).subtract(rResult2.getData().get(0).getPassRate());
                }
            }
            if(weekRate.compareTo(BigDecimal.ZERO) == 0){
                vo.setWeekValue("-");
            }else {
                vo.setWeekValue(new DecimalFormat("0.00%").format(weekRate));
            }
        }
        vo.setXData(xData);
        vo.setYData(yData);
        if(yData.size() == 0){
            vo.setValue("0.00%");
        }else {
            vo.setValue(yData.get(yData.size()));
        }
        BigDecimal targetValue = qtChartTargetValueService.getOne(new LambdaQueryWrapper<QtChartTargetValue>().eq(QtChartTargetValue::getTarget,1)).getValue();
        if(targetValue == null) {
            targetValue = BigDecimal.ZERO;
        }
        vo.setTargetValue(new DecimalFormat("0.00%").format(targetValue));
        return vo;
    }

    /**
     * 生成成品率趋势图
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
        List<String> markLineDataList = new ArrayList<>();
        ProducePassRateDTO pmsDTO = new ProducePassRateDTO();
        pmsDTO.setBeginDate(dto.getDateStart());
        pmsDTO.setEndDate(dto.getDateEnd());
        R<List<ProducePassRateVO>> rResult = new R<>();
        //调用数据生成全量数据表
        rResult = remotePassRateFeignApi.producePassRateGroup(pmsDTO);
        if(CollUtil.isNotEmpty(rResult.getData())){
            //先过滤数据,只要基片车间
            List<ProducePassRateVO> rList = rResult.getData();
            //过滤基片车间
            rList = rList.stream().filter(s->s.getWorkshopName().contains("基片")).collect(Collectors.toList());
            //根据日期分组
            Map<LocalDate,List<ProducePassRateVO>> groupByDate = rList.stream().collect(Collectors.groupingBy(ProducePassRateVO::getDate));
            //根据班次、产线、车间分bar类型
            Map<String,QtChartBarSeriesVO> barMap = MapUtil.newHashMap();
            //若按照班次分则只有两个
            if(dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.WORK.getCode())){
                QtChartBarSeriesVO<String> bar1 = new QtChartBarSeriesVO<>();
                List<String> bar1Data = Lists.newArrayList();
                bar1.setData(bar1Data);
                bar1.setName(QtManagerConstant.QtClassesEnum.DAY.getName());
                QtChartBarSeriesVO<String> bar2 = new QtChartBarSeriesVO<>();
                List<String> bar2Data = Lists.newArrayList();
                bar2.setData(bar2Data);
                bar2.setName(QtManagerConstant.QtClassesEnum.NIGHT.getName());
                barMap.put(QtManagerConstant.QtClassesEnum.DAY.getName(),bar1);
                barMap.put(QtManagerConstant.QtClassesEnum.NIGHT.getName(), bar2);
            }else {
                SysOfficeDTO officeDTO = new SysOfficeDTO();
                officeDTO.setOfficeName(QtManagerConstant.SUB_OFFICE_NAME);
                if(dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.PRODUCTLINE.getCode())){
                    //产线
                    officeDTO.setOfficeType(String.valueOf(9));
                }else {
                    //车间
                    officeDTO.setOfficeType(String.valueOf(8));
                }
                R<List<SysOfficeVO>> rSysOfficeResult = remoteOfficeFeign.getByTypeAndLine(officeDTO);
                if(CollUtil.isNotEmpty(rSysOfficeResult.getData())){
                    rSysOfficeResult.getData().forEach(tmp -> {
                        QtChartBarSeriesVO<String> bar1 = new QtChartBarSeriesVO<>();
                        List<String> bar1Data = Lists.newArrayList();
                        bar1.setData(bar1Data);
                        bar1.setName(tmp.getOfficeName());
                        barMap.put(tmp.getOfficeName(),bar1);
                    });
                }
            }
            //先放入x轴的数据和y轴数据
            Long dateRange = dto.getDateEnd().toEpochDay() - dto.getDateStart().toEpochDay();
            for(int i = 0; i < dateRange; i++){
                LocalDate tmpDate = dto.getDateStart().plusDays(i);
                xAis.add(tmpDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                List<ProducePassRateVO> groupByDateVO = groupByDate.get(tmpDate);
                Map<String,BigDecimal> dataMap = MapUtil.newHashMap();
                //若VO不为空
                if(CollUtil.isNotEmpty(groupByDateVO)){
                    //对markLine进行求和
                    BigDecimal markLineData = groupByDateVO.stream().collect(CollectorsUtil.summingBigDecimal(ProducePassRateVO::getPassRate));
                    markLineDataList.add(new DecimalFormat("0.00%").format(markLineData));
                    //对比维度为班次
                    if(dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.WORK.getCode())){
                        dataMap = groupByDateVO.stream().collect(Collectors.groupingBy(ProducePassRateVO::getClassesName,CollectorsUtil.summingBigDecimal(ProducePassRateVO::getPassRate)));
                    }
                    //对比维度为产线
                    if(dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.PRODUCTLINE.getCode())){
                        dataMap = groupByDateVO.stream().collect(Collectors.groupingBy(ProducePassRateVO::getProductLineName,CollectorsUtil.summingBigDecimal(ProducePassRateVO::getPassRate)));
                    }
                    //对比维度为车间
                    if(dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.WORKSHOP.getCode())){
                        dataMap = groupByDateVO.stream().collect(Collectors.groupingBy(ProducePassRateVO::getWorkshopName,CollectorsUtil.summingBigDecimal(ProducePassRateVO::getPassRate)));
                    }
                    for(Map.Entry<String,QtChartBarSeriesVO> entry:barMap.entrySet()){
                        if(dataMap.get(entry.getKey())==null){
                            entry.getValue().getData().add("0.00%");
                        }else {
                            entry.getValue().getData().add(new DecimalFormat("0.00%").format(dataMap.get(entry.getKey())));
                        }
                    }
                }
            }
            for(Map.Entry<String,QtChartBarSeriesVO> entry:barMap.entrySet()){
                series.add(entry.getValue());
            }
        }
        markLine.setData(markLineDataList);
        vo.setXAis(xAis);
        vo.setMarkLine(markLine);
        vo.setSeries(series);
        return vo;
    }

    /**
     * 基片趋势饼状图
     * @param dto
     * @return
     */
    @Override
    public QtChartPieVO generateSubstratePieTend(QtChartParamDTO dto) {
        //准备变量
        QtChartPieVO<String> vo = new QtChartPieVO();
        List<QtChartPieValueVO<String>> data = Lists.newArrayList();
        //调用数据生成全量数据表
        ProducePassRateDTO pmsDTO = new ProducePassRateDTO();
        pmsDTO.setBeginDate(dto.getDateStart());
        pmsDTO.setEndDate(dto.getDateEnd());
        R<List<ProducePassRateVO>> rResult = remotePassRateFeignApi.producePassRateGroup(pmsDTO);;
        Map<LocalDate,BigDecimal> groupByDate = MapUtil.newHashMap();
        if(CollUtil.isNotEmpty(rResult.getData())){
            //根据日期分组
            groupByDate = rResult.getData().stream().collect(Collectors.groupingBy(ProducePassRateVO::getDate,CollectorsUtil.summingBigDecimal(ProducePassRateVO::getPassRate)));
            for(Map.Entry<LocalDate,BigDecimal> entry:groupByDate.entrySet()){
                QtChartPieValueVO<String> pieValue = new QtChartPieValueVO<>();
                pieValue.setName(entry.getKey().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                pieValue.setValue(new DecimalFormat("0.00%").format(entry.getValue()));
                data.add(pieValue);
            }
        }
        vo.setData(data);
        return vo;
    }

    /**
     * 计算最晚数据完整日期
     * @return
     */
    public LocalDateTime calculateOrder(LocalDateTime now){
        //计算最晚数据完整日期 在区间 (当前日期（T）的8:00:00, 次日（T+1）的8:00:00]内访问系统，最晚数据完整日期为T-1
        LocalDateTime classTimeStart = now.withHour(8).withMinute(0).withSecond(0);
        LocalDateTime classTimeEnd = now.withHour(8).withMinute(0).withSecond(0).plusDays(1);
        //最晚数据完整日期
        LocalDateTime fullDate = now;
        if(now.isAfter(classTimeStart) && now.isBefore(classTimeEnd)){
            fullDate = now.minusDays(1);
        }
        return fullDate;
    }

}
