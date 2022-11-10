package com.steccn.qms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.QtCheckItem;
import com.steccn.qms.domain.QtCheckTask;
import com.steccn.qms.domain.QtCheckTaskDetail;
import com.steccn.qms.domain.QtMaterial;
import com.steccn.qms.dto.QtChartParamDTO;
import com.steccn.qms.dto.QtChartSubDetailDTO;
import com.steccn.qms.service.*;
import com.steccn.qms.vo.QtChartBarVO;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.formula.functions.Count;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    /**
     * 生产基片损耗数据
     * @param dto
     * @return
     */
    @Override
    public List<QtChartSubDetailDTO> generateSubstrateLoss(QtChartParamDTO dto) {
        List<QtChartSubDetailDTO> result = new ArrayList<>();
        //转时间戳
        LocalDateTime startTime = dto.getDateStart().atTime(0,0,1);
        LocalDateTime endTime = dto.getDateEnd().atTime(23, 59,59);
        //准备数据
        //1、先根据已知条件查询任务表
        List<Integer> taskTypeList = new ArrayList<>();
        taskTypeList.add(QtManagerConstant.QtCheckTaskTypeEnum.CHEMICAL.getCode());
        taskTypeList.add(QtManagerConstant.QtCheckTaskTypeEnum.FIRST.getCode());
        taskTypeList.add(QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode());
        taskTypeList.add(QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getCode());
        LambdaQueryWrapper<QtCheckTask> taskCondition = new LambdaQueryWrapper<QtCheckTask>()
                //任务完成
                .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED)
                //完成时间范围
                .between(QtCheckTask::getFinishedTime,startTime,endTime)
                //班次
                .eq(dto.getWorkOrderId()!=null,QtCheckTask::getClassesId,dto.getWorkOrderId())
                //只有化验抽检、首检、在线检、巡检有车间
                .in(QtCheckTask::getTaskType,taskTypeList)
                //车间
                .apply(dto.getProductWorkShopId() != null, "JSON_CONTAINS(product_workshop, JSON_OBJECT('id', {0}))", dto.getProductWorkShopId())
                //产线
                .apply(dto.getProductLineId() != null, "JSON_CONTAINS(product_line, JSON_OBJECT('id', {0}))", dto.getProductLineId())
                .orderByAsc(QtCheckTask::getFinishedTime);
        List<QtCheckTask> taskList = qtCheckTaskService.list(taskCondition);
        if(CollUtil.isNotEmpty(taskList)){
            for(QtCheckTask tmp:taskList){
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyyMMdd");
                tmp.setDate(df.format(tmp.getFinishedTime()));
                tmp.setProductLineId(tmp.getProductLine().getId());
            }
            //根据日期进行分组
            Map<String,List<QtCheckTask>> groupByDate = taskList.stream().collect(Collectors.groupingBy(QtCheckTask::getDate));
            //遍历map
            for(Map.Entry<String,List<QtCheckTask>> entry:groupByDate.entrySet()){
                List<QtCheckTask> checkTaskDateList = entry.getValue();
                //再根据班组进行分组
                Map<Integer,List<QtCheckTask>> groupByWorkOrder = checkTaskDateList.stream().collect(Collectors.groupingBy(QtCheckTask::getClassesId));
                //遍历班组分组
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
                    BigDecimal totalBaiItem = new BigDecimal(0);
                    BigDecimal totalBaiItemCount = new BigDecimal(0);
                    BigDecimal totalDanItem = new BigDecimal(0);
                    BigDecimal totalDanItemCount = new BigDecimal(0);
                    BigDecimal totalLaItem = new BigDecimal(0);
                    BigDecimal totalLaItemCount = new BigDecimal(0);
                    //根据产线进行分组
                    Map<Integer, List<QtCheckTask>> groupByProductLine = checkTaskWorkOrderList.stream().collect(Collectors.groupingBy(QtCheckTask::getProductLineId));
                    //遍历产线
                    for(Map.Entry<Integer,List<QtCheckTask>> entry3:groupByProductLine.entrySet()){
                        //准备产线下taskId
                        List<QtCheckTask> checkTaskProductLineList = entry3.getValue();
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
                    }
                    //平均单片掰边重量：产线A在班次日期D的班次b时间段内（例如2021-09-21 8:00:00 ~ 20:00:00）所有巡检任务录入的“单片掰边重量”（需提前配置好“单片掰边重量”检验项）取平均值，单位为kg
                    BigDecimal avgBaiItem = new BigDecimal(0);
                    if(totalBaiItem.intValue()!=0){
                        avgBaiItem = totalBaiItem.divide(totalBaiItemCount);
                    }
                    //平均单片重量：产线A在班次日期D的班次b时间段内（例如2021-09-21 8:00:00 ~ 20:00:00）所有巡检任务录入的“单片重量”（需提前配置好“单片重量”检验项）取平均值，单位为kg
                    BigDecimal avgDanItem = new BigDecimal(0);
                    if(totalDanItem.intValue()!=0){
                        avgDanItem = totalDanItem.divide(totalDanItemCount);
                    }
                    //拉引时速：产线A在班次日期D的班次b时间段内（例如2021-09-21 8:00:00 ~ 20:00:00）所有巡检任务录入的“拉引速度”（需提前配置好“拉引速度”检验项）取平均值，单位为m/h
                    BigDecimal avgLaItem = new BigDecimal(0);
                    if(totalDanItem.intValue()!=0){
                        avgLaItem = totalLaItem.divide(totalLaItemCount);
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
                    //拉引量 = (平均单片掰边重量 + 平均单片重量) * （拉引时速 / 玻璃片长度) * 12 / 1000000
                }

            }
        }else {
            return result;
        }
        return null;
    }
}
