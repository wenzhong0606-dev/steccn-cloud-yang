package com.steccn.qms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.QtCheckTask;
import com.steccn.qms.domain.QtCheckTaskDetail;
import com.steccn.qms.domain.QtDefectiveItem;
import com.steccn.qms.dto.QtChartParamDTO;
import com.steccn.qms.dto.QtCheckTaskConditionDTO;
import com.steccn.qms.dto.QtOnlineCheckDefectDTO;
import com.steccn.qms.service.*;
import com.steccn.qms.util.ShardTimeUtil;
import com.steccn.qms.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 缺陷日报表
 * @createDate 2022-11-12 08:54:09
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class QtDefectiveReportServiceImpl implements QtDefectiveReportService {

    private final QtCheckTaskService qtCheckTaskService;
    private final QtCheckTaskDetailService qtCheckTaskDetailService;
    private final QtDefectiveItemService qtDefectiveItemService;
    private final QtItemCategoryService qtItemCategoryService;
    private final RemoteOfficeFeign remoteOfficeFeign;

    /**
     * 深加工缺陷类别分布
     *
     * @param dto dto
     * @return
     */
    @Override
    public QtChartBarVO deepDefectiveCategoryBar(QtChartParamDTO dto) {
        //扁平化数据
        List<QtCheckTaskDetailReportVO> flatDataList = defectiveDataFlat(dto);
        //对扁平化数据获取类别
        Set<Integer> categoryIdSet = flatDataList.stream().map(QtCheckTaskDetailReportVO::getDefectCategory).collect(Collectors.toSet());
        Map<Integer, String> categoryIdNameMap = qtItemCategoryService.listByIdCh(categoryIdSet);
        return compareCategoryAndItem(dto.getCompare(), flatDataList, categoryIdNameMap, (tDataList, key) -> tDataList.stream().filter(v -> v.getDefectCategory().equals(key)).collect(Collectors.toList()));
    }

    /**
     * 班次对比
     *
     * @param compare      对比维度
     * @param flatDataList 扁平数据
     * @param xMap         类别id-name
     * @return
     */
    private QtChartBarVO compareCategoryAndItem(Integer compare, List<QtCheckTaskDetailReportVO> flatDataList, Map<Integer, String> xMap, BiFunction<List<QtCheckTaskDetailReportVO>, Integer, List<QtCheckTaskDetailReportVO>> function) {
        //定义返回vo
        QtChartBarVO resVo = new QtChartBarVO();
        List<QtChartBarSeriesVO> series = Lists.newArrayList();
        //对比项目分组
        Map<Integer, List<QtCheckTaskDetailReportVO>> targetMap;
        List<QtCheckTaskDetailReportVO> defaultReportVoList = Lists.newArrayList();
        //图例名字
        Map<Integer, String> idNameMap;
        if (QtManagerConstant.QtChartCompareEnum.WORKSHOP.getCode().equals(compare)) {
            targetMap = flatDataList.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getProductWorkshopId));
            Set<Integer> workshopIdSet = flatDataList.stream().map(QtCheckTaskDetailReportVO::getProductWorkshopId).collect(Collectors.toSet());
            idNameMap = FeignOptional.of(remoteOfficeFeign.listByIdCh(workshopIdSet)).get();
        } else if (QtManagerConstant.QtChartCompareEnum.PRODUCTLINE.getCode().equals(compare)) {
            targetMap = flatDataList.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getProductLineId));
            Set<Integer> lineIdSet = flatDataList.stream().map(QtCheckTaskDetailReportVO::getProductLineId).collect(Collectors.toSet());
            idNameMap = FeignOptional.of(remoteOfficeFeign.listByIdCh(lineIdSet)).get();
        } else if (QtManagerConstant.QtChartCompareEnum.DEFECTCATEGORY.getCode().equals(compare)) {
            targetMap = flatDataList.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getDefectCategory));
            Set<Integer> defectCategorySet = flatDataList.stream().map(QtCheckTaskDetailReportVO::getDefectCategory).collect(Collectors.toSet());
            idNameMap = FeignOptional.of(remoteOfficeFeign.listByIdCh(defectCategorySet)).get();
        } else {
            targetMap = flatDataList.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getClassId));
            idNameMap = Maps.newHashMap();
            idNameMap.put(QtManagerConstant.QtClassesEnum.DAY.getCode(), QtManagerConstant.QtClassesEnum.DAY.getName());
            idNameMap.put(QtManagerConstant.QtClassesEnum.NIGHT.getCode(), QtManagerConstant.QtClassesEnum.NIGHT.getName());
        }
        AtomicReference<Boolean> flag = new AtomicReference<>(Boolean.FALSE);
        List<String> xAis = Lists.newArrayList();
        Map<Integer, String> finalIdNameMap = idNameMap;
        //遍历目标map
        for (Map.Entry<Integer, String> entry1 : finalIdNameMap.entrySet()) {
            List<QtCheckTaskDetailReportVO> tDataList = targetMap.getOrDefault(entry1.getKey(), defaultReportVoList);
            List<Integer> data = Lists.newArrayList();
            for (Map.Entry<Integer, String> entry2 : xMap.entrySet()) {
                List<QtCheckTaskDetailReportVO> xDataList = function.apply(tDataList, entry2.getKey());
                //每个类别下，每个班次的缺陷数之和
                int sum = xDataList.stream().mapToInt(QtCheckTaskDetailReportVO::getDefectNum).sum();
                data.add(sum);
                if (!flag.get()) {
                    xAis.add(entry2.getValue());
                }
            }
            flag.set(Boolean.TRUE);
            series.add(new QtChartBarSeriesVO(entry1.getValue(), data, "total"));
        }
        resVo.setXAis(xAis);
        resVo.setSeries(series);
        return resVo;
    }

    /**
     * 班次对比
     *
     * @param compare           对比维度
     * @param series            保存数据
     * @param flatDataList      扁平数据
     * @param categoryIdNameMap 类别id-name
     * @return
     */
    private List<String> compare(Integer compare, List<QtChartBarSeriesVO> series, List<QtCheckTaskDetailReportVO> flatDataList, Map<Integer, String> categoryIdNameMap) {
        //对比项目分组
        Map<Integer, List<QtCheckTaskDetailReportVO>> targetMap;
        List<QtCheckTaskDetailReportVO> defaultReportVoList = Lists.newArrayList();
        Map<Integer, String> idNameMap;
        if (QtManagerConstant.QtChartCompareEnum.WORKSHOP.getCode().equals(compare)) {
            targetMap = flatDataList.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getProductWorkshopId));
            Set<Integer> workshopIdSet = flatDataList.stream().map(QtCheckTaskDetailReportVO::getProductWorkshopId).collect(Collectors.toSet());
            idNameMap = FeignOptional.of(remoteOfficeFeign.listByIdCh(workshopIdSet)).get();
        } else if (QtManagerConstant.QtChartCompareEnum.PRODUCTLINE.getCode().equals(compare)) {
            targetMap = flatDataList.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getProductLineId));
            Set<Integer> lineIdSet = flatDataList.stream().map(QtCheckTaskDetailReportVO::getProductLineId).collect(Collectors.toSet());
            idNameMap = FeignOptional.of(remoteOfficeFeign.listByIdCh(lineIdSet)).get();
        } else if (QtManagerConstant.QtChartCompareEnum.DEFECTCATEGORY.getCode().equals(compare)) {
            targetMap = flatDataList.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getDefectCategory));
            Set<Integer> defectCategorySet = flatDataList.stream().map(QtCheckTaskDetailReportVO::getDefectCategory).collect(Collectors.toSet());
            idNameMap = FeignOptional.of(remoteOfficeFeign.listByIdCh(defectCategorySet)).get();
        } else {
            targetMap = flatDataList.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getClassId));
            idNameMap = Maps.newHashMap();
            idNameMap.put(QtManagerConstant.QtClassesEnum.DAY.getCode(), QtManagerConstant.QtClassesEnum.DAY.getName());
            idNameMap.put(QtManagerConstant.QtClassesEnum.NIGHT.getCode(), QtManagerConstant.QtClassesEnum.NIGHT.getName());
        }
        AtomicReference<Boolean> flag = new AtomicReference<>(Boolean.FALSE);
        List<String> xAis = Lists.newArrayList();
        Map<Integer, String> finalIdNameMap = idNameMap;
        //遍历目标map
        for (Map.Entry<Integer, String> entry1 : finalIdNameMap.entrySet()) {
            List<QtCheckTaskDetailReportVO> tDataList = targetMap.getOrDefault(entry1.getKey(), defaultReportVoList);
            List<Integer> data = Lists.newArrayList();
            for (Map.Entry<Integer, String> entry2 : categoryIdNameMap.entrySet()) {
                List<QtCheckTaskDetailReportVO> cateDataList = tDataList.stream().filter(v -> v.getDefectCategory().equals(entry2.getKey())).collect(Collectors.toList());
                //每个类别下，每个班次的缺陷数之和
                int sum = cateDataList.stream().mapToInt(QtCheckTaskDetailReportVO::getDefectNum).sum();
                data.add(sum);
                if (!flag.get()) {
                    xAis.add(entry2.getValue());
                }
            }
            flag.set(Boolean.TRUE);
            series.add(new QtChartBarSeriesVO(entry1.getValue(), data, "total"));
        }
        return xAis;
    }

    @Override
    public List<QtCheckTaskDetailReportVO> defectiveDataFlat(QtChartParamDTO dto) {
        //查询符合条件的检验任务
        QtCheckTaskConditionDTO condition = QtCheckTaskConditionDTO.builder()
                .taskType(QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode())
                .startTime(LocalDateTime.of(dto.getDateStart(), LocalTime.MIN))
                .endTime(LocalDateTime.of(dto.getDateEnd(), LocalTime.MAX))
                .workOrderId(dto.getWorkOrderId())
                .productWorkShopId(dto.getProductWorkShopId())
                .productLineId(dto.getProductLineId())
                .defectType(dto.getDefectType())
                .defectId(dto.getDefectId())
                .build();
        List<QtCheckTask> qtCheckTasks = qtCheckTaskService.conditionTaskList(condition);
        Assert.notEmpty(qtCheckTasks, "缺陷类别分布暂无数据");
        //获取检验项类别
        Map<Long, QtCheckTask> taskMap = Maps.newHashMap();
        qtCheckTasks.forEach(ele -> taskMap.put(ele.getId(), ele));
        //对符合条件的检验任务进行缺陷项的统计
        Set<Long> taskIdSet = qtCheckTasks.stream().map(QtCheckTask::getId).collect(Collectors.toSet());
        //获取所有的检验任务明细结果
        List<QtCheckTaskDetail> detailList = qtCheckTaskDetailService.list(new LambdaQueryWrapper<QtCheckTaskDetail>().in(QtCheckTaskDetail::getTaskId, taskIdSet));
        //获取所有的缺陷项id集合
        Set<Integer> defectiveIdSet = Sets.newHashSet();
        detailList.forEach(ele -> {
            if (CollUtil.isNotEmpty(ele.getDefectNum())) {
                String numString = JSON.toJSONString(ele.getDefectNum());
                List<QtOnlineCheckDefectDTO> defectNumList = JSON.parseArray(numString, QtOnlineCheckDefectDTO.class);
                for (QtOnlineCheckDefectDTO de : defectNumList) {
                    defectiveIdSet.add(de.getDefectId());
                }
            }
        });
        //获取缺陷项id集合
        Assert.notEmpty(defectiveIdSet, "深加工缺陷项集合为空，暂无报表");

        //缺陷项类别与类别的创建时间
        Map<Integer, Integer> categoryMap = Maps.newHashMap();
        List<QtDefectiveItem> qtDefectiveItemList = qtDefectiveItemService.listByIds(defectiveIdSet);
        QtDefectiveItemVO defaultQtDefectiveItemVO = new QtDefectiveItemVO();
        Assert.notEmpty(qtDefectiveItemList, "深加工缺陷集合为空，暂无报表");
        qtDefectiveItemList.forEach(ele -> categoryMap.put(ele.getId(), ele.getCategoryId()));

        //遍历缺陷项集合
        List<QtCheckTaskDetailReportVO> flatDefectiveList = Lists.newArrayList();
        detailList.forEach(ele -> {
            //获取检验任务，一定存在，因为明细是根据任务查询出来的
            QtCheckTask qtCheckTask = taskMap.get(ele.getTaskId());
            if (CollUtil.isNotEmpty(ele.getDefectNum())) {
                String numString = JSON.toJSONString(ele.getDefectNum());
                List<QtOnlineCheckDefectDTO> defectNumList = JSON.parseArray(numString, QtOnlineCheckDefectDTO.class);
                defectNumList.stream().filter(v -> Objects.nonNull(v.getDefectId())).forEach(de -> {
                    if (Objects.nonNull(de.getDefectId())) {
                        QtCheckTaskDetailReportVO vo = new QtCheckTaskDetailReportVO();
                        vo.setDefectId(de.getDefectId());
                        vo.setDefectCategory(categoryMap.get(de.getDefectId()));
                        vo.setDefectNum(Objects.nonNull(de.getDefectNum()) ? de.getDefectNum() : 0);
                        vo.setId(ele.getId());
                        vo.setTaskId(ele.getTaskId());
                        vo.setTaskCode(qtCheckTask.getCode());
                        vo.setProductWorkshopId(Objects.nonNull(qtCheckTask.getProductWorkshop()) ? qtCheckTask.getProductWorkshop().getId() : null);
                        vo.setProductLineId(Objects.nonNull(qtCheckTask.getProductLine()) ? qtCheckTask.getProductLine().getId() : null);
                        vo.setClassId(qtCheckTask.getClassesId());
                        vo.setClassesDate(ele.getCreateTime().toLocalDate());
                        vo.setDefectStartTime(qtCheckTask.getDefectStartTime());
                        vo.setDefectEndTime(qtCheckTask.getDefectEndTime());
                        vo.setFinishedTime(qtCheckTask.getFinishedTime());
                        flatDefectiveList.add(vo);
                    }
                });
            }
        });
        return flatDefectiveList;
    }

    /**
     * 深加工类别分布-饼图
     *
     * @param dto dto
     * @return
     */
    @Override
    public QtChartPieVO deepDefectiveCategoryPie(QtChartParamDTO dto) {
        //扁平化数据
        List<QtCheckTaskDetailReportVO> flatDataList = defectiveDataFlat(dto);
        Assert.notEmpty(flatDataList, "数据为空，暂无图表");
        //总缺陷数
        int sum = flatDataList.stream().mapToInt(QtCheckTaskDetailReportVO::getDefectNum).sum();
        Map<Integer, List<QtCheckTaskDetailReportVO>> defectDataMap = flatDataList.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getDefectCategory));
        Set<Integer> categoryIdSet = flatDataList.stream().map(QtCheckTaskDetailReportVO::getDefectCategory).collect(Collectors.toSet());
        Map<Integer, String> idNameMap = qtItemCategoryService.listByIdCh(categoryIdSet);
        return pieGraph(idNameMap, defectDataMap, sum);
    }

    /**
     * 饼图
     *
     * @param idNameMap     饼图每块的名字
     * @param defectDataMap 缺陷数据map
     * @param sum           总缺陷数
     * @return
     */
    private QtChartPieVO pieGraph(Map<Integer, String> idNameMap, Map<Integer, List<QtCheckTaskDetailReportVO>> defectDataMap, int sum) {
        //定义返回vo
        QtChartPieVO qtChartPieVO = new QtChartPieVO();
        List<QtChartPieValueVO> dataList = Lists.newArrayList();
        //遍历缺陷类别
        defectDataMap.forEach((k, v) -> {
            QtChartPieValueVO vo = new QtChartPieValueVO();
            vo.setName(idNameMap.getOrDefault(k, Strings.EMPTY));
            int categorySum = v.stream().mapToInt(QtCheckTaskDetailReportVO::getDefectNum).sum();
            BigDecimal multiply = BigDecimal.valueOf(categorySum).multiply(BigDecimal.valueOf(100));
            BigDecimal divide = BigDecimalUtil.divide(multiply, BigDecimal.valueOf(sum), BigDecimalUtil.SCALE_2);
            vo.setValue(divide);
            dataList.add(vo);
        });
        qtChartPieVO.setData(dataList);
        return qtChartPieVO;
    }

    @Override
    public Page<DeepDefectiveCategoryDetailVO> deepDefectiveCategoryDetail(QtChartParamDTO dto) {
        //扁平化数据
        List<QtCheckTaskDetailReportVO> flatDataList = defectiveDataFlat(dto);
        Assert.notEmpty(flatDataList, "数据为空，暂无报表");
        Set<Integer> officeIdSet = Sets.newHashSet();
        Set<Integer> defectIdSet = Sets.newHashSet();
        flatDataList.forEach(ele -> {
            defectIdSet.add(ele.getDefectId());
            officeIdSet.add(ele.getProductWorkshopId());
            officeIdSet.add(ele.getProductLineId());
        });
        Map<Integer, String> officeChMap = FeignOptional.of(remoteOfficeFeign.listByIdCh(officeIdSet)).get();
        Map<Integer, QtDefectiveItem> defectItemMap = Maps.newHashMap();
        List<QtDefectiveItem> qtDefectiveItems = qtDefectiveItemService.listByIds(defectIdSet);
        QtDefectiveItem defaultQtDefectiveItem = new QtDefectiveItem();
        if (CollUtil.isNotEmpty(qtDefectiveItems)) {
            qtDefectiveItems.forEach(ele -> defectItemMap.put(ele.getId(), ele));
        }

        //定义数据集合
        List<DeepDefectiveCategoryDetailVO> pageList = Lists.newArrayList();
        //班次日期分组
        Map<LocalDate, List<QtCheckTaskDetailReportVO>> classesDateMap = flatDataList.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getClassesDate));
        classesDateMap.forEach((classDate, classDateData) -> {
            Map<Integer, List<QtCheckTaskDetailReportVO>> classesMap = classDateData.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getClassId));
            classesMap.forEach((classes, classesData) -> {
                QtManagerConstant.QtClassesEnum classesEnum = QtManagerConstant.QtClassesEnum.code(classes);
                Map<Integer, List<QtCheckTaskDetailReportVO>> workshopMap = classesData.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getProductWorkshopId));
                workshopMap.forEach((ws, wsData) -> {
                    Map<Integer, List<QtCheckTaskDetailReportVO>> lineMap = wsData.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getProductLineId));
                    lineMap.forEach((line, lineData) -> {
                        Map<Integer, List<QtCheckTaskDetailReportVO>> defectMap = lineData.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getDefectId));
                        defectMap.forEach((defectId, defectData) -> {
                            DeepDefectiveCategoryDetailVO countVo = new DeepDefectiveCategoryDetailVO();
                            countVo.setClassesDate(classDate);
                            countVo.setClassId(classes);
                            countVo.setClassesName(classesEnum.getName());
                            countVo.setDefectNum(defectData.stream().mapToInt(QtCheckTaskDetailReportVO::getDefectNum).sum());
                            countVo.setProductWorkshopId(ws);
                            countVo.setProductWorkshopName(officeChMap.get(ws));
                            countVo.setProductLineId(line);
                            countVo.setProductLineName(officeChMap.get(line));
                            countVo.setDefectId(defectId);
                            QtDefectiveItem qtDefectiveItem = defectItemMap.getOrDefault(defectId, defaultQtDefectiveItem);
                            countVo.setDefectName(qtDefectiveItem.getName());
                            countVo.setDefectCreateTime(qtDefectiveItem.getCreateTime());
                            //明细
                            List<DeepDefectiveCategoryDetailListVO> detailList = Lists.newArrayList();
                            defectData.forEach(ele -> {
                                DeepDefectiveCategoryDetailListVO listVO = new DeepDefectiveCategoryDetailListVO();
                                BeanUtils.copyProperties(ele, listVO);
                                listVO.setDefectName(qtDefectiveItem.getName());
                                listVO.setResultSource("人工检验");
                                detailList.add(listVO);
                            });
                            countVo.setDetailList(detailList);
                            pageList.add(countVo);
                        });
                    });
                });
            });
        });

        //排序+逻辑分页
        Integer skip = (dto.getCurrent() - 1) * dto.getSize();
        pageList.stream().sorted(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getClassesDate))
                .thenComparing(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getProductWorkshopId)))
                .thenComparing(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getProductLineId)))
                .thenComparing(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getClassId)))
                .thenComparing(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getDefectCreateTime))));
        List<DeepDefectiveCategoryDetailVO> records = pageList.stream().skip(skip).limit(dto.getSize()).collect(Collectors.toList());
        Page<DeepDefectiveCategoryDetailVO> resPage = new Page(dto.getCurrent(), dto.getSize());
        resPage.setTotal(pageList.size());
        resPage.setRecords(records);
        return resPage;
    }

    @Override
    public QtChartBarVO deepDefectiveItem(QtChartParamDTO dto) {
        //扁平化数据
        List<QtCheckTaskDetailReportVO> flatDataList = defectiveDataFlat(dto);
        //对扁平化数据获取类别
        Set<Integer> defectIdSet = flatDataList.stream().map(QtCheckTaskDetailReportVO::getDefectId).collect(Collectors.toSet());
        Map<Integer, String> idNameMap = qtDefectiveItemService.listByIdCh(defectIdSet);
        return compareCategoryAndItem(dto.getCompare(), flatDataList, idNameMap, (tDataList, key) -> tDataList.stream().filter(v -> v.getDefectId().equals(key)).collect(Collectors.toList()));
    }

    /**
     * 基片缺陷分布-柱状图
     *
     * @param dto
     * @return
     */
    @Override
    public QtChartBarVO substratePlateBar(QtChartParamDTO dto) {
        List<QtCheckTaskDetailReportVO> flatDataList = defectiveDataFlat(dto);
        //对扁平化数据获取类别
        Set<Integer> defectIdSet = flatDataList.stream().map(QtCheckTaskDetailReportVO::getDefectId).collect(Collectors.toSet());
        Map<Integer, String> idNameMap = qtDefectiveItemService.listByIdCh(defectIdSet);
        return compareCategoryAndItem(dto.getCompare(), flatDataList, idNameMap, (tDataList, key) -> tDataList.stream().filter(v -> v.getDefectId().equals(key)).collect(Collectors.toList()));
    }

    /**
     * 深加工缺陷项分布饼图
     *
     * @param dto
     * @return
     */
    @Override
    public QtChartPieVO deepDefectiveItemPie(QtChartParamDTO dto) {
        //扁平化数据
        List<QtCheckTaskDetailReportVO> flatDataList = defectiveDataFlat(dto);
        Assert.notEmpty(flatDataList, "数据为空，暂无图表");
        //总缺陷数
        int sum = flatDataList.stream().mapToInt(QtCheckTaskDetailReportVO::getDefectNum).sum();
        Map<Integer, List<QtCheckTaskDetailReportVO>> defectDataMap = flatDataList.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getDefectId));
        Set<Integer> defectIdSet = flatDataList.stream().map(QtCheckTaskDetailReportVO::getDefectId).collect(Collectors.toSet());
        Map<Integer, String> idNameMap = qtDefectiveItemService.listByIdCh(defectIdSet);
        return pieGraph(idNameMap, defectDataMap, sum);
    }

    /**
     * 基片缺陷项分布-饼图
     *
     * @param dto
     * @return
     */
    @Override
    public QtChartPieVO substrateCategoryJPie(QtChartParamDTO dto) {
        //扁平化数据
        List<QtCheckTaskDetailReportVO> flatDataList = defectiveDataFlat(dto);
        Assert.notEmpty(flatDataList, "数据为空，暂无图表");
        //总缺陷数
        int sum = flatDataList.stream().mapToInt(QtCheckTaskDetailReportVO::getDefectNum).sum();
        Map<Integer, List<QtCheckTaskDetailReportVO>> defectDataMap = flatDataList.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getDefectId));
        Set<Integer> defectIdSet = flatDataList.stream().map(QtCheckTaskDetailReportVO::getDefectId).collect(Collectors.toSet());
        Map<Integer, String> idNameMap = qtDefectiveItemService.listByIdCh(defectIdSet);
        return pieGraph(idNameMap, defectDataMap, sum);
    }

    /**
     * 基片缺陷分布明细
     *
     * @param dto
     * @return
     */
    @Override
    public Page<DeepDefectiveCategoryDetailVO> substrateDefectiveCategoryDetail(QtChartParamDTO dto) {
        //扁平化数据
        List<QtCheckTaskDetailReportVO> flatDataList = defectiveDataFlat(dto);
        Assert.notEmpty(flatDataList, "数据为空，暂无报表");
        Set<Integer> categoryIdSet = flatDataList.stream().map(QtCheckTaskDetailReportVO::getDefectCategory).collect(Collectors.toSet());
        Map<Integer, String> categoryIdNameMap = qtItemCategoryService.listByIdCh(categoryIdSet);

        //定义数据集合
        List<DeepDefectiveCategoryDetailVO> pageList = Lists.newArrayList();
        //班次日期分组
        Map<LocalDate, List<QtCheckTaskDetailReportVO>> classesDateMap = flatDataList.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getClassesDate));
        classesDateMap.forEach((classDate, classDateData) -> {


            Map<Integer, List<QtCheckTaskDetailReportVO>> classesMap = classDateData.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getClassId));
            classesMap.forEach((classes, classesData) -> {
                QtManagerConstant.QtClassesEnum classesEnum = QtManagerConstant.QtClassesEnum.code(classes);

                Map<Integer, List<QtCheckTaskDetailReportVO>> workshopMap = classesData.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getProductWorkshopId));
                workshopMap.forEach((ws, wsData) -> {
                    Map<Integer, List<QtCheckTaskDetailReportVO>> lineMap = wsData.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getProductLineId));
                    lineMap.forEach((line, lineData) -> {
                        Map<Integer, List<QtCheckTaskDetailReportVO>> categoryMap = lineData.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getDefectCategory));
                        categoryMap.forEach((cate, cateData) -> {
                            DeepDefectiveCategoryDetailVO countVo = new DeepDefectiveCategoryDetailVO();
                            countVo.setClassId(classes);
                            pageList.add(countVo);
                        });
                    });
                });
            });
        });

        //排序+逻辑分页
        Integer skip = (dto.getCurrent() - 1) * dto.getSize();
