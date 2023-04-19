package com.steccn.qms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.CalculateDateUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.util.MapUtil;
import com.steccn.common.vo.QtTaskClassCalculate;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.pms.vo.BaseReportHeaderVO;
import com.steccn.pms.vo.FurtherValueStrVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.*;
import com.steccn.qms.dto.QtChartParamDTO;
import com.steccn.qms.dto.QtDaySamplingDTO;
import com.steccn.qms.mapper.QtCheckTaskMapper;
import com.steccn.qms.mapper.QtDefectiveItemMapper;
import com.steccn.qms.service.*;
import com.steccn.qms.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
    private final RemoteSpareFeignApi remoteSpareFeignApi;
    private final RemoteOfficeFeign remoteOfficeFeign;
    private final QtCheckPlanGroupItemService qtCheckPlanGroupItemService;

    private final QtCheckTaskService qtCheckTaskService;
    private final QtDefectiveItemService qtDefectiveItemService;
    private final QtDefectiveItemMapper qtDefectiveItemMapper;
    private final QtCheckTaskDestoryInfoService qtCheckTaskDestoryInfoService;


    private final QtCheckTaskMapper qtCheckTaskMapper;
    private final QtCheckTaskDetailService qtCheckTaskDetailService;
    private final QtCheckItemService qtCheckItemService;
    private final QtItemCategoryService qtItemCategoryService;

    @Override
    public QtDayReportOnlineVO online(LocalDate date, Integer productLineId) {
        QtDayReportOnlineVO resVo = new QtDayReportOnlineVO();
        List<QtDayReportOnlineDataVO> tableData = Lists.newArrayList();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("HH:mm");
        //查询人工数据
        //获取所有扁平化数据
        QtChartParamDTO dto = new QtChartParamDTO();
        dto.setDateStart(date);
        dto.setDateEnd(date);
        dto.setProductLineId(productLineId);
        //获取人工采集的缺陷集合+获取机器采集的缺陷集合
        dto.setCheckType(QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode());
        List<QtCheckTaskDetailReportVO> manData = qtDefectiveReportService.defectiveDataFlat(dto);
        List<QtCheckTaskDetailReportVO> manDataList = manData;
        SteccnTableAssertUtil.notEmpty(manDataList, "数据为空");
        //查询物料
        Set<Long> materialIdSet = manDataList.stream().map(QtCheckTaskDetailReportVO::getMaterialId).collect(Collectors.toSet());
        Map<Long, String> materialIdNameMap = FeignOptional.of(() -> remoteSpareFeignApi.listByIdCh(materialIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Map<Integer, String> sysOfficeMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(Sets.newHashSet(dto.getProductLineId())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

        //做缺陷类别+缺陷分组
        Map<Integer, Set<Integer>> categoryDefectMap = Maps.newHashMap();
        manDataList.forEach(ele -> MapUtil.computeSet(categoryDefectMap, ele.getDefectCategory(), ele.getDefectId()));
        //构建module
        List<BaseReportHeaderVO> resModule = moduleHead(categoryDefectMap);

        //根据来源分组
        Map<Integer, BigDecimal> countMap = Maps.newHashMap();
        Map<Integer, List<QtCheckTaskDetailReportVO>> classMap = manDataList.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getClassId));

        String productLineName = sysOfficeMap.get(dto.getProductLineId());

        for (Map.Entry<Integer, List<QtCheckTaskDetailReportVO>> classEntry : classMap.entrySet()) {
            //根据班次分组
            Map<Integer, BigDecimal> classCountMap = Maps.newHashMap();
            Map<Integer, List<QtCheckTaskDetailReportVO>> dataSourceMap = classEntry.getValue().stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getDataSource));
            for (Map.Entry<Integer, List<QtCheckTaskDetailReportVO>> dataSourceEntry : dataSourceMap.entrySet()) {
                //根据缺陷记录时段分组
                Map<String, List<QtCheckTaskDetailReportVO>> defectTimeMap = dataSourceEntry.getValue().stream().collect(Collectors.groupingBy(v -> v.getDefectStartTime() + "~" + v.getDefectEndTime()));
                for (Map.Entry<String, List<QtCheckTaskDetailReportVO>> defectTimeEntry : defectTimeMap.entrySet()) {
                    //根据物料分组
                    Map<Long, List<QtCheckTaskDetailReportVO>> taskMap = defectTimeEntry.getValue().stream().collect(Collectors.groupingBy(v -> Objects.nonNull(v.getTaskId()) ? v.getTaskId() : -1L));
                    for (Map.Entry<Long, List<QtCheckTaskDetailReportVO>> taskEntry : taskMap.entrySet()) {
                        QtDayReportOnlineDataVO vo = new QtDayReportOnlineDataVO();
                        vo.setDataSource(dataSourceEntry.getKey());
                        vo.setDataSourceStr(QtManagerConstant.QtDefectDataSource.code(dataSourceEntry.getKey()).getName());
                        vo.setClassId(classEntry.getKey());
                        vo.setClassName(QtManagerConstant.QtClassesEnum.code(classEntry.getKey()).getName());
                        String[] split = defectTimeEntry.getKey().split("~");
                        vo.setDefectStartTime(LocalDateTime.parse(split[0]));
                        vo.setDefectEndTime(LocalDateTime.parse(split[1]));
                        vo.setDefectTimeStr(vo.getDefectStartTime().format(df) + "~" + vo.getDefectEndTime().format(df));
                        vo.setType(0);
                        vo.setTaskId(-1 == taskEntry.getKey() ? null : taskEntry.getKey());
                        QtCheckTaskDetailReportVO qtCheckTask = taskEntry.getValue().get(0);
                        vo.setMaterialId(qtCheckTask.getMaterialId());
                        vo.setNote(qtCheckTask.getNote());
                        vo.setMaterialName(materialIdNameMap.get(vo.getMaterialId()));
                        vo.setProductLineName(productLineName);
                        tableData.add(vo);
                        List<FurtherValueStrVO> module = Lists.newArrayList();
                        vo.setModule(module);
                        //遍历类别
                        for (Map.Entry<Integer, Set<Integer>> categoryDefectEntry : categoryDefectMap.entrySet()) {
                            FurtherValueStrVO strVO = new FurtherValueStrVO();
                            List<String> value = Lists.newArrayList();
                            for (Integer defectId : categoryDefectEntry.getValue()) {
                                //获取该缺陷下的所有数据
                                List<QtCheckTaskDetailReportVO> defectDataList = taskEntry.getValue().stream().filter(v -> v.getDefectId().equals(defectId)).collect(Collectors.toList());
                                int sum = defectDataList.stream().filter(v -> Objects.nonNull(v.getDefectNum())).mapToInt(QtCheckTaskDetailReportVO::getDefectNum).sum();
                                BigDecimalUtil.accumulate(countMap, defectId, BigDecimal.valueOf(sum));
                                BigDecimalUtil.accumulate(classCountMap, defectId, BigDecimal.valueOf(sum));
                                value.add(String.valueOf(sum));
                            }
                            strVO.setValue(value);
                            module.add(strVO);
                        }
                    }
                }
            }
            QtDayReportOnlineDataVO vo = insertCount(1, categoryDefectMap, classEntry.getKey(), classCountMap, productLineName);
            tableData.add(vo);
        }
        QtDayReportOnlineDataVO allVo = insertCount(2, categoryDefectMap, 2, countMap, productLineName);
        tableData.add(allVo);
        //数据需要排序
        tableData.sort((o1, o2) -> {
            //先对比班次
            if (!o1.getClassId().equals(o2.getClassId())) {
                return o1.getClassId() - o2.getClassId();
            }
            //对比数据来源为空，则排序到前面
            if (Objects.isNull(o1.getDataSource())) {
                return 1;
            }
            //对比数据来源
            if (!o1.getDataSource().equals(o2.getDataSource())) {
                return o1.getDataSource() - o2.getDataSource();
            }
            //对比缺陷记录时段
            if (!o1.getDefectStartTime().equals(o2.getDefectStartTime())) {
                return o1.getDefectStartTime().compareTo(o2.getDefectStartTime());
            }
            return 1;
        });
        resVo.setTableData(tableData);
        QtDayReportTableHeadersVO header = new QtDayReportTableHeadersVO();
        resVo.setHeaders(header);
        header.setModule(resModule);
        header.setCommon(generateCommonHead());
        //落板原因
        resVo.setFallList(fallList(manData, productLineName));
        return resVo;
    }

    /**
     * 获取落板原因
     *
     * @param manDataList 人工检测数据
     * @return 落板原因集合
     */
    private List<QtCheckTaskDestoryOnlineVO> fallList(List<QtCheckTaskDetailReportVO> manDataList, String productLineName) {
        List<QtCheckTaskDestoryOnlineVO> fallList = Lists.newArrayList();
        if (CollUtil.isNotEmpty(manDataList)) {
            manDataList = manDataList.stream().filter(v -> Objects.nonNull(v.getTaskId())).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(manDataList)) {
                //先根据人工数据查询所有的taskId
                Map<Long, Integer> taskClassMap = Maps.newHashMap();
                manDataList.forEach(ele -> taskClassMap.put(ele.getTaskId(), ele.getClassId()));
                //查询所有的落板原因
                List<QtCheckTaskDestoryInfo> destroyList = qtCheckTaskDestoryInfoService.list(new LambdaQueryWrapper<QtCheckTaskDestoryInfo>()
                        .in(QtCheckTaskDestoryInfo::getTaskId, taskClassMap.keySet())
                        .eq(QtCheckTaskDestoryInfo::getDelFlag, MpConstant.NO));
                if (CollUtil.isNotEmpty(destroyList)) {
                    Map<Long, List<QtCheckTaskDestoryInfo>> collect = destroyList.stream().collect(Collectors.groupingBy(QtCheckTaskDestoryInfo::getTaskId));
                    for (Map.Entry<Long, List<QtCheckTaskDestoryInfo>> entry : collect.entrySet()) {
                        Optional<QtCheckTaskDestoryInfo> min = entry.getValue().stream().min(Comparator.comparing(QtCheckTaskDestoryInfo::getDestoryStartTime));
                        for (QtCheckTaskDestoryInfo destroy : entry.getValue()) {
                            QtCheckTaskDestoryOnlineVO vo = new QtCheckTaskDestoryOnlineVO();
                            BeanUtils.copyProperties(destroy, vo);
                            vo.setFirstTime(min.get().getDestoryStartTime());
                            vo.setClassId(taskClassMap.get(entry.getKey()));
                            QtManagerConstant.QtClassesEnum classEnum = QtManagerConstant.QtClassesEnum.code(vo.getClassId());
                            if (Objects.nonNull(classEnum)) {
                                vo.setClassName(classEnum.getName());
                            }
                            vo.setProductLineName(productLineName);
                            fallList.add(vo);
                        }
                    }
                    fallList.sort((o1, o2) -> {
                        //先对比班次-如果为空，放置到最后
                        if (Objects.nonNull(o1.getClassId()) && Objects.nonNull(o2.getClassId()) && !o1.getClassId().equals(o2.getClassId())) {
                            return o1.getClassId() - o2.getClassId();
                        }
                        //对比落板开始时间
                        if (!o1.getFirstTime().equals(o2.getFirstTime())) {
                            return o1.getFirstTime().compareTo(o2.getFirstTime());
                        }
                        //对比taskId
                        if (!o1.getTaskId().equals(o2.getTaskId())) {
                            return o1.getTaskId().compareTo(o2.getTaskId());
                        }
                        return 1;
                    });
                }
            }
        }
        return fallList;
    }


    /**
     * 插入单条合计
     *
     * @param type
     * @param categoryDefectMap
     * @param classId
     * @param classCountMap
     * @param productLineName
     * @return
     */
    private QtDayReportOnlineDataVO insertCount(Integer type, Map<Integer, Set<Integer>> categoryDefectMap, Integer classId, Map<Integer, BigDecimal> classCountMap, String productLineName) {
        //插入一条合计
        QtDayReportOnlineDataVO vo = new QtDayReportOnlineDataVO();
        if (type == 1) {
            vo.setProductLineName(productLineName);
        }
        vo.setType(type);
        List<FurtherValueStrVO> module = Lists.newArrayList();
        vo.setModule(module);
        vo.setClassId(classId);
        for (Map.Entry<Integer, Set<Integer>> categoryDefectEntry : categoryDefectMap.entrySet()) {
            FurtherValueStrVO strVO = new FurtherValueStrVO();
            List<String> value = Lists.newArrayList();
            for (Integer defectId : categoryDefectEntry.getValue()) {
                value.add(String.valueOf(classCountMap.get(defectId)));
            }
            strVO.setValue(value);
            module.add(strVO);
        }
        return vo;
    }

    /**
     * 生成静态表头
     *
     * @return list
     */
    private List<QtSpcHeadTitleVO> generateCommonHead() {
        List<QtSpcHeadTitleVO> commonHeadList = Lists.newArrayList();
        commonHeadList.add(new QtSpcHeadTitleVO("productLineName", "产线"));
        commonHeadList.add(new QtSpcHeadTitleVO("className", "班次"));
        commonHeadList.add(new QtSpcHeadTitleVO("dataSourceStr", "来源"));
        commonHeadList.add(new QtSpcHeadTitleVO("materialName", "物料名称"));
        commonHeadList.add(new QtSpcHeadTitleVO("defectTimeStr", "缺陷记录时段"));
        commonHeadList.add(new QtSpcHeadTitleVO("note", "备注"));
        return commonHeadList;
    }

    /**
     * 构建module
     *
     * @param categoryDefectMap 缺陷map
     * @return
     */
    private List<BaseReportHeaderVO> moduleHead(Map<Integer, Set<Integer>> categoryDefectMap) {
        //查询缺陷和缺陷类别名称
        Set<Integer> defectIdSet = Sets.newHashSet();
        categoryDefectMap.values().forEach(ele -> defectIdSet.addAll(ele));
        Map<Integer, String> defectIdNameMap = qtDefectiveItemService.listByIdCh(defectIdSet);
        Map<Integer, String> categoryIdNameMap = qtItemCategoryService.listByIdCh(categoryDefectMap.keySet());

        //构造module
        List<BaseReportHeaderVO> resModule = Lists.newArrayList();
        for (Map.Entry<Integer, Set<Integer>> categoryDefectEntry : categoryDefectMap.entrySet()) {
            List<String> value = Lists.newArrayList();
            BaseReportHeaderVO vo = new BaseReportHeaderVO();
            vo.setLabel(categoryIdNameMap.get(categoryDefectEntry.getKey()));
            for (Integer defectId : categoryDefectEntry.getValue()) {
                value.add(defectIdNameMap.get(defectId));
            }
            vo.setValue(value);
            resModule.add(vo);
        }
        return resModule;
    }


    @Override
    public QtDayReportFirstVO first(LocalDate date, Integer productLineId, Integer checkTaskType) {
        LocalDateTime startTime = LocalDateUtil.getMorning(date);
        LocalDateTime endTime = LocalDateUtil.getMorning(date.plusDays(1));
        //关联查询
        MPJLambdaWrapper<QtCheckTask> taskCondition = new MPJLambdaWrapper<QtCheckTask>()
                .select(QtCheckTask::getId, QtCheckTask::getType, QtCheckTask::getMaterialId, QtCheckTask::getMaterialWorkshop,
                        QtCheckTask::getProductLine, QtCheckTask::getProductOrder, QtCheckTask::getProductBatch,
                        QtCheckTask::getCheckPlanId, QtCheckTask::getStatus, QtCheckTask::getCheckNum, QtCheckTask::getGoodNum,
                        QtCheckTask::getBadNum, QtCheckTask::getResult, QtCheckTask::getNote, QtCheckTask::getCreateBy, QtCheckTask::getCreateName,
                        QtCheckTask::getCreateTime, QtCheckTask::getUpdateBy, QtCheckTask::getUpdateName, QtCheckTask::getUpdateTime,
                        QtCheckTask::getDelFlag, QtCheckTask::getStartTime, QtCheckTask::getFinishedTime, QtCheckTask::getArrivalTime,
                        QtCheckTask::getDeliverTime, QtCheckTask::getSupplier, QtCheckTask::getCustomer, QtCheckTask::getDefectStartTime,
                        QtCheckTask::getDefectEndTime, QtCheckTask::getDeliverOrder, QtCheckTask::getTaskType, QtCheckTask::getDeliverNum,
                        QtCheckTask::getCode, QtCheckTask::getProductWorkshop, QtCheckTask::getStartBy, QtCheckTask::getStartName,
                        QtCheckTask::getCancelBy, QtCheckTask::getCancelName, QtCheckTask::getFinishedBy, QtCheckTask::getFinishedName,
                        QtCheckTask::getCancelTime, QtCheckTask::getSendOrder, QtCheckTask::getDefectNum, QtCheckTask::getClassesId, QtCheckTask::getStationId,
                        QtCheckTask::getClassTime, QtCheckTask::getSpotTime, QtCheckTask::getStation)
                //关联物料表
                .selectAs(QtMaterial::getName, QtCheckTaskListVO::getMaterialName)
                //json映射
                .selectAs(QtCheckTask::getProductLineName, QtCheckTaskListVO::getProductLineName)
                //关联
                .leftJoin(QtMaterial.class, QtMaterial::getId, QtCheckTask::getMaterialId)
                .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())
                .eq(QtCheckTask::getTaskType, checkTaskType)
                //产线
                .apply(productLineId != null, "JSON_CONTAINS(product_line, JSON_OBJECT('id', {0}))", productLineId);

        if (QtManagerConstant.QtCheckTaskTypeEnum.FIRST.getCode().equals(checkTaskType)) {
            //大于等于开始   小于结束
            taskCondition.ge(QtCheckTask::getFinishedTime, startTime).lt(QtCheckTask::getFinishedTime, endTime);
        } else {
            //大于等于开始   小于结束
            taskCondition.ge(QtCheckTask::getSpotTime, startTime).lt(QtCheckTask::getSpotTime, endTime);
        }

        List<QtCheckTaskListVO> taskList = qtCheckTaskMapper.selectJoinList(QtCheckTaskListVO.class, taskCondition);
        List<QtCheckTaskDetailReportVO> manData = Lists.newArrayList();
        Set<Integer> officeIds = Sets.newHashSet();
        taskList.forEach(task -> {
            officeIds.add(task.getStationId());
            QtCheckTaskDetailReportVO qtCheckTaskDetailReportVO = new QtCheckTaskDetailReportVO();
            qtCheckTaskDetailReportVO.setClassId(task.getClassesId());
            qtCheckTaskDetailReportVO.setTaskId(task.getId());
            manData.add(qtCheckTaskDetailReportVO);
        });
        officeIds.add(productLineId);
        Map<Integer, String> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(officeIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        String productLineName = officeMap.get(productLineId);
        List<Long> taskIdList = Lists.newArrayList();
        if (CollUtil.isEmpty(taskList)) {
            log.info("任务为空(质量日报表-首检)");
            SteccnTableAssertUtil.notEmpty(taskList, "数据为空");
        }
        taskList.forEach(tmp -> {
            taskIdList.add(tmp.getId());
        });
        //任务详情表
        List<QtCheckTaskDetail> taskDetailList = qtCheckTaskDetailService.list(new LambdaQueryWrapper<QtCheckTaskDetail>().in(QtCheckTaskDetail::getTaskId, taskIdList));
        if (CollUtil.isEmpty(taskDetailList)) {
            log.info("任务详情为空(质量日报表-首检)");
            SteccnTableAssertUtil.notEmpty(taskDetailList, "数据为空");
        }
        //查到分类数据和检验项数据
        List<Integer> itemIdList = Lists.newArrayList();
        taskDetailList.forEach(tmp -> {
            itemIdList.add(tmp.getItemId());
        });
        //检验项按照创建时间降序排列
        List<QtCheckItem> itemList = qtCheckItemService.list(new LambdaQueryWrapper<QtCheckItem>()
                .in(QtCheckItem::getId, itemIdList)
                .orderByDesc(QtCheckItem::getCreateTime));
        if (CollUtil.isEmpty(itemList)) {
            log.info("点检项为空(质量日报表-首检)");
            SteccnTableAssertUtil.notEmpty(itemList, "数据为空");
        }
        //查询

        //存全量item
        List<QtCheckItem> itemFullList = itemList;
        //相同code只留一个
        itemList = itemList.stream().collect(Collectors.collectingAndThen(
                Collectors.toCollection(() -> new TreeSet<>(
                        Comparator.comparing(QtCheckItem::getCode))), ArrayList::new));
        List<Integer> categoryIdList = Lists.newArrayList();
        itemList.forEach(tmp -> {
            categoryIdList.add(tmp.getCategoryId());
        });
        //根据创建时间排序
        List<QtItemCategory> qtItemCategoryList = qtItemCategoryService.list(new LambdaQueryWrapper<QtItemCategory>()
                .in(QtItemCategory::getId, categoryIdList)
                .orderByAsc(QtItemCategory::getCreateTime));
        QtDayReportFirstVO result = new QtDayReportFirstVO();
        QtDayReportTableHeadersVO headersVO = new QtDayReportTableHeadersVO();
        //静态表头
        List<QtSpcHeadTitleVO> common = Lists.newArrayList();

        if (QtManagerConstant.QtCheckTaskTypeEnum.FIRST.getCode().equals(checkTaskType)) {
            //首检表头
            QtSpcHeadTitleVO staticHead1 = new QtSpcHeadTitleVO();
            staticHead1.setLabel(QtManagerConstant.QtChartCompareEnum.PRODUCTLINE.getName());
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
        } else {
            //巡检表头
            QtSpcHeadTitleVO staticHead1 = new QtSpcHeadTitleVO();
            staticHead1.setLabel(QtManagerConstant.QtChartCompareEnum.PRODUCTLINE.getName());
            staticHead1.setValue(QtManagerConstant.SPC_HEADER_FIELD_PRODUCT_LINE_VALUE);
            common.add(staticHead1);
            QtSpcHeadTitleVO staticHead2 = new QtSpcHeadTitleVO();
            staticHead2.setLabel(QtManagerConstant.DAY_REPORT_FIELD_ORDER);
            staticHead2.setValue(QtManagerConstant.DAY_REPORT_FIELD_ORDER_VALUE);
            common.add(staticHead2);
            QtSpcHeadTitleVO staticHead3 = new QtSpcHeadTitleVO();
            staticHead3.setLabel(QtManagerConstant.SPC_HEADER_FIELD_SPOT_TIME);
            staticHead3.setValue(QtManagerConstant.SPC_HEADER_FIELD_SPOT_TIME_VALUE);
            common.add(staticHead3);
            QtSpcHeadTitleVO staticHead4 = new QtSpcHeadTitleVO();
            staticHead4.setLabel(QtManagerConstant.SPC_HEADER_FIELD_STATION);
            staticHead4.setValue(QtManagerConstant.SPC_HEADER_FIELD_STATION_VALUE);
            common.add(staticHead4);
            QtSpcHeadTitleVO staticHead5 = new QtSpcHeadTitleVO();
            staticHead5.setLabel(QtManagerConstant.SPC_HEADER_FIELD_MATERIAL_NAME);
            staticHead5.setValue(QtManagerConstant.SPC_HEADER_FIELD_MATERIAL_NAME_VALUE);
            common.add(staticHead5);
            QtSpcHeadTitleVO staticHead6 = new QtSpcHeadTitleVO();
            staticHead6.setLabel(QtManagerConstant.DAY_REPORT_FIELD_RESULT);
            staticHead6.setValue(QtManagerConstant.DAY_REPORT_FIELD_RESULT_VALUE);
            common.add(staticHead6);
            QtSpcHeadTitleVO staticHead7 = new QtSpcHeadTitleVO();
            staticHead7.setLabel(QtManagerConstant.DAY_REPORT_FIELD_NOTE);
            staticHead7.setValue(QtManagerConstant.DAY_REPORT_FIELD_NOTE_VALUE);
            common.add(staticHead7);
            QtSpcHeadTitleVO staticHead8 = new QtSpcHeadTitleVO();
            staticHead8.setLabel(QtManagerConstant.DAY_REPORT_FIELD_COPY);
            staticHead8.setValue(QtManagerConstant.DAY_REPORT_FIELD_COPY_VALUE);
            common.add(staticHead8);
        }
        headersVO.setCommon(common);
        //动态表头
        //把itemList根据category分组
        List<BaseReportHeaderVO> module = Lists.newArrayList();
        Map<Integer, List<QtCheckItem>> groupByCategory = itemList.stream().collect(Collectors.groupingBy(v -> v.getCategoryId()));
        for (int i = 0; i < qtItemCategoryList.size(); i++) {
            BaseReportHeaderVO dynamicHeader = new BaseReportHeaderVO();
            if (!groupByCategory.containsKey(qtItemCategoryList.get(i).getId())) {
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
        headersVO.setModule(module);
        result.setHeaders(headersVO);
        //设置数据
        List<QtDayReportFirstDataVO> tableData = Lists.newArrayList();
        //根据taskId分组
        Map<Long, List<QtCheckTaskDetail>> groupByTaskId = taskDetailList.stream().collect(Collectors.groupingBy(v -> v.getTaskId()));
        //先根据班次排序再根据完成时间排序
        taskList = taskList.stream().sorted(Comparator.comparing(QtCheckTaskListVO::getClassesId)
                .thenComparing(QtCheckTaskListVO::getStationId, Comparator.nullsLast(Comparator.comparing(v -> v)))
                .thenComparing(QtCheckTaskListVO::getFinishedTime)).collect(Collectors.toList());
        //根据taskId重组map
        Map<Long, List<QtCheckTaskDetail>> groupByTaskIdOrder = new LinkedHashMap<>();
        taskList.forEach(tmp -> {
            groupByTaskIdOrder.put(tmp.getId(), groupByTaskId.get(tmp.getId()));
        });
        for (Map.Entry<Long, List<QtCheckTaskDetail>> taskEntry : groupByTaskIdOrder.entrySet()) {
            //获取task
            QtCheckTaskListVO task = taskList.stream().filter(v -> v.getId().equals(taskEntry.getKey())).findFirst().get();
            List<QtCheckTaskDetail> detailTmp = taskEntry.getValue();
            //避免空指针异常
            if (Objects.isNull(detailTmp)) {
                continue;
            }
            //一个任务下,根据copy分组
            Map<Integer, List<QtCheckTaskDetail>> groupByCopyTmp = detailTmp.stream().collect(Collectors.groupingBy(v -> v.getCopy()));
            //根据样本序号排序
            Map<Integer, List<QtCheckTaskDetail>> groupByCopy = new LinkedHashMap<>();
            groupByCopyTmp.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEachOrdered(x -> groupByCopy.put(x.getKey(), x.getValue()));
            for (Map.Entry<Integer, List<QtCheckTaskDetail>> copyEntry : groupByCopy.entrySet()) {
                List<QtCheckTaskDetail> copyDetailTmp = copyEntry.getValue();
                //组合数据
                QtDayReportFirstDataVO column = new QtDayReportFirstDataVO();
                //具体data
                column.setId(taskEntry.getKey());
                column.setMaterialId(task.getMaterialId());
                column.setMaterialName(task.getMaterialName());
                column.setFinishedTime(task.getFinishedTime());
                column.setSpotTime(task.getSpotTime());
                if (Objects.nonNull(task.getStationId())) {
                    column.setStation(officeMap.get(task.getStationId()));
                }
                column.setResult(task.getResult());
                column.setCopy(copyEntry.getKey());
                column.setNote(task.getNote());
                column.setClassName(QtManagerConstant.QtClassesEnum.code(task.getClassesId()).getName());
                column.setProductLine(task.getProductLineName());
                //动态data
                List<FurtherValueObjVO> moduleData = Lists.newArrayList();
                //找到动态表头
                for (BaseReportHeaderVO headModule : module) {
                    List<ColorValueVO> colorValueList = Lists.newArrayList();
                    for (String headValue : headModule.getValue()) {
                        //表头检验项code
                        String headItemCode = itemList.stream().filter(v -> v.getName().equals(headValue)).findFirst().get().getCode();
                        //查到全量数据中所有的检验项id
                        List<Integer> itemIds = itemFullList.stream().filter(v -> v.getCode().equals(headItemCode)).map(QtCheckItem::getId).collect(Collectors.toList());
                        //根据itemId过滤
                        QtCheckTaskDetail copyTmp = copyDetailTmp.stream().filter(v -> itemIds.contains(v.getItemId())).findFirst().orElse(null);
                        QtCheckItem itemTmp = new QtCheckItem();
                        if (Objects.isNull(copyTmp)) {
                            itemTmp.setCode("-");
                        } else {
                            itemTmp = itemFullList.stream().filter(v -> v.getId().equals(copyTmp.getItemId())).findFirst().get();
                        }
                        ColorValueVO colorValue = new ColorValueVO();
                        //不匹配则为-
                        if (headItemCode.equals(itemTmp.getCode())) {
                            String checkResult = copyTmp.getCheckResult();
                            //为空则显示-
                            if (StrUtil.isBlank(checkResult)) {
                                colorValue.setValue("");
                                colorValue.setColor(0);
                                colorValueList.add(colorValue);
                            } else if (checkResult.equals(QtManagerConstant.QT_TASK_GOOD)) {
                                colorValue.setValue(QtManagerConstant.QT_TASK_GOOD);
                                colorValue.setColor(0);
                                colorValueList.add(colorValue);
                            } else if (checkResult.equals(QtManagerConstant.QT_TASK_BAD)) {
                                colorValue.setValue(QtManagerConstant.QT_TASK_BAD);
                                colorValue.setColor(1);
                                colorValueList.add(colorValue);
                            } else {
                                try {
                                    //判断数值
                                    BigDecimal checkResultNum = new BigDecimal(checkResult);
                                    //查上下偏差
                                    QtCheckPlanGroupItem planGroupItem = qtCheckPlanGroupItemService.getOne(new LambdaQueryWrapper<QtCheckPlanGroupItem>()
                                            .eq(QtCheckPlanGroupItem::getGroupId, copyTmp.getGroupId())
                                            .eq(QtCheckPlanGroupItem::getCheckItemId, copyTmp.getItemId()), false);
                                    //定义一个flag,超过上下偏差则为false
                                    Boolean flag = true;
                                    BigDecimal stdValue = planGroupItem.getStandardValue();
                                    BigDecimal upValue = planGroupItem.getUpperValue();
                                    if (upValue != null) {
                                        BigDecimal maxValue = BigDecimalUtil.add(stdValue, upValue);
                                        //大于上偏差
                                        if (checkResultNum.compareTo(maxValue) == 1) {
                                            flag = false;
                                        }
                                    }
                                    BigDecimal downValue = planGroupItem.getLowerValue();
                                    if (downValue != null) {
                                        BigDecimal minValue = BigDecimalUtil.subtract(stdValue, downValue);
                                        //小于下偏差
                                        if (checkResultNum.compareTo(minValue) == -1) {
                                            flag = false;
                                        }
                                    }
                                    colorValue.setValue(checkResult);
                                    if (flag) {
                                        colorValue.setColor(0);
                                    } else {
                                        colorValue.setColor(1);
                                    }
                                    colorValueList.add(colorValue);
                                } catch (Exception e) {
                                    colorValue.setValue(checkResult);
                                    colorValue.setColor(0);
                                    colorValueList.add(colorValue);
                                }
                            }

                        } else {
                            colorValue.setValue("");
                            colorValue.setColor(0);
                            colorValueList.add(colorValue);
                        }
                    }
                    FurtherValueObjVO valueObjVO = new FurtherValueObjVO();
                    valueObjVO.setValue(colorValueList);
                    moduleData.add(valueObjVO);
                }
                column.setModule(moduleData);
                tableData.add(column);
            }
        }
        result.setTableData(tableData);
        //如果所有的工位都为空，则表头不需要工位那一列
        long count = tableData.stream().filter(v -> StrUtil.isNotBlank(v.getStation())).count();
        if (count == 0) {
            List<QtSpcHeadTitleVO> oldCommon = result.getHeaders().getCommon();
            List<QtSpcHeadTitleVO> newCommon = oldCommon.stream().filter(v -> !QtManagerConstant.SPC_HEADER_FIELD_STATION.equals(v.getLabel())).collect(Collectors.toList());
            result.getHeaders().setCommon(newCommon);
        }

        if (QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getCode().equals(checkTaskType)) {
            //落板原因
            result.setFallList(fallList(manData, productLineName));
            //缺陷表格
        }
        return result;
    }

    @Override
    public QtDayPollingFirstVO pollingDefectItem(LocalDate date, Integer productLineId) {
        QtDayPollingFirstVO qtDayPollingFirstVO = new QtDayPollingFirstVO();
        LocalDateTime startTime = LocalDateUtil.getMorning(date);
        LocalDateTime endTime = LocalDateUtil.getMorning(date.plusDays(1));
        LambdaQueryWrapper<QtCheckTask> eq = new LambdaQueryWrapper<QtCheckTask>()
                .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())
                .eq(QtCheckTask::getTaskType, QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getCode())
                //大于等于开始   小于结束
                .ge(QtCheckTask::getSpotTime, startTime)
                .lt(QtCheckTask::getSpotTime, endTime)
                //产线
                .apply(productLineId != null, "JSON_CONTAINS(product_line, JSON_OBJECT('id', {0}))", productLineId);
        List<QtCheckTask> list = qtCheckTaskService.list(eq);
        List<Long> taskIds = Lists.newArrayList();
        list.forEach(task -> taskIds.add(task.getId()));
        SteccnTableAssertUtil.notEmpty(list, "数据为空");
        List<QtDefectItemInfoVO> defectItemList = qtCheckTaskMapper.getDefectItemList(QtManagerConstant.DAY_REPORT_FIELD_APPARENT_CONDITION, taskIds);
        SteccnTableAssertUtil.notEmpty(defectItemList, "数据为空");
        // 根据缺陷项类别分组
        Map<String, List<QtDefectItemInfoVO>> map = defectItemList.stream().collect(Collectors.groupingBy(QtDefectItemInfoVO::getCategoryName));
        // 根据班次类别分组
        Map<Integer, List<QtDefectItemInfoVO>> classMap = defectItemList.stream().collect(Collectors.groupingBy(QtDefectItemInfoVO::getClassesId));
        QtDayReportTableHeadersVO qtDayReportTableHeadersVO = new QtDayReportTableHeadersVO();
        List<QtSpcHeadTitleVO> commonHeadList = Lists.newArrayList();
        commonHeadList.add(new QtSpcHeadTitleVO("productLineName", "产线"));
        commonHeadList.add(new QtSpcHeadTitleVO("className", "班次"));
        qtDayReportTableHeadersVO.setCommon(commonHeadList);
        //处理数据
        List<BaseReportHeaderVO> module = Lists.newArrayList();
        List<QtDayReportPollingDataVO> reportPollingList = Lists.newArrayList();

        Set<Integer> productLineIds = Sets.newHashSet();
        productLineIds.add(productLineId);
        Map<Integer, String> productLineMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(productLineIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //根据缺陷项分类处理表头数据
        for (Map.Entry<String, List<QtDefectItemInfoVO>> ItemEntry : map.entrySet()) {
            BaseReportHeaderVO baseReportHeaderVO = new BaseReportHeaderVO();
            baseReportHeaderVO.setLabel(ItemEntry.getKey());
            List<String> value = Lists.newArrayList();
            List<QtDefectItemInfoVO> collect = ItemEntry.getValue().stream().collect(Collectors.collectingAndThen(Collectors.toCollection(()
                    -> new TreeSet<>(Comparator.comparing(QtDefectItemInfoVO::getId))), ArrayList::new));
            for (QtDefectItemInfoVO qtDefectItemInfoVO : collect) {
                value.add(qtDefectItemInfoVO.getName());
            }
            baseReportHeaderVO.setValue(value);
            module.add(baseReportHeaderVO);
        }
        qtDayReportTableHeadersVO.setModule(module);
        //根据班次处理表格数据
        for (Map.Entry<Integer, List<QtDefectItemInfoVO>> classEntry : classMap.entrySet()) {
            QtDayReportPollingDataVO qtDayReportPollingDataVO = new QtDayReportPollingDataVO();
            qtDayReportPollingDataVO.setProductLineName(productLineMap.get(productLineId));
            qtDayReportPollingDataVO.setClassId(classEntry.getKey());
            List<FurtherValueStrVO> valueList = Lists.newArrayList();
            if (QtManagerConstant.QtClassesEnum.DAY.getCode().equals(classEntry.getKey())) {
                qtDayReportPollingDataVO.setClassName(QtManagerConstant.QtClassesEnum.DAY.getName());
            } else if (QtManagerConstant.QtClassesEnum.NIGHT.getCode().equals(classEntry.getKey())) {
                qtDayReportPollingDataVO.setClassName(QtManagerConstant.QtClassesEnum.NIGHT.getName());
            }
            qtDayReportPollingDataVO.setType(0);
            //根据类型处理数据对应表头数据
            for (Map.Entry<String, List<QtDefectItemInfoVO>> typeEntry : map.entrySet()) {
                FurtherValueStrVO furtherValueStrVO = new FurtherValueStrVO();
                List<String> value = Lists.newArrayList();
                List<QtDefectItemInfoVO> collect = typeEntry.getValue().stream().collect(Collectors.collectingAndThen(Collectors.toCollection(()
                        -> new TreeSet<>(Comparator.comparing(QtDefectItemInfoVO::getId))), ArrayList::new));
                for (QtDefectItemInfoVO qtDefectItemInfoVO : collect) {
                    value.add(String.valueOf(getDefectItemQuantity(qtDefectItemInfoVO.getId(), classEntry.getValue())));
                }
                furtherValueStrVO.setValue(value);
                valueList.add(furtherValueStrVO);
            }
            qtDayReportPollingDataVO.setModule(valueList);
            reportPollingList.add(qtDayReportPollingDataVO);
        }
        //处理合计数据
        QtDayReportPollingDataVO qtDayReportPollingDataVO = new QtDayReportPollingDataVO();
        qtDayReportPollingDataVO.setType(1);
        List<FurtherValueStrVO> totalList = Lists.newArrayList();
        //根据类型处理数据对应表头数据
        for (Map.Entry<String, List<QtDefectItemInfoVO>> typeEntry : map.entrySet()) {
            FurtherValueStrVO furtherValueStrVO = new FurtherValueStrVO();
            List<String> value = Lists.newArrayList();
            List<QtDefectItemInfoVO> collect = typeEntry.getValue().stream().collect(Collectors.collectingAndThen(Collectors.toCollection(()
                    -> new TreeSet<>(Comparator.comparing(QtDefectItemInfoVO::getId))), ArrayList::new));
            for (QtDefectItemInfoVO qtDefectItemInfoVO : collect) {
                value.add(String.valueOf(getDefectItemQuantity(qtDefectItemInfoVO.getId(), defectItemList)));
            }
            furtherValueStrVO.setValue(value);
            totalList.add(furtherValueStrVO);
        }
        qtDayReportPollingDataVO.setModule(totalList);
        reportPollingList.add(qtDayReportPollingDataVO);
        //表头
        qtDayPollingFirstVO.setFlawHeaders(qtDayReportTableHeadersVO);
        //表格数据
        qtDayPollingFirstVO.setFlawTableData(reportPollingList);
        return qtDayPollingFirstVO;
    }

    @Override
    public QtDaySamplingVO sampling(LocalDate date) {
        //1.获取原始数据
        List<QtDaySamplingDTO> sourceList = qtCheckTaskMapper.samplingList();
        //2.如果原始数据集合不为空，则继续执行
        SteccnTableAssertUtil.notEmpty(sourceList, "配料抽检原始数据集合为空");
        //3.获取早班和晚班时间
        LocalDateTime morning = LocalDateUtil.getMorning(date);
        LocalDateTime night = LocalDateUtil.getNight(date);
        //4.拆分时间并过滤
        List<QtDaySamplingDTO> filterList = Lists.newArrayList();
        for (QtDaySamplingDTO samplingDTO : sourceList) {
            for (LocalDateTime ldt : samplingDTO.getSamplingTimeArray()) {
                //如果时间符合要求，则保存到filterList中
                if (LocalDateUtil.isRange(ldt, morning, night, Boolean.TRUE, Boolean.TRUE)) {
                    QtDaySamplingDTO newDTO = new QtDaySamplingDTO();
                    BeanUtils.copyProperties(samplingDTO,newDTO);
                    newDTO.setSamplingTime(ldt);
                    filterList.add(newDTO);
                }
            }
        }
        SteccnTableAssertUtil.notEmpty(filterList, "配料抽检时间范围内数据为空");
        return null;
    }


    /**
     * 获取缺陷项数量
     *
     * @param id
     * @param list
     * @return
     */
    private Integer getDefectItemQuantity(Integer id, List<QtDefectItemInfoVO> list) {
        Integer quantity = 0;
        for (QtDefectItemInfoVO qtDefectItemInfoVO : list) {
            if (id.equals(qtDefectItemInfoVO.getId())) {
                quantity++;
            }
        }
        return quantity;
    }

}
