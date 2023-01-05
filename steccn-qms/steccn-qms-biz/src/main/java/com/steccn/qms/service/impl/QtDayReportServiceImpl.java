package com.steccn.qms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.CalculateDateUtil;
import com.steccn.common.vo.QtTaskClassCalculate;
import com.steccn.data.feign.RemoteDataPlatformFeignApi;
import com.steccn.data.vo.DwDefectCollectVO;
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
import com.steccn.qms.vo.QtCheckTaskDetailReportVO;
import com.steccn.qms.vo.QtCheckTaskListVO;
import com.steccn.qms.vo.QtDayReportFirstVO;
import com.steccn.qms.vo.QtDayReportOnlineVO;
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
    private final RemoteSysConfigurationFeignApi remoteSysConfigurationFeignApi;
    private final RemoteDataPlatformFeignApi remoteDataPlatformFeignApi;
    private final QtCheckTaskService qtCheckTaskService;
    private final QtDefectiveItemService qtDefectiveItemService;


    private final QtCheckTaskMapper qtCheckTaskMapper;
    private final QtCheckTaskDetailService qtCheckTaskDetailService;
    private final QtCheckItemService qtCheckItemService;

    @Override
    public QtDayReportOnlineVO online(LocalDate date, Integer productLineId) {
        //查询人工数据
        //获取所有扁平化数据
        QtChartParamDTO dto = new QtChartParamDTO();
        dto.setDateStart(date);
        dto.setDateEnd(date);
        dto.setProductLineId(productLineId);
        List<QtCheckTaskDetailReportVO> manDataList = qtDefectiveReportService.defectiveDataFlat(dto);
        //查询检验设备数据
        Map<String, String> refMap = FeignOptional.of(() -> remoteSysConfigurationFeignApi.sysConfRef(new TupleDTO<>(SysOfficeConstant.HBBH_DEFECTIVE_SAMPLE_LINE, Sets.newHashSet(productLineId))), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        if (CollUtil.isNotEmpty(refMap)) {
            //获取到所有的映射产线
            Set<String> lineSet = refMap.values().stream().collect(Collectors.toSet());
            List<DwDefectCollectVO> collectList = FeignOptional.of(() -> remoteDataPlatformFeignApi.collectDetail(new TripleDTO<>(LocalDateTime.of(date, LocalTime.of(8, 0, 0)), LocalDateTime.of(date.plusDays(1), LocalTime.of(8, 0, 0)), lineSet)), FeignConstant.STECCN_DATA_PLATFORM_SERVICE_NAME).get();
            if(CollUtil.isNotEmpty(collectList)){
                List<QtCheckTaskDetailReportVO> qtCheckTaskDetailReportVOS = convertDefectCollect(collectList, productLineId);
            }
        }
        return null;
    }

    /**
     * 计算机器采集的缺陷
     *
     * @param collectList 采集缺陷集合
     * @param productLineId 产线id
     * @return list
     */
    private List<QtCheckTaskDetailReportVO> convertDefectCollect(List<DwDefectCollectVO> collectList, Integer productLineId) {
        List<QtCheckTaskDetailReportVO> resList = Lists.newArrayList();

        if (CollUtil.isNotEmpty(collectList) && Objects.nonNull(productLineId)) {
            Set<String> defectTypeSet = collectList.stream().map(DwDefectCollectVO::getDefectType).collect(Collectors.toSet());
            Map<String, QtDefectiveItem> resMap = qtDefectiveItemService.collectDefect(defectTypeSet);

            collectList.forEach(ele -> {
                QtCheckTaskDetailReportVO vo = new QtCheckTaskDetailReportVO();
                vo.setFinishedTime(ele.getProductTime());
                LocalDate date = ele.getProductTime().toLocalDate();
                LocalTime time = ele.getProductTime().toLocalTime();
                //计算缺陷记录时段
                if (time.getMinute() == 0 && time.getSecond() == 0) {
                    vo.setDefectStartTime(LocalDateTime.of(date, time));
                    vo.setDefectEndTime(LocalDateTime.of(date, time.plusHours(1)));
                } else {
                    //time
                    vo.setDefectStartTime(LocalDateTime.of(date, LocalTime.of(time.getHour(), 0, 0)));
                    vo.setDefectEndTime(LocalDateTime.of(date, LocalTime.of(time.plusHours(1).getHour(), 0, 0)));
                }
                vo.setProductLineId(productLineId);
                QtTaskClassCalculate qtTaskClassCalculate = CalculateDateUtil.calculateClassTime(vo.getDefectEndTime());
                vo.setClassTime(qtTaskClassCalculate.getClassDate());
                vo.setClassId(qtTaskClassCalculate.getClassType());
                vo.setDefectNum(ele.getDefectCount());
                //配置缺陷项

                resList.add(vo);
            });
        }
        return resList;
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
            return null;
        }
        taskList.forEach(tmp->{
            taskIdList.add(tmp.getId());
        });
        //任务详情表
        List<QtCheckTaskDetail> taskDetailList = qtCheckTaskDetailService.list(new LambdaQueryWrapper<QtCheckTaskDetail>().in(QtCheckTaskDetail::getTaskId,taskIdList));
        if(CollUtil.isEmpty(taskDetailList)){
            return null;
        }
        //查到分类数据和检验项数据
        List<Integer> itemIdList = Lists.newArrayList();
        taskDetailList.forEach(tmp->{
            itemIdList.add(tmp.getItemId());
        });

        return null;
    }
}