//        pageList.stream().sorted(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getClassesDate))
//                .thenComparing(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getProductWorkshopId)))
//                .thenComparing(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getProductLineId)))
//                .thenComparing(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getClassId)))
//                .thenComparing(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getDefectCategoryCreateTime))));
        List<DeepDefectiveCategoryDetailVO> records = pageList.stream().skip(skip).collect(Collectors.toList());
        Page<DeepDefectiveCategoryDetailVO> resPage = new Page(dto.getCurrent(), dto.getSize());
        resPage.setTotal(pageList.size());
        resPage.setRecords(records);
        return resPage;

    }

    @Override
    public QtLineChartBarVO deepDefectiveCategoryLine(QtChartParamDTO dto) {
        //获取扁平化数据
        List<QtCheckTaskDetailReportVO> flatDataList = defectiveDataFlat(dto);
        Assert.notEmpty(flatDataList, "数据为空，暂无报表");
        //获取粒度枚举
        QtManagerConstant.QtGranularityEnum granularityEnum = QtManagerConstant.QtGranularityEnum.getByName(dto.getGranularity());
        Assert.notNull(granularityEnum, "粒度不存在");
        //将时间范围根据粒度分片
        List<LocalDateTime> cutDate = ShardTimeUtil.cutDateRange(granularityEnum.getType()
                , LocalDateTime.of(dto.getDateStart(), LocalTime.MIN)
                , LocalDateTime.of(dto.getDateEnd(), LocalTime.MAX));
        Assert.notEmpty(cutDate, "时间范围根据粒度分片为空");

        Set<Integer> categoryIdSet = flatDataList.stream().map(QtCheckTaskDetailReportVO::getDefectCategory).collect(Collectors.toSet());
        Map<Integer, String> categoryIdNameMap = qtItemCategoryService.listByIdCh(categoryIdSet);

        //定义返回数据
        QtLineChartBarVO<String> resVo = new QtLineChartBarVO();
        List<QtSeriesDataVO> seriesDataVO = Lists.newArrayList();
        //确定数据范围
        Map<Integer, List<String>> dataMap = Maps.newHashMap();
        Map<Integer, QtSeriesDataVO> serialDataMap = Maps.newHashMap();
        List<String> legendData = Lists.newArrayList();
        List<String> xData = Lists.newArrayList();
        boolean flag = Boolean.TRUE;
        for (int i = 1; i < cutDate.size(); i++) {
            LocalDateTime start = cutDate.get(i - 1);
            LocalDateTime end = cutDate.get(i);
            List<QtCheckTaskDetailReportVO> conditionDataList = flatDataList.stream().filter(v -> {
                //数据范围应该是大于等于开始时间  小于结束时间
                if ((start.isBefore(v.getFinishedTime()) || start.isEqual(v.getFinishedTime())) && end.isAfter(v.getFinishedTime())) {
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }).collect(Collectors.toList());

            xData.add(start.toString());
            //类别
            for (Map.Entry<Integer, String> entry : categoryIdNameMap.entrySet()) {
                Integer k = entry.getKey();
                QtSeriesDataVO qtSeriesDataVO = serialDataMap.get(k);
                if (!dataMap.containsKey(k)) {
                    dataMap.put(k, Lists.newArrayList());
                }
                List<String> dataList = dataMap.get(k);
                List<QtCheckTaskDetailReportVO> categoryDataList = conditionDataList.stream().filter(ele -> k.equals(ele.getDefectCategory())).collect(Collectors.toList());
                int categorySum = categoryDataList.stream().mapToInt(QtCheckTaskDetailReportVO::getDefectNum).sum();
                dataList.add(String.valueOf(categorySum));
                if (flag) {
                    legendData.add(categoryIdNameMap.get(k));
                }
                if (Objects.isNull(qtSeriesDataVO)) {
                    qtSeriesDataVO = new QtSeriesDataVO();
                    qtSeriesDataVO.setData(dataList);
                    qtSeriesDataVO.setName(categoryIdNameMap.get(k));
                    qtSeriesDataVO.setType("line");
                    qtSeriesDataVO.setStack("total");
                    seriesDataVO.add(qtSeriesDataVO);
                    serialDataMap.put(k, qtSeriesDataVO);
                }
            }
            flag = Boolean.FALSE;
            resVo.setSeriesDataVO(seriesDataVO);
        }
        resVo.setXData(xData);
        resVo.setYear(String.valueOf(LocalDate.now().getYear()));
        resVo.setName(legendData);
        return resVo;
    }

    /**
     * 深加工缺陷项趋势-折线图
     *
     * @param dto
     * @return
     */
    @Override
    public QtChartBarVO deepDefectiveCategoryTrend(QtChartParamDTO dto) {
        //定义返回vo
        QtChartBarVO resVo = new QtChartBarVO();
        List<QtChartBarSeriesVO> series = Lists.newArrayList();
        List<String> lgendData = Lists.newArrayList();
        //扁平化数据
        List<QtCheckTaskDetailReportVO> flatDataList = defectiveDataFlat(dto);
        //对扁平化数据获取类别
        Set<Integer> categoryIdSet = flatDataList.stream().map(QtCheckTaskDetailReportVO::getDefectCategory).collect(Collectors.toSet());
        Map<Integer, String> categoryIdNameMap = qtItemCategoryService.listByIdCh(categoryIdSet);
        List<String> xAis = compareTrend(dto.getDefectId(), series, flatDataList, categoryIdNameMap);
        resVo.setSeries(series);
        resVo.setXAis(xAis);
        return resVo;

        /*for (QtCheckTaskDetailReportVO tem:flatDataList) {
            resVo.setSeries();String.valueOf(tem.getDefectCategoryCreateTime().getYear());
        }
        Map<Integer, String> categoryIdNameMap = qtItemCategoryService.listByIdCh(categoryIdSet);



        List<String> xAis = compare(categoryIdSet.stream().filter(dto.getDateStart()), series, flatDataList, categoryIdNameMap);
        resVo.setSeries(series);
        resVo.setXAis(xAis);
        return resVo;*/
    }

    /**
     * 基片缺陷项趋势-折线图
     *
     * @param dto
     * @return
     */
    @Override
    public QtLineChartBarVO substrateDefectiveCategoryTrend(QtChartParamDTO dto) {
        //获取扁平化数据
        List<QtCheckTaskDetailReportVO> flatDataList = defectiveDataFlat(dto);
        Assert.notEmpty(flatDataList, "数据为空，暂无报表");
        //获取粒度枚举
        QtManagerConstant.QtGranularityEnum granularityEnum = QtManagerConstant.QtGranularityEnum.getByName(dto.getGranularity());
        Assert.notNull(granularityEnum, "粒度不存在");
        //将时间范围根据粒度分片
        List<LocalDateTime> cutDate = ShardTimeUtil.cutDateRange(granularityEnum.getType()
                , LocalDateTime.of(dto.getDateStart(), LocalTime.MIN)
                , LocalDateTime.of(dto.getDateEnd(), LocalTime.MAX));
        Assert.notEmpty(cutDate, "时间范围根据粒度分片为空");

        //查询基片车间
        Set<Integer> categoryIdSet = flatDataList.stream().map(QtCheckTaskDetailReportVO::getProductLineId).collect(Collectors.toSet());
        Map<Integer, String> categoryIdNameMap = qtItemCategoryService.listByIdCh(categoryIdSet);

        //定义返回数据
        QtLineChartBarVO<String> resVo = new QtLineChartBarVO();
        List<QtSeriesDataVO> seriesDataVO = Lists.newArrayList();
        //确定数据范围
        Map<Integer, List<String>> dataMap = Maps.newHashMap();
        Map<Integer, QtSeriesDataVO> serialDataMap = Maps.newHashMap();
        List<String> legendData = Lists.newArrayList();
        List<String> xData = Lists.newArrayList();
        boolean flag = Boolean.TRUE;
        for (int i = 1; i < cutDate.size(); i++) {
            LocalDateTime start = cutDate.get(i - 1);
            LocalDateTime end = cutDate.get(i);
            List<QtCheckTaskDetailReportVO> conditionDataList = flatDataList.stream().filter(v -> {
                //数据范围应该是大于等于开始时间  小于结束时间
                if ((start.isBefore(v.getFinishedTime()) || start.isEqual(v.getFinishedTime())) && end.isAfter(v.getFinishedTime())) {
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }).collect(Collectors.toList());

            xData.add(start.toString());
            //类别
            for (Map.Entry<Integer, String> entry : categoryIdNameMap.entrySet()) {
                Integer k = entry.getKey();
                QtSeriesDataVO qtSeriesDataVO = serialDataMap.get(k);
                if (!dataMap.containsKey(k)) {
                    dataMap.put(k, Lists.newArrayList());
                }
                List<String> dataList = dataMap.get(k);
                List<QtCheckTaskDetailReportVO> categoryDataList = conditionDataList.stream().filter(ele -> k.equals(ele.getDefectCategory())).collect(Collectors.toList());
                int categorySum = categoryDataList.stream().mapToInt(QtCheckTaskDetailReportVO::getDefectNum).sum();
                dataList.add(String.valueOf(categorySum));
                if (flag) {
                    legendData.add(categoryIdNameMap.get(k));
                }
                if (Objects.isNull(qtSeriesDataVO)) {
                    qtSeriesDataVO = new QtSeriesDataVO();
                    qtSeriesDataVO.setData(dataList);
                    qtSeriesDataVO.setName(categoryIdNameMap.get(k));
                    qtSeriesDataVO.setType("line");
                    qtSeriesDataVO.setStack("total");
                    seriesDataVO.add(qtSeriesDataVO);
                    serialDataMap.put(k, qtSeriesDataVO);
                }
            }
            flag = Boolean.FALSE;
            resVo.setSeriesDataVO(seriesDataVO);
        }
        resVo.setXData(xData);
        resVo.setYear(String.valueOf(LocalDate.now().getYear()));
        resVo.setName(legendData);
        return resVo;
    }

    /**
     * 深加工缺陷项
     *
     * @param dto
     * @return
     */
    @Override
    public QtLineChartBarVO deepDefectiveCategoryInfoTrend(QtChartParamDTO dto) {
        //获取扁平化数据
        List<QtCheckTaskDetailReportVO> flatDataList = defectiveDataFlat(dto);
        Assert.notEmpty(flatDataList, "数据为空，暂无报表");
        //获取粒度枚举
        QtManagerConstant.QtGranularityEnum granularityEnum = QtManagerConstant.QtGranularityEnum.getByName(dto.getGranularity());
        Assert.notNull(granularityEnum, "粒度不存在");
        //将时间范围根据粒度分片
        List<LocalDateTime> cutDate = ShardTimeUtil.cutDateRange(granularityEnum.getType()
                , LocalDateTime.of(dto.getDateStart(), LocalTime.MIN)
                , LocalDateTime.of(dto.getDateEnd(), LocalTime.MAX));
        Assert.notEmpty(cutDate, "时间范围根据粒度分片为空");

        //查询基片车间
        Set<Integer> categoryIdSet = flatDataList.stream().map(QtCheckTaskDetailReportVO::getProductLineId).collect(Collectors.toSet());
        Map<Integer, String> categoryIdNameMap = qtItemCategoryService.listByIdCh(categoryIdSet);

        //定义返回数据
        QtLineChartBarVO<String> resVo = new QtLineChartBarVO();
        List<QtSeriesDataVO> seriesDataVO = Lists.newArrayList();
        //确定数据范围
        Map<Integer, List<String>> dataMap = Maps.newHashMap();
        Map<Integer, QtSeriesDataVO> serialDataMap = Maps.newHashMap();
        List<String> legendData = Lists.newArrayList();
        List<String> xData = Lists.newArrayList();
        boolean flag = Boolean.TRUE;
        for (int i = 1; i < cutDate.size(); i++) {
            LocalDateTime start = cutDate.get(i - 1);
            LocalDateTime end = cutDate.get(i);
            List<QtCheckTaskDetailReportVO> conditionDataList = flatDataList.stream().filter(v -> {
                //数据范围应该是大于等于开始时间  小于结束时间
                if ((start.isBefore(v.getFinishedTime()) || start.isEqual(v.getFinishedTime())) && end.isAfter(v.getFinishedTime())) {
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }).collect(Collectors.toList());

            xData.add(start.toString());
            //类别
            for (Map.Entry<Integer, String> entry : categoryIdNameMap.entrySet()) {
                Integer k = entry.getKey();
                QtSeriesDataVO qtSeriesDataVO = serialDataMap.get(k);
                if (!dataMap.containsKey(k)) {
                    dataMap.put(k, Lists.newArrayList());
                }
                List<String> dataList = dataMap.get(k);
                List<QtCheckTaskDetailReportVO> categoryDataList = conditionDataList.stream().filter(ele -> k.equals(ele.getDefectCategory())).collect(Collectors.toList());
                int categorySum = categoryDataList.stream().mapToInt(QtCheckTaskDetailReportVO::getDefectNum).sum();
                dataList.add(String.valueOf(categorySum));
                if (flag) {
                    legendData.add(categoryIdNameMap.get(k));
                }
                if (Objects.isNull(qtSeriesDataVO)) {
                    qtSeriesDataVO = new QtSeriesDataVO();
                    qtSeriesDataVO.setData(dataList);
                    qtSeriesDataVO.setName(categoryIdNameMap.get(k));
                    qtSeriesDataVO.setType("line");
                    qtSeriesDataVO.setStack("total");
                    seriesDataVO.add(qtSeriesDataVO);
                    serialDataMap.put(k, qtSeriesDataVO);
                }
            }
            flag = Boolean.FALSE;
            resVo.setSeriesDataVO(seriesDataVO);
        }
        resVo.setXData(xData);
        resVo.setYear(String.valueOf(LocalDate.now().getYear()));
        resVo.setName(legendData);
        return resVo;
    }

    /**
     * 深加工缺陷分布明细
     *
     * @param dto
     * @return
     */
    @Override
    public Page<DeepDefectiveCategoryDetailVO> deepDefectiveItemDetail(QtChartParamDTO dto) {
        //扁平化数据
        List<QtCheckTaskDetailReportVO> flatDataList = defectiveDataFlat(dto);
        Assert.notEmpty(flatDataList, "数据为空，暂无报表");
        Set<Integer> categoryIdSet = flatDataList.stream().map(QtCheckTaskDetailReportVO::getDefectId).collect(Collectors.toSet());
        Map<Integer, String> categoryIdNameMap = qtItemCategoryService.listByIdCh(categoryIdSet);


        //定义数据集合
        List<DeepDefectiveCategoryDetailVO> pageList = Lists.newArrayList();
        //班次日期分组
        Map<LocalDate, List<QtCheckTaskDetailReportVO>> classesDateMap = flatDataList.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getClassesDate));
        classesDateMap.forEach((classDate, classDateData) -> {


            Map<Integer, List<QtCheckTaskDetailReportVO>> classesMap = classDateData.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getClassId));
            classesMap.forEach((classes, classesData) -> {
                QtManagerConstant.QtClassesEnum classesEnum = QtManagerConstant.QtClassesEnum.code(classes);

                Map<Integer, List<QtCheckTaskDetailReportVO>> workshopMap = classesData.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getProductWorkshopId));
                workshopMap.forEach((ws, wsData) -> {
                    Map<Integer, List<QtCheckTaskDetailReportVO>> lineMap = wsData.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getProductLineId));
                    lineMap.forEach((line, lineData) -> {
                        Map<Integer, List<QtCheckTaskDetailReportVO>> categoryMap = lineData.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getDefectId));
                        categoryMap.forEach((cate, cateData) -> {
                            DeepDefectiveCategoryDetailVO countVo = new DeepDefectiveCategoryDetailVO();
                            countVo.setClassId(classes);
                            pageList.add(countVo);
                        });
                    });
                });
            });
        });

        //排序+逻辑分页
        Integer skip = (dto.getCurrent() - 1) * dto.getSize();
