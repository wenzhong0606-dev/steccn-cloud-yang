package com.steccn.qms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.CalculateDateUtil;
import com.steccn.common.vo.QtTaskClassCalculate;
import com.steccn.data.feign.RemoteDataPlatformFeignApi;
import com.steccn.data.vo.DwDefectCollectVO;
import com.steccn.pms.vo.BaseReportHeaderVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.*;
import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.dto.TupleDTO;
import com.steccn.platform.constant.SysOfficeConstant;
import com.steccn.platform.feign.RemoteSysConfigurationFeignApi;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.QtCheckTask;
import com.steccn.qms.domain.QtCheckTaskDetail;
import com.steccn.qms.domain.QtDefectiveItem;
import com.steccn.qms.domain.QtMaterial;
import com.steccn.qms.dto.QtChartParamDTO;
import com.steccn.qms.mapper.QtCheckTaskMapper;
import com.steccn.qms.service.*;
import com.steccn.qms.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @desc 质量日报表-接口实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QtDayReportServiceImpl implements QtDayReportService {

    private final QtDefectiveReportService qtDefectiveReportService;

    private final QtCheckTaskService qtCheckTaskService;
    private final QtDefectiveItemService qtDefectiveItemService;


    private final QtCheckTaskMapper qtCheckTaskMapper;
    private final QtCheckTaskDetailService qtCheckTaskDetailService;
    private final QtCheckItemService qtCheckItemService;
    private final QtItemCategoryService qtItemCategoryService;

    @Override
    public QtDayReportOnlineVO online(LocalDate date, Integer productLineId) {
        //查询人工数据
        //获取所有扁平化数据
        QtChartParamDTO dto = new QtChartParamDTO();
        dto.setDateStart(date);
        dto.setDateEnd(date);
        dto.setProductLineId(productLineId);
        //获取人工采集的缺陷集合
        List<QtCheckTaskDetailReportVO> manDataList = qtDefectiveReportService.defectiveDataFlat(dto);
        //获取机器采集的缺陷集合
        dto.setCheckType(QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode());
        List<QtCheckTaskDetailReportVO> collectDataList = qtDefectiveReportService.defectCollect(dto);
        manDataList.addAll(collectDataList);
        SteccnTableAssertUtil.notEmpty(manDataList, "数据为空");
        log.info("{}", manDataList);
        return null;
    }

    @Override
    public QtDayReportOnlineVO polling(LocalDate date, Integer productLineId) {
        return null;
    }

    @Override
    public QtDayReportFirstVO first(LocalDate date, Integer productLineId) {
        LocalDateTime startTime = date.atTime(8,0,0,0);
        LocalDateTime endTime = date.plusDays(1).atTime(7,59,59,99999999);
        //关联查询
        MPJLambdaWrapper<QtCheckTask> taskCondition = new MPJLambdaWrapper<QtCheckTask>()
                .select(QtCheckTask::getId,QtCheckTask::getType,QtCheckTask::getMaterialId,QtCheckTask::getMaterialWorkshop,
                        QtCheckTask::getProductLine,QtCheckTask::getProductOrder,QtCheckTask::getProductBatch,
                        QtCheckTask::getCheckPlanId,QtCheckTask::getStatus,QtCheckTask::getCheckNum,QtCheckTask::getGoodNum,
                        QtCheckTask::getBadNum,QtCheckTask::getResult,QtCheckTask::getNote,QtCheckTask::getCreateBy,QtCheckTask::getCreateName,
                        QtCheckTask::getCreateTime,QtCheckTask::getUpdateBy,QtCheckTask::getUpdateName,QtCheckTask::getUpdateTime,
                        QtCheckTask::getDelFlag,QtCheckTask::getStartTime,QtCheckTask::getFinishedTime,QtCheckTask::getArrivalTime,
                        QtCheckTask::getDeliverTime,QtCheckTask::getSupplier,QtCheckTask::getCustomer,QtCheckTask::getDefectStartTime,
                        QtCheckTask::getDefectEndTime,QtCheckTask::getDeliverOrder,QtCheckTask::getTaskType,QtCheckTask::getDeliverNum,
                        QtCheckTask::getCode,QtCheckTask::getProductWorkshop,QtCheckTask::getStartBy,QtCheckTask::getStartName,
                        QtCheckTask::getCancelBy,QtCheckTask::getCancelName,QtCheckTask::getFinishedBy,QtCheckTask::getFinishedName,
                        QtCheckTask::getCancelTime,QtCheckTask::getSendOrder,QtCheckTask::getDefectNum,QtCheckTask::getClassesId,QtCheckTask::getStationId,
                        QtCheckTask::getClassTime,QtCheckTask::getSpotTime)
                //关联物料表
                .selectAs(QtMaterial::getName, QtCheckTaskListVO::getMaterialName)
                //json映射
                .selectAs(QtCheckTask::getProductLineName, QtCheckTaskListVO::getProductLineName)
                //关联
                .leftJoin(QtMaterial.class, QtMaterial::getId, QtCheckTask::getMaterialId)
                .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())
                .eq(QtCheckTask::getTaskType, QtManagerConstant.QtCheckTaskTypeEnum.FIRST.getCode())
                .eq(QtCheckTask::getTaskType,QtManagerConstant.QtCheckTaskTypeEnum.FIRST.getCode())
                .between(QtCheckTask::getFinishedTime,startTime,endTime)
                //产线
                .apply(productLineId != null, "JSON_CONTAINS(product_line, JSON_OBJECT('id', {0}))", productLineId);
        List<QtCheckTaskListVO> taskList = qtCheckTaskMapper.selectJoinList(QtCheckTaskListVO.class,taskCondition);
        List<Long> taskIdList = Lists.newArrayList();
        if(CollUtil.isEmpty(taskList)){
            log.info("任务为空(质量日报表-首检)");
            return null;
        }
        taskList.forEach(tmp->{
            taskIdList.add(tmp.getId());
        });
        //任务详情表
        List<QtCheckTaskDetail> taskDetailList = qtCheckTaskDetailService.list(new LambdaQueryWrapper<QtCheckTaskDetail>().in(QtCheckTaskDetail::getTaskId,taskIdList));
        if(CollUtil.isEmpty(taskDetailList)){
            log.info("任务详情为空(质量日报表-首检)");
            return null;
        }
        //查到分类数据和检验项数据
        List<Integer> itemIdList = Lists.newArrayList();
        taskDetailList.forEach(tmp->{
            itemIdList.add(tmp.getItemId());
        });
        //检验项按照创建时间降序排列
        List<QtCheckItem> itemList = qtCheckItemService.list(new LambdaQueryWrapper<QtCheckItem>()
                .in(QtCheckItem::getId,itemIdList)
                .orderByDesc(QtCheckItem::getCreateTime));
        //相同code只留一个
        itemList = itemList.stream().collect(Collectors.collectingAndThen(
                Collectors.toCollection(() -> new TreeSet<>(
                        Comparator.comparing(QtCheckItem::getCode))), ArrayList::new));
        if(CollUtil.isEmpty(itemList)){
            log.info("点检项为空(质量日报表-首检)");
            return null;
        }
        List<Integer> categoryIdList = Lists.newArrayList();
        itemList.forEach(tmp -> {
            categoryIdList.add(tmp.getCategoryId());
        });
        //根据创建时间排序
        List<QtItemCategory> qtItemCategoryList = qtItemCategoryService.list(new LambdaQueryWrapper<QtItemCategory>()
                .in(QtItemCategory::getId,itemIdList)
                .orderByAsc(QtItemCategory::getCreateTime));
        QtDayReportFirstVO result = new QtDayReportFirstVO();
        //静态表头
        List<QtSpcHeadTitleVO> common = Lists.newArrayList();
        QtSpcHeadTitleVO staticHead1 = new QtSpcHeadTitleVO();
        staticHead1.setLabel(QtManagerConstant.SPC_HEADER_FIELD_PRODUCT_LINE);
        staticHead1.setValue(QtManagerConstant.SPC_HEADER_FIELD_PRODUCT_LINE_VALUE);
        common.add(staticHead1);
        QtSpcHeadTitleVO staticHead2 = new QtSpcHeadTitleVO();
        staticHead2.setLabel(QtManagerConstant.DAY_REPORT_FIELD_ORDER);
        staticHead2.setValue(QtManagerConstant.DAY_REPORT_FIELD_ORDER_VALUE);
        common.add(staticHead2);
        QtSpcHeadTitleVO staticHead3 = new QtSpcHeadTitleVO();
        staticHead3.setLabel(QtManagerConstant.SPC_HEADER_FIELD_FINISHED_TIME);
        staticHead3.setValue(QtManagerConstant.SPC_HEADER_FIELD_FINISHED_TIME_VALUE);
        common.add(staticHead3);
        QtSpcHeadTitleVO staticHead4 = new QtSpcHeadTitleVO();
        staticHead4.setLabel(QtManagerConstant.SPC_HEADER_FIELD_MATERIAL_NAME);
        staticHead4.setValue(QtManagerConstant.SPC_HEADER_FIELD_MATERIAL_NAME_VALUE);
        common.add(staticHead4);
        QtSpcHeadTitleVO staticHead5 = new QtSpcHeadTitleVO();
        staticHead5.setLabel(QtManagerConstant.DAY_REPORT_FIELD_RESULT);
        staticHead5.setValue(QtManagerConstant.DAY_REPORT_FIELD_RESULT_VALUE);
        common.add(staticHead5);
        QtSpcHeadTitleVO staticHead6 = new QtSpcHeadTitleVO();
        staticHead6.setLabel(QtManagerConstant.DAY_REPORT_FIELD_NOTE);
        staticHead6.setValue(QtManagerConstant.DAY_REPORT_FIELD_NOTE_VALUE);
        common.add(staticHead6);
        QtSpcHeadTitleVO staticHead7 = new QtSpcHeadTitleVO();
        staticHead7.setLabel(QtManagerConstant.DAY_REPORT_FIELD_COPY);
        staticHead7.setValue(QtManagerConstant.DAY_REPORT_FIELD_COPY_VALUE);
        common.add(staticHead7);
        result.setCommon(common);
        //动态表头
        //把itemList根据category分组
        List<BaseReportHeaderVO> module = Lists.newArrayList();
        Map<Integer,List<QtCheckItem>> groupByCategory = itemList.stream().collect(Collectors.groupingBy(v -> v.getCategoryId()));
        for(int i = 0; i < qtItemCategoryList.size(); i++){
            BaseReportHeaderVO dynamicHeader = new BaseReportHeaderVO();
            if(!groupByCategory.containsKey(qtItemCategoryList.get(i).getId())){
                break;
            }
            dynamicHeader.setLabel(qtItemCategoryList.get(i).getName());
            List<QtCheckItem> itemListTmp = groupByCategory.get(qtItemCategoryList.get(i).getId());
            //对item值根据创建时间升序排列
            itemListTmp = itemListTmp.stream().sorted(Comparator.comparing(QtCheckItem::getCreateTime)).collect(Collectors.toList());
            List<String> value = Lists.newArrayList();
            itemListTmp.forEach(tmp -> {
                value.add(tmp.getName());
            });
            dynamicHeader.setValue(value);
            module.add(dynamicHeader);
        }
        result.setModule(module);
        //设置数据
        List<QtDayReportFirstDataVO> tableData = Lists.newArrayList();
        //根据taskId分组
        Map<Long,List<QtCheckTaskDetail>> groupByTaskId = taskDetailList.stream().collect(Collectors.groupingBy(v -> v.getTaskId()));
        for(Map.Entry<Long,List<QtCheckTaskDetail>> taskEntry:groupByTaskId.entrySet()){
            //获取task
            QtCheckTaskListVO task = taskList.stream().filter(v -> v.getId().equals(taskEntry.getKey())).findFirst().get();
            List<QtCheckTaskDetail> detailTmp = taskEntry.getValue();
            //一个任务下,根据copy分组
            Map<Integer,List<QtCheckTaskDetail>> groupByCopy = detailTmp.stream().collect(Collectors.groupingBy(v -> v.getCopy()));
            for(Map.Entry<Integer,List<QtCheckTaskDetail>> copyEntry:groupByCopy.entrySet()){
                List<QtCheckTaskDetail> copyDetailTmp = copyEntry.getValue();
                //根据序号排列
                copyDetailTmp = copyDetailTmp.stream().sorted(Comparator.comparing(QtCheckTaskDetail::getCopy)).collect(Collectors.toList());
                for(QtCheckTaskDetail copyTmp:copyDetailTmp){
                    //组合数据
                    QtDayReportFirstDataVO column = new QtDayReportFirstDataVO();
                    //具体data
                    column.setId(taskEntry.getKey());
                    column.setMaterialId(task.getMaterialId());
                    column.setMaterialName(task.getMaterialName());
                    column.setFinishedTime(task.getFinishedTime());
                    column.setResult(task.getResult());
                    column.setCopy(copyTmp.getCopy());
                    column.setNote(task.getNote());
                    column.setClassOrder(task.getClasses());
                    column.setProductLine(task.getProductLineName());
                }
            }
        }
        return result;
    }
}
