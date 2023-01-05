package com.steccn.qms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.lang.Assert;
import com.alibaba.excel.util.MapUtils;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.exception.SteccnChartException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.dto.StackGraphDTO;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.CalculateDateUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.util.StackGraphUtil;
import com.steccn.common.vo.QtTaskClassCalculate;
import com.steccn.data.feign.RemoteDataPlatformFeignApi;
import com.steccn.data.vo.DwDefectCollectVO;
import com.steccn.platform.constant.SysOfficeConstant;
import com.steccn.platform.dto.SysOfficeDTO;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSysConfigurationFeignApi;
import com.steccn.platform.util.LogicalPageUtil;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.QtCheckTask;
import com.steccn.qms.domain.QtCheckTaskDetail;
import com.steccn.qms.domain.QtDefectiveItem;
import com.steccn.qms.domain.QtItemCategory;
import com.steccn.qms.dto.QtChartParamDTO;
import com.steccn.qms.dto.QtCheckTaskConditionDTO;
import com.steccn.qms.dto.QtOnlineCheckDefectDTO;
import com.steccn.qms.service.*;
import com.steccn.qms.util.DownloadUtil;
import com.steccn.qms.util.ShardTimeUtil;
import com.steccn.qms.util.XAxisUtil;
import com.steccn.qms.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
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
    private final RemoteSysConfigurationFeignApi remoteSysConfigurationFeignApi;
    private final RemoteDataPlatformFeignApi remoteDataPlatformFeignApi;

    /**
     * 深加工缺陷类别分布
     *
     * @param dto dto
     * @return
     */
    @Override
    public QtLineChartBarDTO deepDefectiveCategoryBar(QtChartParamDTO dto) {
        //获取所有扁平化数据
        List<QtCheckTaskDetailReportVO> flatDataList = defectiveDataFlat(dto);
        //如果车间和产线为空，表示查询深加工车间所有数据
        flatDataList = workshopFilter(dto, flatDataList, QtManagerConstant.DEEP_OFFICE_NAME);
        SteccnTableAssertUtil.notEmpty(flatDataList,"数据为空，暂无报表");
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
    private QtLineChartBarDTO compareCategoryAndItem(Integer compare, List<QtCheckTaskDetailReportVO> flatDataList, Map<Integer, String> xMap, BiFunction<List<QtCheckTaskDetailReportVO>, Integer, List<QtCheckTaskDetailReportVO>> function) {
        List<QtCheckTaskDetailReportVO> defaultReportVoList = Lists.newArrayList();
        //对比项目分组
        TupleDTO<Map<Integer, List<QtCheckTaskDetailReportVO>>, Map<Integer, String>> mapMapTupleDTO = commonGroup(compare, flatDataList);
        Map<Integer, List<QtCheckTaskDetailReportVO>> targetMap = mapMapTupleDTO.getField1();
        Map<Integer, String> idNameMap = mapMapTupleDTO.getField2();
        //存放数据的map
        List<StackGraphDTO<String, Integer>> stackGraphList = Lists.newArrayList();
        //根据x轴分组
        for (Map.Entry<Integer, String> categoryEntry : xMap.entrySet()) {
            StackGraphDTO stackGraphDTO = new StackGraphDTO();
            stackGraphDTO.setData(Lists.newArrayList());
            stackGraphDTO.setXAxis(categoryEntry.getValue());
            //考虑对比分组，也可能不分组
            for (Map.Entry<Integer, String> contrastEntry : idNameMap.entrySet()) {
                List<QtCheckTaskDetailReportVO> tDataList = targetMap.getOrDefault(contrastEntry.getKey(), defaultReportVoList);
                List<QtCheckTaskDetailReportVO> xDataList = function.apply(tDataList, categoryEntry.getKey());
                //每个类别下，每个班次的缺陷数之和
                int sum = xDataList.stream().mapToInt(QtCheckTaskDetailReportVO::getDefectNum).sum();
                stackGraphDTO.getData().add(new TupleDTO<>(contrastEntry.getValue(), sum));
            }
            stackGraphList.add(stackGraphDTO);
        }
        //倒序排序
        stackGraphList.sort((o1, o2) -> {
            int sum1 = o1.getData().stream().filter(v -> Objects.nonNull(v.getField2())).mapToInt(v -> v.getField2()).sum();
            int sum2 = o2.getData().stream().filter(v -> Objects.nonNull(v.getField2())).mapToInt(v -> v.getField2()).sum();
            return Integer.valueOf(sum2).compareTo(sum1);
        });
        return StackGraphUtil.convertData(stackGraphList);
    }

    /**
     * 公共分组
     *
     * @param compare      对比
     * @param flatDataList 数据集合
     */
    private TupleDTO<Map<Integer, List<QtCheckTaskDetailReportVO>>, Map<Integer, String>> commonGroup(Integer compare, List<QtCheckTaskDetailReportVO> flatDataList) {
        Map<Integer, List<QtCheckTaskDetailReportVO>> targetMap;
        Map<Integer, String> idNameMap;
        if (QtManagerConstant.QtChartCompareEnum.WORKSHOP.getCode().equals(compare)) {
            targetMap = flatDataList.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getProductWorkshopId));
            Set<Integer> workshopIdSet = flatDataList.stream().map(QtCheckTaskDetailReportVO::getProductWorkshopId).collect(Collectors.toSet());
            Map<Integer, SysOfficeVO> dataMap = FeignOptional.of(remoteOfficeFeign.listById(workshopIdSet)).get();
            idNameMap =  officeContrastSort(dataMap);
        } else if (QtManagerConstant.QtChartCompareEnum.PRODUCTLINE.getCode().equals(compare)) {
            targetMap = flatDataList.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getProductLineId));
            Set<Integer> lineIdSet = flatDataList.stream().map(QtCheckTaskDetailReportVO::getProductLineId).collect(Collectors.toSet());
            Map<Integer, SysOfficeVO> dataMap = FeignOptional.of(remoteOfficeFeign.listById(lineIdSet)).get();
            idNameMap =  officeContrastSort(dataMap);
        } else if (QtManagerConstant.QtChartCompareEnum.DEFECTCATEGORY.getCode().equals(compare)) {
            targetMap = flatDataList.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getDefectCategory));
            Set<Integer> defectCategorySet = flatDataList.stream().map(QtCheckTaskDetailReportVO::getDefectCategory).collect(Collectors.toSet());
            Map<Integer, QtItemCategory> defectCategoryMap = qtItemCategoryService.listById(defectCategorySet);
            idNameMap = defectCategorySort(defectCategoryMap);
        } else if(QtManagerConstant.QtChartCompareEnum.WORK.getCode().equals(compare)){
            targetMap = flatDataList.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getClassId));
            List<Integer> classIdSet = flatDataList.stream().map(QtCheckTaskDetailReportVO::getClassId).collect(Collectors.toList());
            idNameMap = Maps.newHashMap();
            Collections.sort(classIdSet);
            for (Integer classId : classIdSet) {
                QtManagerConstant.QtClassesEnum code = QtManagerConstant.QtClassesEnum.code(classId);
                idNameMap.put(code.getCode(), code.getName());
            }
        }else{
            //不对比
            targetMap = Maps.newHashMap();
            targetMap.put(0, flatDataList);
            idNameMap = Maps.newHashMap();
            idNameMap.put(0, StackGraphUtil.STR_DEFAULT);
        }
        return new TupleDTO<>(targetMap, idNameMap);
    }

    /**
     * 组织架构对比排序
     * @param dataMap 数据map
     * @return 排序后的map
     */
    private Map<Integer, String> officeContrastSort(Map<Integer, SysOfficeVO> dataMap) {
        Map<Integer,String> idNameMap = Maps.newLinkedHashMap();
        if(CollUtil.isNotEmpty(dataMap)){
            List<SysOfficeVO> collect = dataMap.values().stream().sorted(Comparator.comparing(SysOfficeVO::getTreeSort)).collect(Collectors.toList());
            collect.forEach(ele -> {
                idNameMap.put(ele.getId(),ele.getOfficeName());
            });
        }
        return idNameMap;
    }

    @Override
    public List<QtCheckTaskDetailReportVO> defectiveDataFlat(QtChartParamDTO dto) {
        //查询符合条件的检验任务
        QtCheckTaskConditionDTO condition = QtCheckTaskConditionDTO.builder()
                .taskType(QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode())
                .startTime(LocalDateTime.of(dto.getDateStart(), LocalTime.of(8, 0, 0)))
                .endTime(LocalDateTime.of(dto.getDateEnd().plusDays(1), LocalTime.of(8, 0, 0)))
                .productWorkShopId(dto.getProductWorkShopId())
                .productLineId(dto.getProductLineId())
                .defectType(dto.getDefectType())
                .defectId(dto.getDefectId())
                .build();
        List<QtCheckTask> qtCheckTasks = qtCheckTaskService.conditionTaskList(condition);
        SteccnTableAssertUtil.notEmpty(qtCheckTasks,"缺陷类别分布暂无数据");
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
        SteccnTableAssertUtil.notEmpty(defectiveIdSet, "深加工缺陷项集合为空，暂无报表");

        //缺陷项类别与类别的创建时间
        Map<Integer, Integer> categoryMap = Maps.newHashMap();
        List<QtDefectiveItem> qtDefectiveItemList = qtDefectiveItemService.listByIds(defectiveIdSet);

        //缺陷项 -> 第一个快照缺陷id ， 第一个缺陷类别id【缺陷类别id】
        Set<String> defectCodeSet = qtDefectiveItemList.stream().map(QtDefectiveItem::getCode).collect(Collectors.toSet());
        Map<String,TupleDTO<Integer,Integer>> firstSnapshotData = qtDefectiveItemService.firstSnapshotData(defectCodeSet);
        Map<Integer, String> defectIdCodeMap = Maps.newHashMap();
        qtDefectiveItemList.forEach(v -> defectIdCodeMap.put(v.getId(), v.getCode()));

        SteccnTableAssertUtil.notEmpty(qtDefectiveItemList, "深加工缺陷集合为空，暂无报表");
        qtDefectiveItemList.forEach(ele -> categoryMap.put(ele.getId(), ele.getCategoryId()));
        //遍历缺陷项集合
        List<QtCheckTaskDetailReportVO> flatDefectiveList = Lists.newArrayList();
        for (QtCheckTaskDetail ele : detailList) {
            //获取检验任务，一定存在，因为明细是根据任务查询出来的
            QtCheckTask qtCheckTask = taskMap.get(ele.getTaskId());
            if (CollUtil.isNotEmpty(ele.getDefectNum())) {
                String numString = JSON.toJSONString(ele.getDefectNum());
                List<QtOnlineCheckDefectDTO> defectNumList = JSON.parseArray(numString, QtOnlineCheckDefectDTO.class);
                List<QtOnlineCheckDefectDTO> collect = defectNumList.stream().filter(v -> Objects.nonNull(v.getDefectId())).collect(Collectors.toList());
                for (QtOnlineCheckDefectDTO de : collect) {
                    if (Objects.nonNull(de.getDefectId()) && Objects.nonNull(de.getDefectNum()) && de.getDefectNum() > 0) {
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
                        vo.setClassesDate(qtCheckTask.getClassTime().toLocalDate());
                        vo.setDefectStartTime(qtCheckTask.getDefectStartTime());
                        vo.setDefectEndTime(qtCheckTask.getDefectEndTime());
                        //根据缺陷记录时段计算完成时间
                        QtTaskClassCalculate qtTaskClassCalculate = CalculateDateUtil.calculateClassTime(qtCheckTask.getDefectEndTime());
                        vo.setFinishedTime(qtCheckTask.getFinishedTime());
                        vo.setClassTime(qtTaskClassCalculate.getClassDate());
                        vo.setClassId(qtTaskClassCalculate.getClassType());
                        flatDefectiveList.add(vo);
                    }
                }
            }
        }
        //过滤班次信息
        if(Objects.nonNull(dto.getWorkingOrder())){
            flatDefectiveList = flatDefectiveList.stream().filter(v -> dto.getWorkingOrder().equals(v.getClassId())).collect(Collectors.toList());
        }
        //过滤缺陷项类别
        if(Objects.nonNull(dto.getDefectCategory())){
            flatDefectiveList = flatDefectiveList.stream().filter(v -> dto.getDefectCategory().equals(v.getDefectCategory())).collect(Collectors.toList());
        }
        //如果缺陷项不为空，则过滤缺陷项
        if (Objects.nonNull(dto.getDefectId())) {
            Map<Integer, Set<Integer>> defectSnapshot = qtDefectiveItemService.defectSnapshot(Sets.newHashSet(dto.getDefectId()));
            Set<Integer> dataFilter = Sets.newHashSet();
            for (Set<Integer> set : defectSnapshot.values()) {
                dataFilter.addAll(set);
            }
            flatDefectiveList = flatDefectiveList.stream().filter(v -> dataFilter.contains(v.getDefectId())).collect(Collectors.toList());
        }
        //替换快照数据
        flatDefectiveList.forEach(v -> {
            String code = defectIdCodeMap.get(v.getDefectId());
            TupleDTO<Integer, Integer> tupleDTO = firstSnapshotData.get(code);
            Assert.notNull(tupleDTO, "缺陷快照转换异常");
            Assert.notNull(tupleDTO.getField1(), "缺陷快照field1转换异常");
            Assert.notNull(tupleDTO.getField2(), "缺陷快照field2转换异常");
            v.setDefectId(tupleDTO.getField1());
            v.setDefectCategory(tupleDTO.getField2());
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
        //如果车间和产线为空，表示查询深加工车间所有数据
        flatDataList = workshopFilter(dto, flatDataList, QtManagerConstant.DEEP_OFFICE_NAME);
        SteccnTableAssertUtil.notEmpty(flatDataList, "数据为空，暂无图表");
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
        List<QtChartPieValueVO<Integer>> dataList = Lists.newArrayList();
        //遍历缺陷类别
        defectDataMap.forEach((k, v) -> {
            QtChartPieValueVO<Integer> vo = new QtChartPieValueVO();
            vo.setName(idNameMap.getOrDefault(k, Strings.EMPTY));
            int categorySum = v.stream().mapToInt(QtCheckTaskDetailReportVO::getDefectNum).sum();
            if(categorySum >0) {
                vo.setValue(categorySum);
                dataList.add(vo);
            }
        });
        SteccnTableAssertUtil.notEmpty(dataList,"暂无数据");
        dataList.sort(Comparator.comparingInt(QtChartPieValueVO<Integer>::getValue).reversed());
        qtChartPieVO.setData(dataList);
        return qtChartPieVO;
    }

    @Override
    public Page<DeepDefectiveCategoryDetailVO> deepDefectiveCategoryDetail(QtChartParamDTO dto) {

        List<DeepDefectiveCategoryDetailVO> pageList = getDeepDefectiveCategoryDetailList(dto);
        //排序+逻辑分页
        pageList.stream().sorted(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getClassesDate))
                .thenComparing(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getProductWorkshopId)))
                .thenComparing(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getProductLineId)))
                .thenComparing(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getClassId)))
                .thenComparing(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getCreateTime))));
        return LogicalPageUtil.page(pageList,dto.getCurrent(),dto.getSize());
    }

    @SneakyThrows
    @Override
    public void deepDefectiveCategoryDownload(QtChartParamDTO dto, HttpServletResponse response, Integer type) {
        List<?> dataList = new ArrayList<>();
        String modelName = null;
        String fileName = null;
        QtManagerConstant.QtChatDownloadEnum downloadType = QtManagerConstant.QtChatDownloadEnum.getByCode(type);
        if (Objects.isNull(downloadType)) {
            throw new BusinessException("下载类型不能为空");
        }
        try {
            switch (downloadType) {
                case SJGQXLB:
                    fileName = QtManagerConstant.QT_CHART_DEFECTIVE_CATEGORY_NAME;
                    modelName = QtManagerConstant.QT_CHART_EXCEL_MODEL_NAME_SJGQXLBFB;
                    dataList = getDeepDefectiveCategoryDetailList(dto);
                    break;
                case SJGQX:
                    fileName = QtManagerConstant.QT_CHART_DEFECTIVE_ITEM_NAME;
                    modelName = QtManagerConstant.QT_CHART_EXCEL_MODEL_NAME_SJGQXX;
                    dataList = getDeepDefectiveItemDetailList(dto);
                    break;
                case JBQXX:
                    fileName = QtManagerConstant.QT_CHART_SUBSTRATE_ITEM_NAME;
                    modelName = QtManagerConstant.QT_CHART_EXCEL_MODEL_NAME_JPQXX;
                    dataList = getSubstrateItemDetail(dto);
                    break;
                case SJGQXLBQS:
                    fileName = QtManagerConstant.QT_CHART_DEFECTIVE_CATEGORY_LINE_NAME;
                    modelName = QtManagerConstant.QT_CHART_EXCEL_MODEL_NAME_SJGQXLBQS;
                    dataList = getDeepDefectiveCategoryDetailList(dto);
                    break;
                case SJGQXQS:
                    fileName = QtManagerConstant.QT_CHART_DEEP_ITEM_LINE_NAME;
                    modelName = QtManagerConstant.QT_CHART_EXCEL_MODEL_NAME_SJGQXXQS;
                    dataList = getDeepDefectiveItemDetailList(dto);
                    break;
                case JBQXXQS:
                    fileName = QtManagerConstant.QT_CHART_SUBSTRATE_ITEM_LINE_NAME;
                    modelName = QtManagerConstant.QT_CHART_EXCEL_MODEL_NAME_JQQXXQS;
                    dataList = getSubstrateItemDetail(dto);
                    break;
                default:
                    throw new BusinessException("下载类型不存在");
            }
        } catch (SteccnChartException e) {
            dataList = Lists.newArrayList();
        }
        //获取参数map
        Map<String, Object> map = getMap(dto);

        DownloadUtil.downloadTemplate(response,modelName,fileName,map,dataList,dto.getBase64());

    }

    private Map<String, Object> getMap(QtChartParamDTO dto) {
        Map<String, Object> map = MapUtils.newHashMap();
        map.put("compare","");
        map.put("productWorkShopName","");
        map.put("productLineName","");
        map.put("defectCategory","");
        map.put("defect","");
        map.put("workingOrder","");
        map.put("dateStart",LocalDateUtil.localDateFormat(dto.getDateStart(),DatePattern.NORM_DATE_PATTERN));

        if (Objects.nonNull(dto.getCompare())) {
            map.put("compare",QtManagerConstant.QtChartCompareEnum.getNameByCode(dto.getCompare()));
        }
        if (Objects.nonNull(dto.getProductWorkShopId())) {
            String workshopName = remoteOfficeFeign.getNameById(dto.getProductWorkShopId()).getData();
            map.put("productWorkShopName",workshopName);
        }
        if (Objects.nonNull(dto.getProductLineId())) {
            String productLineName = remoteOfficeFeign.getNameById(dto.getProductLineId()).getData();
            map.put("productLineName",productLineName);
        }
        if (Objects.nonNull(dto.getDefectCategory())) {
            List<QtItemCategoryVO> categoryList = qtItemCategoryService.getCategoryList(1);
            Optional<QtItemCategoryVO> optional = categoryList.stream().filter(qtItemCategoryVO -> qtItemCategoryVO.getId().equals(dto.getDefectCategory())).findAny();
            optional.ifPresent(qtItemCategoryVO -> map.put("defectCategory", qtItemCategoryVO.getName()));
        }
        if (Objects.nonNull(dto.getDefectId())) {
            Map<Integer, String> byIdCh = qtDefectiveItemService.listByIdCh(Collections.singleton(dto.getDefectId()));
            if (!byIdCh.isEmpty()) {
                map.put("defect",byIdCh.get(dto.getDefectId()));
            }
        }
        if (Objects.nonNull(dto.getWorkingOrder())) {
            if (dto.getWorkingOrder().equals(0)) {
                map.put("workingOrder","早班");
            } else if (dto.getWorkingOrder().equals(1)) {
                map.put("workingOrder","夜班");
            } else {
                map.put("workingOrder","全部");
            }
        }

        return map;
    }

    private List<DeepDefectiveCategoryDetailVO> getDeepDefectiveCategoryDetailList(QtChartParamDTO dto) {
        //扁平化数据
        List<QtCheckTaskDetailReportVO> flatDataList = defectiveDataFlat(dto);
        //如果车间和产线为空，表示查询深加工车间所有数据
        flatDataList = workshopFilter(dto, flatDataList, QtManagerConstant.DEEP_OFFICE_NAME);
        SteccnTableAssertUtil.notEmpty(flatDataList, "数据为空，暂无报表");
        Set<Integer> officeIdSet = Sets.newHashSet();
        Set<Integer> categoryIdSet = Sets.newHashSet();
        Set<Integer> defectIdSet = Sets.newHashSet();
        flatDataList.forEach(ele -> {
            defectIdSet.add(ele.getDefectId());
            categoryIdSet.add(ele.getDefectCategory());
            officeIdSet.add(ele.getProductWorkshopId());
            officeIdSet.add(ele.getProductLineId());
        });
        Map<Integer, String> officeChMap = FeignOptional.of(remoteOfficeFeign.listByIdCh(officeIdSet)).get();
        Map<Integer, String> defectIdNameMap = qtDefectiveItemService.listByIdCh(defectIdSet);
        Map<Integer, QtItemCategory> defectCategoryMap = qtItemCategoryService.listById(categoryIdSet);
        QtItemCategory defaultQtItemCategory = new QtItemCategory();

        List<DeepDefectiveCategoryDetailVO> pageList = detailCategoryAndItem(flatDataList, officeChMap, defectIdNameMap, defectCategoryMap, lineData -> lineData.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getDefectCategory)), (defectId, countVo) -> {
            QtItemCategory qtItemCategory = defectCategoryMap.getOrDefault(defectId, defaultQtItemCategory);
            countVo.setName(qtItemCategory.getName());
            countVo.setCreateTime(qtItemCategory.getCreateTime());
        });
        pageList = legendFilter(dto, pageList);

        return pageList;
    }


    /**
     * 缺陷类别或者缺陷项的项
     *
     * @param flatDataList      数据
     * @param officeChMap       机构信息
     * @param defectIdNameMap   缺陷项id信息
     * @param defectCategoryMap 缺陷类别信息
     * @param function          函数
     * @param consumer          消费者
     * @return
     */
    private List<DeepDefectiveCategoryDetailVO> detailCategoryAndItem(List<QtCheckTaskDetailReportVO> flatDataList, Map<Integer, String> officeChMap, Map<Integer, String> defectIdNameMap, Map<Integer, QtItemCategory> defectCategoryMap, Function<List<QtCheckTaskDetailReportVO>, Map<Integer, List<QtCheckTaskDetailReportVO>>> function, BiConsumer<Integer, DeepDefectiveCategoryDetailVO> consumer) {
        //定义数据集合
        List<DeepDefectiveCategoryDetailVO> pageList = Lists.newArrayList();
        QtItemCategory defaultQtItemCategory = new QtItemCategory();
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
                        Map<Integer, List<QtCheckTaskDetailReportVO>> defectMap = function.apply(lineData);
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
                            countVo.setId(defectId);
                            consumer.accept(defectId, countVo);
                            //明细
                            List<DeepDefectiveCategoryDetailListVO> detailList = Lists.newArrayList();
                            defectData.forEach(ele -> {
                                DeepDefectiveCategoryDetailListVO listVO = new DeepDefectiveCategoryDetailListVO();
                                BeanUtils.copyProperties(ele, listVO);
                                listVO.setDefectName(defectIdNameMap.getOrDefault(ele.getDefectId(), Strings.EMPTY));
                                listVO.setResultSource("人工检验");
                                listVO.setDefectCategoryName(defectCategoryMap.getOrDefault(ele.getDefectCategory(), defaultQtItemCategory).getName());
                                detailList.add(listVO);
                            });
                            countVo.setDetailList(detailList);
                            pageList.add(countVo);
                        });
                    });
                });
            });
        });
        return pageList;
    }


    @Override
    public QtLineChartBarDTO deepDefectiveItemBar(QtChartParamDTO dto) {
        //扁平化数据
        List<QtCheckTaskDetailReportVO> flatDataList = defectiveDataFlat(dto);
        //过滤车间
        flatDataList = workshopFilter(dto, flatDataList, QtManagerConstant.DEEP_OFFICE_NAME);
        SteccnTableAssertUtil.notEmpty(flatDataList, "数据为空，暂无报表");
        //对扁平化数据获取类别
        Set<Integer> defectIdSet = flatDataList.stream().map(QtCheckTaskDetailReportVO::getDefectId).collect(Collectors.toSet());
        Map<Integer, String> idNameMap = qtDefectiveItemService.listByIdCh(defectIdSet);
        return compareCategoryAndItem(dto.getCompare(), flatDataList, idNameMap, (tDataList, key) -> tDataList.stream().filter(v -> v.getDefectId().equals(key)).collect(Collectors.toList()));
    }

    /**
     * 根据深加工或基片来获取产线
     * @param keyword 深加工 || 基片
     * @return 产线集合
     */
    private Set<Integer> conditionProduceLine(String keyword){
        SysOfficeDTO sysOfficeDTO = new SysOfficeDTO();
        sysOfficeDTO.setOfficeName(keyword);
        sysOfficeDTO.setOfficeType(QtManagerConstant.OFFICE_PRODUCE_LINE_TYPE);
        List<SysOfficeVO> sysOfficeVOList = FeignOptional.of(remoteOfficeFeign.getByTypeAndLine(sysOfficeDTO)).get();
        Assert.notEmpty(sysOfficeVOList,"产线集合不能为空");
        return sysOfficeVOList.stream().map(SysOfficeVO::getId).collect(Collectors.toSet());
    }

    /**
     * 基片缺陷分布-柱状图
     *
     * @param dto
     * @return
     */
    @Override
    public QtLineChartBarDTO substrateItemBar(QtChartParamDTO dto) {
        List<QtCheckTaskDetailReportVO> flatDataList = defectiveDataFlat(dto);
        //过滤车间
        flatDataList = workshopFilter(dto, flatDataList, QtManagerConstant.SUB_OFFICE_NAME);
        SteccnTableAssertUtil.notEmpty(flatDataList, "数据为空，暂无明细");

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
        //深加工车间
        flatDataList = workshopFilter(dto, flatDataList, QtManagerConstant.DEEP_OFFICE_NAME);
        SteccnTableAssertUtil.notEmpty(flatDataList, "数据为空，暂无图表");
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
    public QtChartPieVO substrateItemPie(QtChartParamDTO dto) {
        //扁平化数据
        List<QtCheckTaskDetailReportVO> flatDataList = defectiveDataFlat(dto);
        //过滤车间
        flatDataList = workshopFilter(dto, flatDataList, QtManagerConstant.SUB_OFFICE_NAME);
        SteccnTableAssertUtil.notEmpty(flatDataList, "数据为空，暂无图表");
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
    public Page<DeepDefectiveCategoryDetailVO> substrateItemDetail(QtChartParamDTO dto) {

        List<DeepDefectiveCategoryDetailVO> pageList = getSubstrateItemDetail(dto);
        //排序+逻辑分页
        pageList.stream().sorted(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getClassesDate))
                .thenComparing(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getProductWorkshopId)))
                .thenComparing(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getProductLineId)))
                .thenComparing(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getClassId)))
                .thenComparing(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getCreateTime))));
        return LogicalPageUtil.page(pageList,dto.getCurrent(),dto.getSize());
    }

    private List<DeepDefectiveCategoryDetailVO> getSubstrateItemDetail(QtChartParamDTO dto) {
        //扁平化数据
        List<QtCheckTaskDetailReportVO> flatDataList = defectiveDataFlat(dto);
        //过滤车间
        flatDataList = workshopFilter(dto, flatDataList, QtManagerConstant.SUB_OFFICE_NAME);
        SteccnTableAssertUtil.notEmpty(flatDataList, "数据为空，暂无报表");
        Set<Integer> officeIdSet = Sets.newHashSet();
        Set<Integer> categoryIdSet = Sets.newHashSet();
        Set<Integer> defectIdSet = Sets.newHashSet();
        flatDataList.forEach(ele -> {
            defectIdSet.add(ele.getDefectId());
            categoryIdSet.add(ele.getDefectCategory());
            officeIdSet.add(ele.getProductWorkshopId());
            officeIdSet.add(ele.getProductLineId());
        });
        Map<Integer, String> officeChMap = FeignOptional.of(remoteOfficeFeign.listByIdCh(officeIdSet)).get();
        Map<Integer, QtDefectiveItem> defectIdMap = qtDefectiveItemService.listById(defectIdSet);
        Map<Integer, String> defectIdNameMap = qtDefectiveItemService.listByIdCh(defectIdSet);
        QtDefectiveItem defaultQtDefectiveItem = new QtDefectiveItem();
        Map<Integer, QtItemCategory> defectCategoryMap = qtItemCategoryService.listById(categoryIdSet);
        List<DeepDefectiveCategoryDetailVO> pageList = detailCategoryAndItem(flatDataList, officeChMap, defectIdNameMap, defectCategoryMap, lineData -> lineData.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getDefectId)), (defectId, countVo) -> {
            QtDefectiveItem item = defectIdMap.getOrDefault(defectId, defaultQtDefectiveItem);
            countVo.setName(item.getName());
            countVo.setCreateTime(item.getCreateTime());
        });
        //图例过滤
        pageList = legendFilter(dto, pageList);

        return pageList;
    }

    @Override
    public QtLineChartBarDTO deepDefectiveCategoryLine(QtChartParamDTO dto) {
        //获取扁平化数据
        List<QtCheckTaskDetailReportVO> flatDataList = defectiveDataFlat(dto);
        //过滤车间
        flatDataList = workshopFilter(dto, flatDataList, QtManagerConstant.DEEP_OFFICE_NAME);
        SteccnTableAssertUtil.notEmpty(flatDataList, "数据为空，暂无报表");
        //获取粒度枚举
        QtManagerConstant.QtGranularityEnum granularityEnum = QtManagerConstant.QtGranularityEnum.getByName(dto.getGranularity());
        Assert.notNull(granularityEnum, "粒度不存在");
        //将时间范围根据粒度分片
        List<LocalDateTime> cutDate = ShardTimeUtil.cutDateRange(granularityEnum.getType()
                , LocalDateTime.of(dto.getDateStart(), LocalTime.MIN)
                , LocalDateTime.of(dto.getDateEnd(), LocalTime.MAX));
        Assert.notEmpty(cutDate, "时间范围根据粒度分片为空");
        Set<Integer> categoryIdSet = flatDataList.stream().map(QtCheckTaskDetailReportVO::getDefectCategory).collect(Collectors.toSet());
        Map<Integer, QtItemCategory> defectMap = qtItemCategoryService.listById(categoryIdSet);
        Map<Integer, String> idNameMap = defectCategorySort(defectMap);
        return commonLine(0,dto, flatDataList, cutDate, idNameMap, dto1 -> Objects.nonNull(dto1.getDefectCategory()));
    }

    @Override
    public List<QtCheckTaskDetailReportVO> defectCollect(QtChartParamDTO dto) {
        Integer productLineId = dto.getProductLineId();
        LocalDate dateStart = dto.getDateStart();
        LocalDate dateEnd = dto.getDateEnd();
        Assert.notNull(productLineId,"产线不能为空");
        Assert.notNull(dateStart,"开始日期不能为空");
        Assert.notNull(dateEnd,"结束日期不能为空");
        Assert.notNull(dto.getCheckType(), "检验类型不能为空");
        //todo 后面需要考虑只传车间 ，不传入产线
        List<QtCheckTaskDetailReportVO> convertDefectCollect = Lists.newArrayList();
        //查询检验设备数据
        Map<String, String> refMap = FeignOptional.of(() -> remoteSysConfigurationFeignApi.sysConfRef(new TupleDTO<>(SysOfficeConstant.HBBH_DEFECTIVE_SAMPLE_LINE, Sets.newHashSet(productLineId))), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        if (CollUtil.isNotEmpty(refMap)) {
            //获取到所有的映射产线
            Set<String> lineSet = refMap.keySet();
            List<DwDefectCollectVO> collectList = FeignOptional.of(() -> remoteDataPlatformFeignApi.collectDetail(new TripleDTO<>(LocalDateTime.of(dateStart, LocalTime.of(8, 0, 0)), LocalDateTime.of(dateEnd.plusDays(1), LocalTime.of(8, 0, 0)), lineSet)), FeignConstant.STECCN_DATA_PLATFORM_SERVICE_NAME).get();
            if(CollUtil.isNotEmpty(collectList)){
                //获取采集的缺陷集合
                convertDefectCollect = convertDefectCollect(collectList, refMap);
            }
        }
        return convertDefectCollect;
    }

    /**
     * 计算机器采集的缺陷
     *
     * @param collectList 采集缺陷集合
     * @param refMap 产线映射
     * @return list
     */
    private List<QtCheckTaskDetailReportVO> convertDefectCollect(List<DwDefectCollectVO> collectList,  Map<String, String> refMap) {
        List<QtCheckTaskDetailReportVO> resList = Lists.newArrayList();

        if (CollUtil.isNotEmpty(collectList) && CollUtil.isNotEmpty(refMap)) {
            Set<String> defectTypeSet = collectList.stream().map(DwDefectCollectVO::getDefectType).collect(Collectors.toSet());
            Map<String, QtDefectiveItem> resMap = qtDefectiveItemService.collectDefect(defectTypeSet);

            collectList.forEach(ele -> {
                QtCheckTaskDetailReportVO vo = new QtCheckTaskDetailReportVO();
                vo.setDataSource(1);
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
                try {
                    String line = refMap.get(ele.getLine());
                    vo.setProductLineId(Integer.parseInt(line));
                    QtTaskClassCalculate qtTaskClassCalculate = CalculateDateUtil.calculateClassTime(vo.getDefectEndTime());
                    vo.setClassTime(qtTaskClassCalculate.getClassDate());
                    vo.setClassId(qtTaskClassCalculate.getClassType());
                    vo.setDefectNum(ele.getDefectCount());
                    //配置缺陷项
                    QtDefectiveItem qtDefectiveItem = resMap.get(ele.getDefectType());
                    if(Objects.nonNull(qtDefectiveItem)){
                        vo.setDefectId(qtDefectiveItem.getId());
                        vo.setDefectCategory(qtDefectiveItem.getCategoryId());
                        resList.add(vo);
                    }
                }catch(Exception e){

                }
            });
        }
        return resList;
    }

    /**
     * 缺陷类别排序
     * @param defectMap 缺陷类别map
     * @return
     */
    private Map<Integer, String> defectCategorySort(Map<Integer, QtItemCategory> defectMap) {
        Map<Integer, String> idNameMap = new LinkedHashMap<>();
        if (CollUtil.isNotEmpty(defectMap)) {
            //排序：按缺陷项所属缺陷项类别的创建时间升序排序
            List<QtItemCategory> collect = defectMap.values().stream().sorted(Comparator.comparing(QtItemCategory::getId)).collect(Collectors.toList());
            for (QtItemCategory ele : collect) {
                idNameMap.put(ele.getId(), ele.getName());
            }
        }
        return idNameMap;
    }

    /**
     * 车间产线过滤
     * @param dto dto
     * @param flatDataList 扁平数据 待过滤
     * @param officeName 车间名称前缀  深加工 || 基片
     * @return 过滤结果
     */
    private List<QtCheckTaskDetailReportVO> workshopFilter(QtChartParamDTO dto, List<QtCheckTaskDetailReportVO> flatDataList, String officeName) {
        if (Objects.isNull(dto.getProductWorkShopId()) && Objects.isNull(dto.getProductLineId())) {
            //默认给深加工产线
            Set<Integer> produceLineSet = conditionProduceLine(officeName);
            flatDataList = flatDataList.stream().filter(v -> produceLineSet.contains(v.getProductLineId())).collect(Collectors.toList());
        }
        return flatDataList;
    }

    /**
     * 求和，返回二元组数据
     *
     * @param tDataList 原始数据
     * @param key       元素显示
     * @return
     */
    private TupleDTO<String, Integer> getTupleDTO(List<QtCheckTaskDetailReportVO> tDataList, String key) {
        int categorySum = tDataList.stream().mapToInt(QtCheckTaskDetailReportVO::getDefectNum).sum();
        return new TupleDTO<>(key, categorySum);
    }

    /**
     * 基片缺陷项趋势-折线图
     *
     * @param dto
     * @return
     */
    @Override
    public QtLineChartBarDTO substrateItemLine(QtChartParamDTO dto) {
        //获取扁平化数据
        List<QtCheckTaskDetailReportVO> flatDataList = defectiveDataFlat(dto);
        //过滤车间
        flatDataList = workshopFilter(dto, flatDataList, QtManagerConstant.SUB_OFFICE_NAME);
        SteccnTableAssertUtil.notEmpty(flatDataList, "数据为空，暂无报表");
        //获取粒度枚举
        QtManagerConstant.QtGranularityEnum granularityEnum = QtManagerConstant.QtGranularityEnum.getByName(dto.getGranularity());
        Assert.notNull(granularityEnum, "粒度不存在");
        //将时间范围根据粒度分片
        List<LocalDateTime> cutDate = ShardTimeUtil.cutDateRange(granularityEnum.getType()
                , LocalDateTime.of(dto.getDateStart(), LocalTime.MIN)
                , LocalDateTime.of(dto.getDateEnd(), LocalTime.MAX));
        Assert.notEmpty(cutDate, "时间范围根据粒度分片为空");

        Set<Integer> defectIdSet = flatDataList.stream().map(QtCheckTaskDetailReportVO::getDefectId).collect(Collectors.toSet());
        Map<Integer, QtDefectiveItem> defectMap = qtDefectiveItemService.listById(defectIdSet);
        Map<Integer, String> defectIdNameMap = new LinkedHashMap<>();
        //保证排序关系
        //①一级排序：按缺陷项所属缺陷项类别的创建时间升序排序
        //②二级排序：按缺陷项的创建时间升序排序
        List<QtDefectiveItem> collect = defectMap.values().stream().sorted(Comparator.comparing(QtDefectiveItem::getCategoryId).thenComparing(QtDefectiveItem::getId)).collect(Collectors.toList());
        for (QtDefectiveItem ele : collect) {
          defectIdNameMap.put(ele.getId(),ele.getName());
        }
        return commonLine(1, dto, flatDataList, cutDate, defectIdNameMap, dto1 -> Objects.nonNull(dto1.getDefectId()));
    }

    /**
     * 深加工缺陷项
     *
     * @param dto
     * @return
     */
    @Override
    public QtLineChartBarDTO deepItemLine(QtChartParamDTO dto) {
        //获取扁平化数据
        List<QtCheckTaskDetailReportVO> flatDataList = defectiveDataFlat(dto);
        //过滤车间
        flatDataList = workshopFilter(dto, flatDataList, QtManagerConstant.DEEP_OFFICE_NAME);
        SteccnTableAssertUtil.notEmpty(flatDataList, "数据为空，暂无报表");
        //获取粒度枚举
        QtManagerConstant.QtGranularityEnum granularityEnum = QtManagerConstant.QtGranularityEnum.getByName(dto.getGranularity());
        Assert.notNull(granularityEnum, "粒度不存在");
        //将时间范围根据粒度分片
        List<LocalDateTime> cutDate = ShardTimeUtil.cutDateRange(granularityEnum.getType()
                , LocalDateTime.of(dto.getDateStart(), LocalTime.MIN)
                , LocalDateTime.of(dto.getDateEnd(), LocalTime.MAX));
        Assert.notEmpty(cutDate, "时间范围根据粒度分片为空");

        Set<Integer> defectIdSet = flatDataList.stream().map(QtCheckTaskDetailReportVO::getDefectId).collect(Collectors.toSet());
        Map<Integer, QtDefectiveItem> defectMap = qtDefectiveItemService.listById(defectIdSet);
        Map<Integer, String> defectIdNameMap = new LinkedHashMap<>();
        //保证排序关系
        //①一级排序：按缺陷项所属缺陷项类别的创建时间升序排序
        //②二级排序：按缺陷项的创建时间升序排序
        List<QtDefectiveItem> collect = defectMap.values().stream().sorted(Comparator.comparing(QtDefectiveItem::getCategoryId).thenComparing(QtDefectiveItem::getId)).collect(Collectors.toList());
        for (QtDefectiveItem ele : collect) {
            defectIdNameMap.put(ele.getId(),ele.getName());
        }
        return commonLine(1,dto, flatDataList, cutDate, defectIdNameMap, dto1 -> Objects.nonNull(dto1.getDefectId()));
    }

    /**
     * 公共折线图
     *
     * @param type         0表示类别  1表示缺陷项
     * @param dto          dto条件
     * @param flatDataList 平铺数据
     * @param cutDate      当前日期分片
     * @param idNameMap    id->name
     * @param predicate    断言
     * @return 折线图对象
     */
    private QtLineChartBarDTO commonLine(Integer type,QtChartParamDTO dto, List<QtCheckTaskDetailReportVO> flatDataList, List<LocalDateTime> cutDate, Map<Integer, String> idNameMap, Predicate<QtChartParamDTO> predicate) {
        //定义数据集合
        List<StackGraphDTO<String, Integer>> stackGraphDTOList = Lists.newArrayList();

        //对比情况下，获取对比的维度
        Map<Integer, String> xMap = Maps.newHashMap();
        if (predicate.test(dto) && Objects.nonNull(dto.getCompare())) {
            //对比班次，车间，产线
            TupleDTO<Map<Integer, List<QtCheckTaskDetailReportVO>>, Map<Integer, String>> mapMapTupleDTO = commonGroup(dto.getCompare(), flatDataList);
            xMap = mapMapTupleDTO.getField2();
        }

        for (int i = 1; i < cutDate.size(); i++) {
            LocalDateTime start = cutDate.get(i - 1);
            LocalDateTime end = cutDate.get(i);
            List<QtCheckTaskDetailReportVO> conditionDataList = flatDataList.stream().filter(v -> {
                //数据范围应该是大于等于开始时间  小于结束时间
                if ((start.isBefore(v.getClassTime()) || start.isEqual(v.getClassTime())) && end.isAfter(v.getClassTime())) {
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }).collect(Collectors.toList());
            StackGraphDTO<String, Integer> stackGraphDTO = new StackGraphDTO<>();
            QtManagerConstant.QtGranularityEnum granularityEnum = QtManagerConstant.QtGranularityEnum.getByName(dto.getGranularity());
            stackGraphDTO.setXAxis(XAxisUtil.convert(granularityEnum, start));
            List<TupleDTO<String, Integer>> data = Lists.newArrayList();
            //类别
            List<QtCheckTaskDetailReportVO> defaultReportVoList = Lists.newArrayList();
            if (predicate.test(dto) && Objects.nonNull(dto.getCompare())) {
                TupleDTO<Map<Integer, List<QtCheckTaskDetailReportVO>>, Map<Integer, String>> mapMapTupleDTO = commonGroup(dto.getCompare(), conditionDataList);
                Map<Integer, List<QtCheckTaskDetailReportVO>> dataMap = mapMapTupleDTO.getField1();
                for (Map.Entry<Integer, String> xEntry : xMap.entrySet()) {
                    List<QtCheckTaskDetailReportVO> tDataList = dataMap.getOrDefault(xEntry.getKey(), defaultReportVoList);
                    data.add(getTupleDTO(tDataList, xEntry.getValue()));
                }
            } else {
                for (Map.Entry<Integer, String> entry : idNameMap.entrySet()) {
                    List<QtCheckTaskDetailReportVO> tmp;
                    if(type == 0){
                        tmp = conditionDataList.stream().filter(v -> entry.getKey().equals(v.getDefectCategory())).collect(Collectors.toList());
                    }else{
                        tmp = conditionDataList.stream().filter(v -> entry.getKey().equals(v.getDefectId())).collect(Collectors.toList());
                    }
                    data.add(getTupleDTO(tmp, entry.getValue()));
                }
            }
            stackGraphDTO.setData(data);
            stackGraphDTOList.add(stackGraphDTO);
        }
        //自定义转换工具
        return StackGraphUtil.convertData(stackGraphDTOList);
    }

    /**
     * 深加工缺陷分布明细
     *
     * @param dto
     * @return
     */
    @Override
    public Page<DeepDefectiveCategoryDetailVO> deepDefectiveItemDetail(QtChartParamDTO dto) {

        List<DeepDefectiveCategoryDetailVO> pageList = getDeepDefectiveItemDetailList(dto);
        //排序+逻辑分页
        pageList.stream().sorted(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getClassesDate))
                .thenComparing(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getProductWorkshopId)))
                .thenComparing(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getProductLineId)))
                .thenComparing(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getClassId)))
                .thenComparing(Comparator.nullsLast(Comparator.comparing(DeepDefectiveCategoryDetailVO::getCreateTime))));
        return LogicalPageUtil.page(pageList,dto.getCurrent(),dto.getSize());
    }

    private List<DeepDefectiveCategoryDetailVO> getDeepDefectiveItemDetailList(QtChartParamDTO dto) {
        //扁平化数据
        List<QtCheckTaskDetailReportVO> flatDataList = defectiveDataFlat(dto);
        //过滤车间
        flatDataList = workshopFilter(dto, flatDataList, QtManagerConstant.DEEP_OFFICE_NAME);
        SteccnTableAssertUtil.notEmpty(flatDataList, "数据为空，暂无报表");
        Set<Integer> officeIdSet = Sets.newHashSet();
        Set<Integer> categoryIdSet = Sets.newHashSet();
        Set<Integer> defectIdSet = Sets.newHashSet();
        flatDataList.forEach(ele -> {
            defectIdSet.add(ele.getDefectId());
            categoryIdSet.add(ele.getDefectCategory());
            officeIdSet.add(ele.getProductWorkshopId());
            officeIdSet.add(ele.getProductLineId());
        });
        Map<Integer, String> officeChMap = FeignOptional.of(remoteOfficeFeign.listByIdCh(officeIdSet)).get();
        Map<Integer, QtDefectiveItem> defectIdMap = qtDefectiveItemService.listById(defectIdSet);
        Map<Integer, String> defectIdNameMap = qtDefectiveItemService.listByIdCh(defectIdSet);
        QtDefectiveItem defaultQtDefectiveItem = new QtDefectiveItem();
        Map<Integer, QtItemCategory> defectCategoryMap = qtItemCategoryService.listById(categoryIdSet);
        List<DeepDefectiveCategoryDetailVO> pageList = detailCategoryAndItem(flatDataList, officeChMap, defectIdNameMap, defectCategoryMap, lineData -> lineData.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getDefectId)), (defectId, countVo) -> {
            QtDefectiveItem item = defectIdMap.getOrDefault(defectId, defaultQtDefectiveItem);
            countVo.setName(item.getName());
            countVo.setCreateTime(item.getCreateTime());
        });
        //图例过滤
        pageList = legendFilter(dto, pageList);

        return pageList;
    }

    /**
     * 图例过滤
     * @param dto
     * @param pageList
     * @return
     */
    private List<DeepDefectiveCategoryDetailVO> legendFilter(QtChartParamDTO dto, List<DeepDefectiveCategoryDetailVO> pageList) {
        //对比模式下考虑到图例的过滤
        if (QtManagerConstant.QtChartCompareEnum.WORKSHOP.getCode().equals(dto.getCompare())) {
            pageList = pageList.stream().filter(v -> CollUtil.isNotEmpty(dto.getFilters()) && dto.getFilters().contains(v.getProductWorkshopName())).collect(Collectors.toList());
        } else if (QtManagerConstant.QtChartCompareEnum.PRODUCTLINE.getCode().equals(dto.getCompare())) {
            pageList = pageList.stream().filter(v -> CollUtil.isNotEmpty(dto.getFilters()) && dto.getFilters().contains(v.getProductLineName())).collect(Collectors.toList());
        } else if (QtManagerConstant.QtChartCompareEnum.WORK.getCode().equals(dto.getCompare())) {
            pageList = pageList.stream().filter(v -> CollUtil.isNotEmpty(dto.getFilters()) && dto.getFilters().contains(v.getClassesName())).collect(Collectors.toList());
        }else if(CollUtil.isNotEmpty(dto.getGuaFilters())){
            pageList = pageList.stream().filter(v -> dto.getGuaFilters().contains(v.getName())).collect(Collectors.toList());
        }
        pageList.forEach(detailVO -> detailVO.setClassesDateString(LocalDateUtil.localDateFormat(detailVO.getClassesDate(),DatePattern.NORM_DATE_PATTERN)));
        return pageList;
    }
}