//        pageList.stream().sorted(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getClassesDate))
//                .thenComparing(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getProductWorkshopId)))
//                .thenComparing(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getProductLineId)))
//                .thenComparing(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getClassId)))
//                .thenComparing(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getDefectCategoryCreateTime))));
        List<DeepDefectiveCategoryDetailVO> records = pageList.stream().skip(skip).collect(Collectors.toList());
        Page<DeepDefectiveCategoryDetailVO> resPage = new Page(dto.getCurrent(), dto.getSize());
        resPage.setTotal(pageList.size());
        resPage.setRecords(records);
        return resPage;

    }

    /**
     * 深加工类别趋势明细
     *
     * @param dto
     * @return
     */
    @Override
    public Page<DeepDefectiveCategoryDetailVO> deepDefectiveCategoryTrendDetail(QtChartParamDTO dto) {
        //扁平化数据
        List<QtCheckTaskDetailReportVO> flatDataList = defectiveDataFlat(dto);
        Assert.notEmpty(flatDataList, "数据为空，暂无报表");
        Set<Integer> categoryIdSet = flatDataList.stream().map(QtCheckTaskDetailReportVO::getDefectId).collect(Collectors.toSet());
        Map<Integer, String> categoryIdNameMap = qtItemCategoryService.listByIdCh(categoryIdSet);


        //定义数据集合
        List<DeepDefectiveCategoryDetailVO> pageList = Lists.newArrayList();
        //班次日期分组
        Map<LocalDate, List<QtCheckTaskDetailReportVO>> classesDateMap = flatDataList.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getClassesDate));
        classesDateMap.forEach((classDate, classDateData) -> {


            Map<Integer, List<QtCheckTaskDetailReportVO>> classesMap = classDateData.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getClassId));
            classesMap.forEach((classes, classesData) -> {
                QtManagerConstant.QtClassesEnum classesEnum = QtManagerConstant.QtClassesEnum.code(classes);

                Map<Integer, List<QtCheckTaskDetailReportVO>> workshopMap = classesData.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getProductWorkshopId));
                workshopMap.forEach((ws, wsData) -> {
                    Map<Integer, List<QtCheckTaskDetailReportVO>> lineMap = wsData.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getProductLineId));
                    lineMap.forEach((line, lineData) -> {
                        Map<Integer, List<QtCheckTaskDetailReportVO>> categoryMap = lineData.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getDefectId));
                        categoryMap.forEach((cate, cateData) -> {
                            DeepDefectiveCategoryDetailVO countVo = new DeepDefectiveCategoryDetailVO();
                            countVo.setClassId(classes);
                            pageList.add(countVo);
                        });
                    });
                });
            });
        });

        //排序+逻辑分页
        Integer skip = (dto.getCurrent() - 1) * dto.getSize();
//        pageList.stream().sorted(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getClassesDate))
//                .thenComparing(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getProductWorkshopId)))
//                .thenComparing(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getProductLineId)))
//                .thenComparing(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getClassId)))
//                .thenComparing(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getDefectCategoryCreateTime))));
        List<DeepDefectiveCategoryDetailVO> records = pageList.stream().skip(skip).collect(Collectors.toList());
        Page<DeepDefectiveCategoryDetailVO> resPage = new Page(dto.getCurrent(), dto.getSize());
        resPage.setTotal(pageList.size());
        resPage.setRecords(records);
        return resPage;
    }

    /**
     * 深加工缺陷项趋势-折线图-明细
     *
     * @param dto
     * @return
     */
    @Override
    public Page<DeepDefectiveCategoryDetailVO> deepDefectiveCategoryInfoTrendDetail(QtChartParamDTO dto) {
        //扁平化数据
        List<QtCheckTaskDetailReportVO> flatDataList = defectiveDataFlat(dto);
        Assert.notEmpty(flatDataList, "数据为空，暂无报表");
        Set<Integer> categoryIdSet = flatDataList.stream().map(QtCheckTaskDetailReportVO::getDefectId).collect(Collectors.toSet());
        Map<Integer, String> categoryIdNameMap = qtItemCategoryService.listByIdCh(categoryIdSet);


        //定义数据集合
        List<DeepDefectiveCategoryDetailVO> pageList = Lists.newArrayList();
        //班次日期分组
        Map<LocalDate, List<QtCheckTaskDetailReportVO>> classesDateMap = flatDataList.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getClassesDate));
        classesDateMap.forEach((classDate, classDateData) -> {


            Map<Integer, List<QtCheckTaskDetailReportVO>> classesMap = classDateData.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getClassId));
            classesMap.forEach((classes, classesData) -> {
                QtManagerConstant.QtClassesEnum classesEnum = QtManagerConstant.QtClassesEnum.code(classes);

                Map<Integer, List<QtCheckTaskDetailReportVO>> workshopMap = classesData.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getProductWorkshopId));
                workshopMap.forEach((ws, wsData) -> {
                    Map<Integer, List<QtCheckTaskDetailReportVO>> lineMap = wsData.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getProductLineId));
                    lineMap.forEach((line, lineData) -> {
                        Map<Integer, List<QtCheckTaskDetailReportVO>> categoryMap = lineData.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getDefectId));
                        categoryMap.forEach((cate, cateData) -> {
                            DeepDefectiveCategoryDetailVO countVo = new DeepDefectiveCategoryDetailVO();
                            countVo.setClassId(classes);
                            pageList.add(countVo);
                        });
                    });
                });
            });
        });

        //排序+逻辑分页
        Integer skip = (dto.getCurrent() - 1) * dto.getSize();
//        pageList.stream().sorted(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getClassesDate))
//                .thenComparing(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getProductWorkshopId)))
//                .thenComparing(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getProductLineId)))
//                .thenComparing(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getClassId)))
//                .thenComparing(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getDefectCategoryCreateTime))));
        List<DeepDefectiveCategoryDetailVO> records = pageList.stream().skip(skip).collect(Collectors.toList());
        Page<DeepDefectiveCategoryDetailVO> resPage = new Page(dto.getCurrent(), dto.getSize());
        resPage.setTotal(pageList.size());
        resPage.setRecords(records);
        return resPage;
    }

    /**
     * 基片缺陷项趋势-折线图-明细
     *
     * @param dto
     * @return
     */
    @Override
    public Page<DeepDefectiveCategoryDetailVO> substrateDefectiveCategoryTrendDetail(QtChartParamDTO dto) {
        //扁平化数据
        List<QtCheckTaskDetailReportVO> flatDataList = defectiveDataFlat(dto);
        Assert.notEmpty(flatDataList, "数据为空，暂无报表");
        Set<Integer> categoryIdSet = flatDataList.stream().map(QtCheckTaskDetailReportVO::getDefectId).collect(Collectors.toSet());
        Map<Integer, String> categoryIdNameMap = qtItemCategoryService.listByIdCh(categoryIdSet);


        //定义数据集合
        List<DeepDefectiveCategoryDetailVO> pageList = Lists.newArrayList();
        //班次日期分组
        Map<LocalDate, List<QtCheckTaskDetailReportVO>> classesDateMap = flatDataList.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getClassesDate));
        classesDateMap.forEach((classDate, classDateData) -> {


            Map<Integer, List<QtCheckTaskDetailReportVO>> classesMap = classDateData.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getClassId));
            classesMap.forEach((classes, classesData) -> {
                QtManagerConstant.QtClassesEnum classesEnum = QtManagerConstant.QtClassesEnum.code(classes);

                Map<Integer, List<QtCheckTaskDetailReportVO>> workshopMap = classesData.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getProductWorkshopId));
                workshopMap.forEach((ws, wsData) -> {
                    Map<Integer, List<QtCheckTaskDetailReportVO>> lineMap = wsData.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getProductLineId));
                    lineMap.forEach((line, lineData) -> {
                        Map<Integer, List<QtCheckTaskDetailReportVO>> categoryMap = lineData.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getDefectId));
                        categoryMap.forEach((cate, cateData) -> {
                            DeepDefectiveCategoryDetailVO countVo = new DeepDefectiveCategoryDetailVO();
                            countVo.setClassId(classes);
                            pageList.add(countVo);
                        });
                    });
                });
            });
        });

        //排序+逻辑分页
        Integer skip = (dto.getCurrent() - 1) * dto.getSize();
//        pageList.stream().sorted(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getClassesDate))
//                .thenComparing(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getProductWorkshopId)))
//                .thenComparing(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getProductLineId)))
//                .thenComparing(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getClassId)))
//                .thenComparing(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getDefectCategoryCreateTime))));
        List<DeepDefectiveCategoryDetailVO> records = pageList.stream().skip(skip).collect(Collectors.toList());
        Page<DeepDefectiveCategoryDetailVO> resPage = new Page(dto.getCurrent(), dto.getSize());
        resPage.setTotal(pageList.size());
        resPage.setRecords(records);
        return resPage;
    }

    private List<String> compareTrend(Integer compare, List<QtChartBarSeriesVO> series, List<QtCheckTaskDetailReportVO> flatDataList, Map<Integer, String> categoryIdNameMap) {
        //对比项目分组
        Map<Integer, List<QtCheckTaskDetailReportVO>> targetMap;
        Map<Integer, String> idNameMap;
        if (QtManagerConstant.QtChartCompareEnum.WORKSHOP.getCode().equals(compare)) {
            targetMap = flatDataList.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getProductWorkshopId));
            Set<Integer> workshopIdSet = flatDataList.stream().map(QtCheckTaskDetailReportVO::getProductWorkshopId).collect(Collectors.toSet());
            idNameMap = FeignOptional.of(remoteOfficeFeign.listByIdCh(workshopIdSet)).get();
        } else if (QtManagerConstant.QtChartCompareEnum.PRODUCTLINE.getCode().equals(compare)) {
            targetMap = flatDataList.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getProductLineId));
            Set<Integer> lineIdSet = flatDataList.stream().map(QtCheckTaskDetailReportVO::getProductLineId).collect(Collectors.toSet());
            idNameMap = FeignOptional.of(remoteOfficeFeign.listByIdCh(lineIdSet)).get();
        } else if (QtManagerConstant.QtChartCompareEnum.DEFECTCATEGORY.getCode().equals(compare)) {
            targetMap = flatDataList.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getDefectCategory));
            Set<Integer> defectCategorySet = flatDataList.stream().map(QtCheckTaskDetailReportVO::getDefectCategory).collect(Collectors.toSet());
            idNameMap = FeignOptional.of(remoteOfficeFeign.listByIdCh(defectCategorySet)).get();
        } else {
            targetMap = flatDataList.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getClassId));
            idNameMap = Maps.newHashMap();
            idNameMap.put(QtManagerConstant.QtClassesEnum.DAY.getCode(), QtManagerConstant.QtClassesEnum.DAY.getName());
            idNameMap.put(QtManagerConstant.QtClassesEnum.NIGHT.getCode(), QtManagerConstant.QtClassesEnum.NIGHT.getName());
        }
        AtomicReference<Boolean> flag = new AtomicReference<>(Boolean.FALSE);
        List<String> xAis = Lists.newArrayList();
        Map<Integer, String> finalIdNameMap = idNameMap;
        //遍历目标map
        targetMap.forEach((t, tDataList) -> {
            List<Integer> data = Lists.newArrayList();
            //根据类别分组map
            Map<Integer, List<QtCheckTaskDetailReportVO>> categoryMap = tDataList.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getDefectCategory));
            categoryMap.forEach((cate, cateDataList) -> {
                //每个类别下，每个班次的缺陷数之和
                int sum = cateDataList.stream().mapToInt(QtCheckTaskDetailReportVO::getDefectNum).sum();
                data.add(sum);
                if (!flag.get()) {
                    xAis.add(categoryIdNameMap.get(cate));
                    // xAis.add()
                }
            });
            flag.set(Boolean.TRUE);
            // series.add(new QtChartBarSeriesVO(finalIdNameMap.getOrDefault(t, Strings.EMPTY), data, "total"));
        });
        return xAis;
    }

}
