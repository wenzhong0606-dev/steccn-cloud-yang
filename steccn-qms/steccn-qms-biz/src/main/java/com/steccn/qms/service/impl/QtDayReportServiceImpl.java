package com.steccn.qms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.constant.SysDictConstant;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.ClassUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.util.MapUtil;
import com.steccn.oa.constant.OaConstant;
import com.steccn.oa.constant.OaManagerConstant;
import com.steccn.oa.dto.OaGroupCountDTO;
import com.steccn.oa.feign.OaInfoService;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.feign.RemoteSysConfigurationFeignApi;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.vo.BaseReportHeaderVO;
import com.steccn.pms.vo.FurtherValueStrVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.*;
import com.steccn.qms.dto.*;
import com.steccn.qms.excel.strategy.ExcelFillCellMergeStrategy;
import com.steccn.qms.excel.strategy.HeadContentStyleStrategy;
import com.steccn.qms.excel.strategy.RowHeightStyleStrategy;
import com.steccn.qms.mapper.QtCheckTaskMapper;
import com.steccn.qms.service.*;
import com.steccn.qms.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
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
    private final RemoteSysConfigurationFeignApi remoteSysConfigurationFeignApi;
    private final QtCheckTaskDestoryInfoService qtCheckTaskDestoryInfoService;
    private final QtChartGenerateService qtChartGenerateService;
    private final QtCheckTaskMapper qtCheckTaskMapper;
    private final QtCheckTaskDetailService qtCheckTaskDetailService;
    private final QtCheckItemService qtCheckItemService;
    private final QtItemCategoryService qtItemCategoryService;
    private final RemoteDictFeignApi remoteDictFeignApi;
    private final OaInfoService oaInfoService;

    @Override
    public QtDayReportOnlineVO online(LocalDate date, List<Integer> productLineId, String source) {
        QtDayReportOnlineVO resVo = new QtDayReportOnlineVO();
        List<QtDayReportOnlineDataVO> tableData = Lists.newArrayList();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("HH:mm");
        //查询人工数据
        //获取所有扁平化数据
        QtChartParamDTO dto = new QtChartParamDTO();
        dto.setDateStart(date);
        dto.setDateEnd(date);
        dto.setProductLineIdList(productLineId);
        //获取人工采集的缺陷集合+获取机器采集的缺陷集合
        dto.setCheckType(QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode());

        //是否加入数采数据
        Map<String, List<Integer>> cfMap = FeignOptional.of(() -> remoteSysConfigurationFeignApi.confListInteger(new TripleDTO<>(QtManagerConstant.CONF_QMS_COLLECT_ENABLE, Sets.newHashSet(source), 2)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<Integer> cfContentList = cfMap.get(QtManagerConstant.CONF_QMS_COLLECT_ENABLE_CONTENT);
        if (CollUtil.isNotEmpty(cfContentList) && cfContentList.get(0) == 1) {
            dto.setJoinAcquisition(1);
        }
        List<QtCheckTaskDetailReportVO> manData = qtDefectiveReportService.defectiveDataFlat(dto);
        List<QtCheckTaskDetailReportVO> manDataList = manData;
        SteccnTableAssertUtil.notEmpty(manDataList, "数据为空");
        //查询物料
        Set<Long> materialIdSet = manDataList.stream().map(QtCheckTaskDetailReportVO::getMaterialId).collect(Collectors.toSet());
        Map<Long, String> materialIdNameMap = FeignOptional.of(() -> remoteSpareFeignApi.listByIdCh(materialIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Map<Integer, SysOfficeVO> sysOfficeVOMap = FeignOptional.of(() -> remoteOfficeFeign.listById(Sets.newHashSet(productLineId)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

        //做缺陷类别+缺陷分组--根据创建时间排序，依赖于defectCode
        Map<Integer, List<QtDefectiveItem>> categoryDefectMap = Maps.newHashMap();
        //根据缺陷code获取缺陷
        Set<String> defectCodeSet = manDataList.stream().map(QtCheckTaskDetailReportVO::getDefectCode).collect(Collectors.toSet());
        //根据缺陷code获取缺陷信息
        Map<String, QtDefectiveItem> defectCodeMap = qtDefectiveItemService.listByCodeSort(defectCodeSet);
        for (QtDefectiveItem ele : defectCodeMap.values()) {
            MapUtil.compute(categoryDefectMap, ele.getCategoryId(), ele);
        }
        //构建module
        List<BaseReportHeaderVO> resModule = moduleHead(categoryDefectMap);
        TupleDTO<LocalDate, LocalDate> dateDTO = new TupleDTO<>(date, date);
        //根据时间获取班组
        Map<String, OaGroupCountDTO> groupInfoMap = FeignOptional.of(() -> oaInfoService.getGroupInfoJp(dateDTO), FeignConstant.STECCN_OA_SERVICE_NAME).get();
        OaGroupCountDTO defaultOaGroupCountDTO = new OaGroupCountDTO();

        //总计
        Map<String, BigDecimal> allMap = Maps.newHashMap();
        //基于产线去分组
        Map<Integer, List<QtCheckTaskDetailReportVO>> lineEntrySet = manDataList.stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getProductLineId));
        for (Map.Entry<Integer, List<QtCheckTaskDetailReportVO>> lineEntry : lineEntrySet.entrySet()) {
            SysOfficeVO sysOfficeVO = sysOfficeVOMap.getOrDefault(lineEntry.getKey(), new SysOfficeVO());
            String productLineName = sysOfficeVO.getOfficeName();
            Integer treeSort = sysOfficeVO.getTreeSort();
            //根据来源分组
            Map<String, BigDecimal> countMap = Maps.newHashMap();
            Map<Integer, List<QtCheckTaskDetailReportVO>> classMap = lineEntry.getValue().stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getClassId));
            for (Map.Entry<Integer, List<QtCheckTaskDetailReportVO>> classEntry : classMap.entrySet()) {
                String name = QtManagerConstant.QtClassesEnum.code(classEntry.getKey()).getName();
                OaGroupCountDTO oaGroupCountDTO = groupInfoMap.getOrDefault(date + ":" + name, defaultOaGroupCountDTO);
                //根据班次分组
                Map<String, BigDecimal> classCountMap = Maps.newHashMap();
                Map<Integer, List<QtCheckTaskDetailReportVO>> dataSourceMap = classEntry.getValue().stream().collect(Collectors.groupingBy(QtCheckTaskDetailReportVO::getDataSource));
                for (Map.Entry<Integer, List<QtCheckTaskDetailReportVO>> dataSourceEntry : dataSourceMap.entrySet()) {
                    Map<String, BigDecimal> manCountMap = Maps.newHashMap();
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
                            vo.setClassName(name);
                            vo.setClassGroupName(oaGroupCountDTO.getGroupName());
                            vo.setClassGroupId(oaGroupCountDTO.getGroupId());
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
                            vo.setProductLineSort(treeSort);
                            //客户属性
                            vo.setCustomerName(qtCheckTask.getCustomerName());
                            vo.setCustomerId(qtCheckTask.getCustomerId());
                            tableData.add(vo);
                            List<FurtherValueStrVO> module = Lists.newArrayList();
                            vo.setModule(module);
                            BigDecimal rowTotal = null;
                            //遍历类别
                            for (Map.Entry<Integer, List<QtDefectiveItem>> categoryDefectEntry : categoryDefectMap.entrySet()) {
                                FurtherValueStrVO strVO = new FurtherValueStrVO();
                                List<String> value = Lists.newArrayList();
                                for (QtDefectiveItem defectItem : categoryDefectEntry.getValue()) {
                                    //获取该缺陷下的所有数据
                                    List<QtCheckTaskDetailReportVO> defectDataList = taskEntry.getValue().stream().filter(v -> v.getDefectCode().equals(defectItem.getCode())).collect(Collectors.toList());
                                    int sum = defectDataList.stream().filter(v -> Objects.nonNull(v.getDefectNum())).mapToInt(QtCheckTaskDetailReportVO::getDefectNum).sum();
                                    BigDecimalUtil.accumulate(manCountMap, defectItem.getCode(), BigDecimal.valueOf(sum));
                                    BigDecimalUtil.accumulate(countMap, defectItem.getCode(), BigDecimal.valueOf(sum));
                                    BigDecimalUtil.accumulate(classCountMap, defectItem.getCode(), BigDecimal.valueOf(sum));
                                    BigDecimalUtil.accumulate(allMap, defectItem.getCode(), BigDecimal.valueOf(sum));
                                    value.add(String.valueOf(sum));
                                    rowTotal = BigDecimalUtil.add(rowTotal, BigDecimal.valueOf(sum));
                                }
                                strVO.setValue(value);
                                module.add(strVO);
                            }
                            vo.setRowTotal(BigDecimalUtil.setScale(rowTotal, 0));
                        }
                    }
                    //早班或晚班人工检验的各个缺陷项的数量加总 ，仅当该行数据全为0时隐藏该行，否则显示该行
                    if (dataSourceEntry.getKey() == 0 && QtManagerConstant.CONF_QMS_COLLECT_ENABLE_CONTENT.equals(source)) {
                        QtDayReportOnlineDataVO vo = insertCount(4, categoryDefectMap, classEntry.getKey(), manCountMap, productLineName, treeSort);
                        vo.setDataSource(0);
                        tableData.add(vo);
                    }
                }
                QtDayReportOnlineDataVO vo = insertCount(1, categoryDefectMap, classEntry.getKey(), classCountMap, productLineName, treeSort);
                vo.setClassName(name);
                vo.setClassGroupName(oaGroupCountDTO.getGroupName());
                tableData.add(vo);
            }
            QtDayReportOnlineDataVO allVo = insertCount(2, categoryDefectMap, 2, countMap, productLineName, treeSort);
            tableData.add(allVo);
        }
        //插入总计
        QtDayReportOnlineDataVO zongjiVo = insertCount(3, categoryDefectMap, 2, allMap, "总计", Integer.MAX_VALUE);
        tableData.add(zongjiVo);
        //数据需要排序
        tableData.sort((o1, o2) -> {
            //先对比产线
            if (Objects.isNull(o1.getProductLineName()) && Objects.nonNull(o2.getProductLineName())) {
                return 1;
            }
            if (Objects.nonNull(o1.getProductLineName()) && Objects.isNull(o2.getProductLineName())) {
                return -1;
            }
            if (Objects.isNull(o1.getProductLineName()) && Objects.isNull(o2.getProductLineName())) {
                return 0;
            }
            if (!o1.getProductLineName().equals(o2.getProductLineName())) {
                return o1.getProductLineSort().compareTo(o2.getProductLineSort());
            }
            //先对比班次
            if (!o1.getClassId().equals(o2.getClassId())) {
                return o1.getClassId() - o2.getClassId();
            }
            //对比数据来源为空，则排序到前面
            if (Objects.isNull(o1.getDataSource())) {
                return 1;
            }
            if (Objects.isNull(o2.getDataSource())) {
                return -1;
            }
            //对比数据来源
            if (!o1.getDataSource().equals(o2.getDataSource())) {
                return o1.getDataSource() - o2.getDataSource();
            }
            //对比type
            if(o1.getType() != o2.getType()){
                return o1.getType() - o2.getType();
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
        header.setCommon(generateCommonHead(source));
        //落板原因
        resVo.setFallList(fallList(date, manData, sysOfficeVOMap));
        return resVo;
    }

    /**
     * 在线检
     * @param date 日期
     * @param lineIdList 产线
     * @param source 配置
     */
    public void onlineDownload(LocalDate date, List<Integer> lineIdList, String source, HttpServletResponse response) throws IOException {
        QtDayReportOnlineVO online = online(date, lineIdList, source);
        List<QtDayReportOnlineDataVO> tableData = online.getTableData();
        SteccnTableAssertUtil.notEmpty(tableData, "数据为空");
        //写表头
        QtDayReportTableHeadersVO headers = online.getHeaders();
        //在线检
        List<List<String>> list1 = new ArrayList<>();
        //落板记录
        List<List<String>> list2 = new ArrayList<>();
        Arrays.asList("产线","班次","班组","落板记录时段","落板工位","落板原因","备注").forEach(ele -> list2.add(Lists.newArrayList(ele)));

        List<QtSpcHeadTitleVO> common = headers.getCommon();
        common.forEach(ele -> {
            list1.add(Lists.newArrayList(ele.getLabel()));
        });
        List<BaseReportHeaderVO> module = headers.getModule();
        module.forEach(ele -> {
            List<String> value = ele.getValue();
            value.forEach(aa -> {
                list1.add(Lists.newArrayList(ele.getLabel(), aa));
            });
        });
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("质量日报表(人工)", "UTF-8");
        fileName += LocalDateUtil.localDateFormat(LocalDate.now(),"yyyy-MM-dd");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        //数据
        List<List<Object>> dataList1 = Lists.newArrayList();
        List<List<Object>> dataList2 = Lists.newArrayList();
        //落板记录
        online.getFallList().forEach(ele -> {
            List<Object> dataEle = Lists.newArrayList();
            dataEle.add(ele.getProductLineName());
            dataEle.add(ele.getClassName());
            dataEle.add(ele.getClassGroupName());
            String start = LocalDateTimeUtil.format(ele.getDestoryStartTime(), "MM-dd HH:mm");
            String end = LocalDateTimeUtil.format(ele.getDestoryEndTime(), "MM-dd HH:mm");
            dataEle.add(start + " ~ " + end);
            dataEle.add(ele.getDestoryWorkStationStr());
            dataEle.add(ele.getDestoryReason());
            dataEle.add(ele.getDestoryInfoRemark());
            dataList2.add(dataEle);
        });

        //在线检
        tableData.forEach(ele -> {
            List<Object> dataEle = Lists.newArrayList();
            if (ele.getType() == 1) {
                dataEle.add(ele.getProductLineName());
                dataEle.add("小计(单班次)");
                dataEle.add("小计(单班次)");
            } else if (ele.getType() == 2) {
                dataEle.add("合计(单产线)");
                dataEle.add("合计(单产线)");
                dataEle.add("合计(单产线)");
            }else if(ele.getType() == 3){
                dataEle.add("总计");
                dataEle.add("总计");
                dataEle.add("总计");
            } else {
                dataEle.add(ele.getProductLineName());
                dataEle.add(ele.getClassName());
                dataEle.add(ele.getClassGroupName());
            }
            dataEle.add(ele.getCustomerName());
            dataEle.add(ele.getMaterialName());
            dataEle.add(ele.getDefectTimeStr());
            dataEle.add(ele.getNote());
            dataEle.add(ele.getRowTotal());
            List<FurtherValueStrVO> rowData = ele.getModule();
            rowData.forEach(rf -> {
                List<String> value = rf.getValue();
                value.forEach(bb -> {
                    dataEle.add(Objects.nonNull(bb) ? Integer.parseInt(bb) : null);
                });
            });
            dataList1.add(dataEle);
        });
        int[] mergeColumnIndex1 = {0, 1, 2};
        int[] mergeColumnIndex2 = {0};

        ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).build();
        WriteSheet writeSheet1 = EasyExcel.writerSheet(0, "在线检日报表")
                .head(list1)
                .registerWriteHandler(new RowHeightStyleStrategy(30, 20))
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .registerWriteHandler(new HeadContentStyleStrategy())
                .registerWriteHandler(new ExcelFillCellMergeStrategy(1, mergeColumnIndex1))
                .build();
        WriteSheet writeSheet2 = EasyExcel.writerSheet(1, "落板记录")
                .head(list2)
                .registerWriteHandler(new RowHeightStyleStrategy(30, 20))
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .registerWriteHandler(new HeadContentStyleStrategy())
                .registerWriteHandler(new ExcelFillCellMergeStrategy(1, mergeColumnIndex2))
                .build();
        excelWriter.write(dataList1, writeSheet1);
        excelWriter.write(dataList2, writeSheet2);
        excelWriter.finish();
        response.flushBuffer();
    }

    /**
     * 获取落板原因
     *
     * @param manDataList 人工检测数据
     * @return 落板原因集合
     */
    private List<QtCheckTaskDestoryOnlineVO> fallList(LocalDate curDate, List<QtCheckTaskDetailReportVO> manDataList, Map<Integer, SysOfficeVO> lineIdNameMap) {
        List<QtCheckTaskDestoryOnlineVO> fallList = Lists.newArrayList();
        if (CollUtil.isNotEmpty(manDataList)) {
            TupleDTO<LocalDate, LocalDate> dateDTO = new TupleDTO(curDate, curDate);
            Map<String, OaGroupCountDTO> groupInfoMap = FeignOptional.of(() -> oaInfoService.getGroupInfo(dateDTO), FeignConstant.STECCN_OA_SERVICE_NAME).get();
            OaGroupCountDTO defaultOaGroupCountDTO = new OaGroupCountDTO();
            manDataList = manDataList.stream().filter(v -> Objects.nonNull(v.getTaskId())).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(manDataList)) {
                Map<Long, Integer> taskIdLineMap = Maps.newHashMap();
                manDataList.forEach(ele -> taskIdLineMap.put(ele.getTaskId(), ele.getProductLineId()));

                //先根据人工数据查询所有的taskId
                Map<Long, Integer> taskClassMap = Maps.newHashMap();
                manDataList.forEach(ele -> taskClassMap.put(ele.getTaskId(), ele.getClassId()));
                //查询所有的落板原因
                List<QtCheckTaskDestoryInfo> destroyList = qtCheckTaskDestoryInfoService.list(new LambdaQueryWrapper<QtCheckTaskDestoryInfo>()
                        .in(QtCheckTaskDestoryInfo::getTaskId, taskClassMap.keySet())
                        .isNotNull(QtCheckTaskDestoryInfo::getDestoryReason)
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
                            String name = QtManagerConstant.QtClassesEnum.code(vo.getClassId()).getName();
                            vo.setClassName(name);
                            OaGroupCountDTO oaGroupCountDTO = groupInfoMap.getOrDefault(curDate + ":" + name, defaultOaGroupCountDTO);
                            vo.setClassGroupName(oaGroupCountDTO.getGroupName());
                            vo.setClassGroupId(oaGroupCountDTO.getGroupId());
                            QtManagerConstant.QtClassesEnum classEnum = QtManagerConstant.QtClassesEnum.code(vo.getClassId());
                            if (Objects.nonNull(classEnum)) {
                                vo.setClassName(classEnum.getName());
                            }
                            Integer line = taskIdLineMap.get(destroy.getTaskId());
                            SysOfficeVO sysOfficeVO = lineIdNameMap.getOrDefault(line, new SysOfficeVO());
                            vo.setProductLineName(sysOfficeVO.getOfficeName());
                            vo.setProductLineSort(sysOfficeVO.getTreeSort());
                            //落板工位（南） 落板工位（北）
                            String station = QtManagerConstant.FallStationEnum.getByCode(destroy.getDestoryWorkStation());
                            vo.setDestoryWorkStationStr(station);
                            fallList.add(vo);
                        }
                    }
                    fallList.sort((o1, o2) -> {
                        //先对比产线
                        if (!o1.getProductLineName().equals(o2.getProductLineName())) {
                            return o1.getProductLineSort().compareTo(o2.getProductLineSort());
                        }
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
    private QtDayReportOnlineDataVO insertCount(Integer type, Map<Integer, List<QtDefectiveItem>> categoryDefectMap, Integer classId, Map<String, BigDecimal> classCountMap, String productLineName, Integer treeSort) {
        //插入一条合计
        QtDayReportOnlineDataVO vo = new QtDayReportOnlineDataVO();
        vo.setProductLineName(productLineName);
        vo.setProductLineSort(treeSort);
        vo.setType(type);
        List<FurtherValueStrVO> module = Lists.newArrayList();
        vo.setModule(module);
        vo.setClassId(classId);
        BigDecimal rowTotal = null;
        for (Map.Entry<Integer, List<QtDefectiveItem>> categoryDefectEntry : categoryDefectMap.entrySet()) {
            FurtherValueStrVO strVO = new FurtherValueStrVO();
            List<String> value = Lists.newArrayList();
            for (QtDefectiveItem defectItem : categoryDefectEntry.getValue()) {
                value.add(BigDecimalUtil.format(classCountMap.get(defectItem.getCode()), 0));
                rowTotal = BigDecimalUtil.add(rowTotal, classCountMap.get(defectItem.getCode()));
            }
            strVO.setValue(value);
            module.add(strVO);
        }
        vo.setRowTotal(BigDecimalUtil.setScale(rowTotal, 0));
        return vo;
    }

    /**
     * 生成静态表头
     *
     * @return list
     * @param source
     */
    private List<QtSpcHeadTitleVO> generateCommonHead(String source) {
        List<QtSpcHeadTitleVO> commonHeadList = Lists.newArrayList();
        commonHeadList.add(new QtSpcHeadTitleVO("产线", "productLineName"));
        commonHeadList.add(new QtSpcHeadTitleVO("班次", "className"));
        commonHeadList.add(new QtSpcHeadTitleVO("班组", "classGroupName"));
        commonHeadList.add(new QtSpcHeadTitleVO("客户", "customerName"));
        if (QtManagerConstant.CONF_QMS_COLLECT_ENABLE_CONTENT.equals(source)) {
            commonHeadList.add(new QtSpcHeadTitleVO("来源", "dataSourceStr"));
        }
        commonHeadList.add(new QtSpcHeadTitleVO("物料名称", "materialName"));
        commonHeadList.add(new QtSpcHeadTitleVO("缺陷记录时段", "defectTimeStr"));
        commonHeadList.add(new QtSpcHeadTitleVO("备注", "note"));
        commonHeadList.add(new QtSpcHeadTitleVO("合计", "rowTotal"));
        return commonHeadList;
    }

    /**
     * 构建module
     *
     * @param categoryDefectMap 缺陷map
     * @return
     */
    private List<BaseReportHeaderVO> moduleHead(Map<Integer, List<QtDefectiveItem>> categoryDefectMap) {
        //查询缺陷和缺陷类别名称
        Map<Integer, String> categoryIdNameMap = qtItemCategoryService.listByIdCh(categoryDefectMap.keySet());
        //构造module
        List<BaseReportHeaderVO> resModule = Lists.newArrayList();
        for (Map.Entry<Integer, List<QtDefectiveItem>> categoryDefectEntry : categoryDefectMap.entrySet()) {
            List<String> value = Lists.newArrayList();
            BaseReportHeaderVO vo = new BaseReportHeaderVO();
            vo.setLabel(categoryIdNameMap.get(categoryDefectEntry.getKey()));
            for (QtDefectiveItem defectItem : categoryDefectEntry.getValue()) {
                value.add(defectItem.getName());
            }
            vo.setValue(value);
            resModule.add(vo);
        }
        return resModule;
    }


    @Override
    public QtDayReportFirstVO first(QtReportFirstPatrolDTO dto) {
        LocalDateTime startTime = LocalDateUtil.getMorning(dto.getDate());
        LocalDateTime endTime = LocalDateUtil.getMorning(dto.getDate().plusDays(1));
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
                        QtCheckTask::getClassTime, QtCheckTask::getSpotTime, QtCheckTask::getStation, QtCheckTask::getClassGroupName)
                //关联物料表
                .selectAs(QtMaterial::getName, QtCheckTaskListVO::getMaterialName)
                //json映射
                .selectAs(QtCheckTask::getProductLineName, QtCheckTaskListVO::getProductLineName)
                .selectAs(QtCheckTask::getProductLineId, QtCheckTaskListVO::getProductLineId)
                .selectAs(QtCheckTask::getCustomerName, QtCheckTaskListVO::getCustomerName)
                //关联
                .leftJoin(QtMaterial.class, QtMaterial::getId, QtCheckTask::getMaterialId)
                .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())
                .eq(QtCheckTask::getTaskType, dto.getCheckTaskType())
                //产线
                .apply("JSON_EXTRACT(product_line, '$.id') IN (" + StrUtil.join(",", dto.getProductLineId()) + ")");

        if (QtManagerConstant.QtCheckTaskTypeEnum.FIRST.getCode().equals(dto.getCheckTaskType())) {
            //大于等于开始   小于结束
            taskCondition.ge(QtCheckTask::getFinishedTime, startTime).lt(QtCheckTask::getFinishedTime, endTime);
        } else {
            //大于等于开始   小于结束
            taskCondition.ge(QtCheckTask::getSpotTime, startTime).lt(QtCheckTask::getSpotTime, endTime);
        }

        List<QtCheckTaskListVO> taskList = qtCheckTaskMapper.selectJoinList(QtCheckTaskListVO.class, taskCondition);
        List<QtCheckTaskDetailReportVO> manData = Lists.newArrayList();
        //获取工位的map
        Set<Integer> officeIds = Sets.newHashSet();
        taskList.forEach(task -> {
            officeIds.add(task.getStationId());
            QtCheckTaskDetailReportVO qtCheckTaskDetailReportVO = new QtCheckTaskDetailReportVO();
            qtCheckTaskDetailReportVO.setClassId(task.getClassesId());
            qtCheckTaskDetailReportVO.setTaskId(task.getId());
            manData.add(qtCheckTaskDetailReportVO);
        });
        Map<Integer, String> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(officeIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Map<Integer, SysOfficeVO> officeVoMap = FeignOptional.of(() -> remoteOfficeFeign.listById(officeIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

        //把深加工或者基片的值取出
        Map<Integer, SysOfficeVO> sysOfficeVOMap = FeignOptional.of(() -> remoteOfficeFeign.listById(dto.getProductLineId()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<SysOfficeVO> productLineList = sysOfficeVOMap.values().stream().collect(Collectors.toList());
        productLineList.stream().forEach(tmp -> {
            tmp.setOfficeNamePrefix(tmp.getOfficeName().substring(0, 2));
        });
        productLineList = productLineList.stream().sorted(Comparator.comparing(SysOfficeVO::getOfficeNamePrefix).thenComparing(SysOfficeVO::getTreeSort)).collect(Collectors.toList());
        Set<Long> taskIdList = Sets.newHashSet();
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
        //获取上偏差下偏差
        Set<Integer> itemSet = taskDetailList.stream().map(QtCheckTaskDetail::getItemId).collect(Collectors.toSet());
        Set<Integer> groupSet = taskDetailList.stream().map(QtCheckTaskDetail::getGroupId).collect(Collectors.toSet());
        List<QtCheckPlanGroupItem> qtCheckPlanGroupItems = Lists.newArrayList();
        if(CollUtil.isNotEmpty(itemSet)&&CollUtil.isNotEmpty(groupSet)){
            qtCheckPlanGroupItems = qtCheckPlanGroupItemService.list(new LambdaQueryWrapper<QtCheckPlanGroupItem>()
                    .select(QtCheckPlanGroupItem::getGroupId,QtCheckPlanGroupItem::getCheckItemId,QtCheckPlanGroupItem::getStandardValue,QtCheckPlanGroupItem::getLowerValue,QtCheckPlanGroupItem::getUpperValue)
                    .in(QtCheckPlanGroupItem::getGroupId, groupSet)
                    .in(QtCheckPlanGroupItem::getCheckItemId, itemSet));
        }
        //拿到所有的
        //查到分类数据和检验项数据
        Set<Integer> itemIdList = Sets.newHashSet();
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

        if (QtManagerConstant.QtCheckTaskTypeEnum.FIRST.getCode().equals(dto.getCheckTaskType())) {
            //首检表头
            QtSpcHeadTitleVO staticHead1 = new QtSpcHeadTitleVO();
            staticHead1.setLabel(QtManagerConstant.QtChartCompareEnum.PRODUCTLINE.getName());
            staticHead1.setValue(QtManagerConstant.SPC_HEADER_FIELD_PRODUCT_LINE_VALUE);
            common.add(staticHead1);
            QtSpcHeadTitleVO staticHead2 = new QtSpcHeadTitleVO();
            staticHead2.setLabel(QtManagerConstant.DAY_REPORT_FIELD_ORDER);
            staticHead2.setValue(QtManagerConstant.DAY_REPORT_FIELD_ORDER_VALUE);
            common.add(staticHead2);
            QtSpcHeadTitleVO staticHead8 = new QtSpcHeadTitleVO();
            staticHead8.setLabel(QtManagerConstant.DAY_REPORT_FIELD_GROUP);
            staticHead8.setValue(QtManagerConstant.DAY_REPORT_FIELD_GROUP_VALUE);
            common.add(staticHead8);
            QtSpcHeadTitleVO staticHead3 = new QtSpcHeadTitleVO();
            staticHead3.setLabel(QtManagerConstant.SPC_HEADER_FIELD_FINISHED_TIME);
            staticHead3.setValue(QtManagerConstant.SPC_HEADER_FIELD_FINISHED_TIME_VALUE);
            common.add(staticHead3);
            QtSpcHeadTitleVO staticHead4 = new QtSpcHeadTitleVO();
            staticHead4.setLabel(QtManagerConstant.SPC_HEADER_FIELD_MATERIAL_NAME);
            staticHead4.setValue(QtManagerConstant.SPC_HEADER_FIELD_MATERIAL_NAME_VALUE);
            common.add(staticHead4);
            QtSpcHeadTitleVO staticHead9 = new QtSpcHeadTitleVO();
            staticHead9.setLabel(QtManagerConstant.DAY_REPORT_FIELD_CUSTOMER);
            staticHead9.setValue(QtManagerConstant.DAY_REPORT_FIELD_CUSTOMER_VALUE);
            common.add(staticHead9);
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
            QtSpcHeadTitleVO staticHead9 = new QtSpcHeadTitleVO();
            staticHead9.setLabel(QtManagerConstant.DAY_REPORT_FIELD_GROUP);
            staticHead9.setValue(QtManagerConstant.DAY_REPORT_FIELD_GROUP_VALUE);
            common.add(staticHead9);
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
            QtSpcHeadTitleVO staticHead10 = new QtSpcHeadTitleVO();
            staticHead10.setLabel(QtManagerConstant.DAY_REPORT_FIELD_CUSTOMER);
            staticHead10.setValue(QtManagerConstant.DAY_REPORT_FIELD_CUSTOMER_VALUE);
            common.add(staticHead10);
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
        List<QtCheckTaskDestoryOnlineVO> luoData = Lists.newArrayList();
        //根据产线循环
        for (SysOfficeVO productLine : productLineList) {
            //先获取产线下的任务
            List<QtCheckTaskListVO> productLineTaskList = taskList.stream().filter(v -> v.getProductLineId().equals(productLine.getId().toString())).collect(Collectors.toList());
            //如果为空则表示该产线下没有数据,跳过循环
            if (CollUtil.isEmpty(productLineTaskList)) {
                //组合数据
                QtDayReportFirstDataVO column = new QtDayReportFirstDataVO();
                //具体data
                column.setProductLine(productLine.getOfficeName());
                //动态data
                List<FurtherValueObjVO> moduleData = Lists.newArrayList();
                //找到动态表头
                for (BaseReportHeaderVO headModule : module) {
                    List<ColorValueVO> colorValueList = Lists.newArrayList();
                    for (String headValue : headModule.getValue()) {
                        ColorValueVO colorValue = new ColorValueVO();
                        colorValue.setValue("");
                        colorValue.setColor(0);
                        colorValueList.add(colorValue);
                    }
                    FurtherValueObjVO valueObjVO = new FurtherValueObjVO();
                    valueObjVO.setValue(colorValueList);
                    moduleData.add(valueObjVO);
                }
                column.setModule(moduleData);
                tableData.add(column);
                continue;
            }
            //班组map
            Map<Integer, String> classGroupMap = Maps.newHashMap();
            //早班晚班对应的班组
            for (int i = 0; i < QtManagerConstant.NUM_2; i++) {
                int finalI = i;
                List<QtCheckTaskListVO> morningCollect = productLineTaskList
                        .stream()
                        .filter(v -> v.getClassesId() != null && v.getClassesId().equals(finalI))
                        .collect(Collectors.toList());
                if (CollUtil.isNotEmpty(morningCollect)) {
//                    1）集合T1内的记录所属的班组（所有记录填报的班组中，取填报次数最多的一个班组）为早班对应的班组
//                    2）集合T2内的记录所属的班组为晚班对应的班组
                    Map<String, Long> countByAttribute = morningCollect.stream()
                            .collect(Collectors.groupingBy(QtCheckTaskListVO::getClassGroupName, Collectors.counting()));
                    //取出现次数最多的班组名
                    String mostFrequentName = countByAttribute.entrySet().stream()
                            .max(Comparator.comparing(Map.Entry::getValue))
                            .map(Map.Entry::getKey)
                            .orElse(null);
                    classGroupMap.put(i, mostFrequentName);
                }
            }
            //过滤产线下的任务详情
            List<Long> productLineTaskIdList = productLineTaskList.stream()
                    .map(QtCheckTaskListVO::getId)
                    .collect(Collectors.toList());
            List<QtCheckTaskDetail> productLineTaskDetailList = taskDetailList.stream().filter(v -> productLineTaskIdList.contains(v.getTaskId())).collect(Collectors.toList());
            //根据taskId分组
            Map<Long, List<QtCheckTaskDetail>> groupByTaskId = productLineTaskDetailList.stream().collect(Collectors.groupingBy(v -> v.getTaskId()));
            //先根据班次排序再根据完成时间排序
            productLineTaskList = productLineTaskList.stream().sorted(Comparator.comparing(QtCheckTaskListVO::getClassesId)
                    .thenComparing((o1, o2) -> {
                        if(Objects.isNull(o1.getStationId()) && Objects.isNull(o2.getStationId())){
                            return 0;
                        }else if(Objects.isNull(o1.getStationId()) || Objects.isNull(o2.getStationId())){
                            return 1;
                        }
                        return o2.getStationId().compareTo(o1.getStationId());
                    })
                    .thenComparing(QtCheckTaskListVO::getFinishedTime)).collect(Collectors.toList());
            //根据taskId重组map
            Map<Long, List<QtCheckTaskDetail>> groupByTaskIdOrder = new LinkedHashMap<>();
            productLineTaskList.forEach(tmp -> {
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
                    column.setCustomerName(task.getCustomerName());
                    if (Objects.nonNull(task.getStationId())) {
                        column.setStation(officeMap.get(task.getStationId()));
                    }
                    column.setResult(task.getResult());
                    column.setCopy(copyEntry.getKey());
                    column.setNote(task.getNote());
                    column.setClassName(QtManagerConstant.QtClassesEnum.code(task.getClassesId()).getName());
                    column.setClassGroupName(classGroupMap.get(task.getClassesId()));
                    column.setProductLine(task.getProductLineName());
                    column.setCustomerName(task.getCustomerName());
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
                                        QtCheckPlanGroupItem planGroupItem = qtCheckPlanGroupItems.stream()
                                                .filter(v->v.getGroupId().equals(copyTmp.getGroupId())&&v.getCheckItemId().equals(copyTmp.getItemId())).findFirst().orElse(new QtCheckPlanGroupItem());
//                                                qtCheckPlanGroupItemService.getOne(new LambdaQueryWrapper<QtCheckPlanGroupItem>()
//                                                .eq(QtCheckPlanGroupItem::getGroupId, copyTmp.getGroupId())
//                                                .eq(QtCheckPlanGroupItem::getCheckItemId, copyTmp.getItemId()), false);
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

            //如果所有的工位都为空，则表头不需要工位那一列
            long count = tableData.stream().filter(v -> StrUtil.isNotBlank(v.getStation())).count();
            if (count == 0) {
                List<QtSpcHeadTitleVO> oldCommon = result.getHeaders().getCommon();
                List<QtSpcHeadTitleVO> newCommon = oldCommon.stream().filter(v -> !QtManagerConstant.SPC_HEADER_FIELD_STATION.equals(v.getLabel())).collect(Collectors.toList());
                result.getHeaders().setCommon(newCommon);
            }

            if (QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getCode().equals(dto.getCheckTaskType())) {
                //落板原因
                luoData.addAll(fallList(dto.getDate(), manData, officeVoMap));
                //缺陷表格
            }

        }
        result.setFallList(luoData);
        result.setTableData(tableData);
        return result;
    }


    @Override
    public void firstDownload(QtReportFirstPatrolDTO dto, HttpServletResponse response) throws IOException {
        if (dto.getCheckTaskType() == 4) {
            //巡检
            patrolCheckDownload(dto, response);
        } else {
            //首检
            firstCheckDownload(dto, response);
        }
    }

    /**
     * 巡检下载
     * @param dto
     * @param response
     */
    private void patrolCheckDownload(QtReportFirstPatrolDTO dto, HttpServletResponse response) throws IOException{
        QtDayReportFirstVO first = first(dto);
        List<QtDayReportFirstDataVO> tableData = first.getTableData();
        SteccnTableAssertUtil.notEmpty(tableData, "数据为空");
        //写表头
        QtDayReportTableHeadersVO headers = first.getHeaders();
        //在线检
        List<List<String>> list1 = new ArrayList<>();
        //落板记录
        List<List<String>> list2 = new ArrayList<>();
        Arrays.asList("产线", "班次", "班组", "落板记录时段", "落板工位", "落板原因", "备注").forEach(ele -> list2.add(Lists.newArrayList(ele)));

        List<QtSpcHeadTitleVO> common = headers.getCommon();
        common.forEach(ele -> {
            list1.add(Lists.newArrayList(ele.getLabel()));
        });
        List<BaseReportHeaderVO> module = headers.getModule();
        module.forEach(ele -> {
            List<String> value = ele.getValue();
            value.forEach(aa -> {
                list1.add(Lists.newArrayList(ele.getLabel(), aa));
            });
        });
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("巡检", "UTF-8");
        fileName += LocalDateUtil.localDateFormat(LocalDate.now(),"yyyy-MM-dd");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        //数据
        List<List<Object>> dataList1 = Lists.newArrayList();
        List<List<Object>> dataList2 = Lists.newArrayList();
        //落板记录
        if (CollUtil.isNotEmpty(first.getFallList())) {
            first.getFallList().forEach(ele -> {
                List<Object> dataEle = Lists.newArrayList();
                dataEle.add(ele.getProductLineName());
                dataEle.add(ele.getClassName());
                dataEle.add(ele.getClassGroupName());
                String start = LocalDateTimeUtil.format(ele.getDestoryStartTime(), "MM-dd HH:mm");
                String end = LocalDateTimeUtil.format(ele.getDestoryEndTime(), "MM-dd HH:mm");
                dataEle.add(start + " ~ " + end);
                dataEle.add(ele.getDestoryWorkStationStr());
                dataEle.add(ele.getDestoryReason());
                dataEle.add(ele.getDestoryInfoRemark());
                dataList2.add(dataEle);
            });
        }
        tableData.forEach(ele -> {
            List<Object> dataEle = Lists.newArrayList();
            dataEle.add(ele.getProductLine());
            dataEle.add(ele.getClassName());
            dataEle.add(ele.getClassGroupName());
            dataEle.add(LocalDateUtil.format(ele.getSpotTime(),"MM-dd HH:mm"));
            dataEle.add(ele.getStation());
            dataEle.add(ele.getMaterialName());
            dataEle.add(ele.getCustomerName());
            dataEle.add(ele.getResult());
            dataEle.add(ele.getNote());
            dataEle.add(ele.getCopy());

            List<FurtherValueObjVO> rowData = ele.getModule();
            rowData.forEach(rf -> {
                List<ColorValueVO> value = rf.getValue();
                value.forEach(bb -> {
                    dataEle.add(bb.getValue());
                });
            });
            dataList1.add(dataEle);
        });
        int[] mergeColumnIndex1 = {0, 1, 2};
        int[] mergeColumnIndex2 = {0};

        ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).build();
        WriteSheet writeSheet1 = EasyExcel.writerSheet(0, "巡检")
                .head(list1)
                .registerWriteHandler(new RowHeightStyleStrategy(30, 20))
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .registerWriteHandler(new HeadContentStyleStrategy())
                .registerWriteHandler(new ExcelFillCellMergeStrategy(1, mergeColumnIndex1))
                .build();
        excelWriter.write(dataList1, writeSheet1);
        if(CollUtil.isNotEmpty(first.getFallList())){
            WriteSheet writeSheet2 = EasyExcel.writerSheet(1, "落板记录")
                    .head(list2)
                    .registerWriteHandler(new RowHeightStyleStrategy(30, 20))
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .registerWriteHandler(new HeadContentStyleStrategy())
                    .registerWriteHandler(new ExcelFillCellMergeStrategy(1, mergeColumnIndex2))
                    .build();
            excelWriter.write(dataList2, writeSheet2);
        }
        excelWriter.finish();
        response.flushBuffer();
    }

    /**
     * 首检下载
     * @param dto
     * @param response
     */
    private void firstCheckDownload(QtReportFirstPatrolDTO dto, HttpServletResponse response) throws IOException {
        QtDayReportFirstVO first = first(dto);
        List<QtDayReportFirstDataVO> tableData = first.getTableData();
        SteccnTableAssertUtil.notEmpty(tableData, "数据为空");
        //写表头
        QtDayReportTableHeadersVO headers = first.getHeaders();
        List<List<String>> list = new ArrayList<>();
        List<QtSpcHeadTitleVO> common = headers.getCommon();
        common.forEach(ele -> {
            list.add(Lists.newArrayList(ele.getLabel()));
        });
        List<BaseReportHeaderVO> module = headers.getModule();
        module.forEach(ele -> {
            List<String> value = ele.getValue();
            value.forEach(aa -> {
                list.add(Lists.newArrayList(ele.getLabel(), aa));
            });
        });
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("首检", "UTF-8");
        fileName += LocalDateUtil.localDateFormat(LocalDate.now(), "yyyy-MM-dd");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        //数据
        List<List<Object>> dataList = Lists.newArrayList();
        tableData.forEach(ele -> {
            List<Object> dataEle = Lists.newArrayList();
            dataEle.add(ele.getProductLine());
            dataEle.add(ele.getClassName());
            dataEle.add(ele.getClassGroupName());
            dataEle.add(LocalDateUtil.format(ele.getFinishedTime(), "MM-dd HH:mm"));
            dataEle.add(ele.getMaterialName());
            dataEle.add(ele.getCustomerName());
            dataEle.add(ele.getResult());
            dataEle.add(ele.getNote());
            dataEle.add(ele.getCopy());
            List<FurtherValueObjVO> rowData = ele.getModule();
            rowData.forEach(rf -> {
                List<ColorValueVO> value = rf.getValue();
                value.forEach(bb -> {
                    dataEle.add(bb.getValue());
                });
            });
            dataList.add(dataEle);
        });
        int[] mergeColumnIndex = {0, 1, 2};
        ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).build();
        WriteSheet writeSheet1 = EasyExcel.writerSheet(0, "首检")
                .head(list)
                .registerWriteHandler(new RowHeightStyleStrategy(30, 20))
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .registerWriteHandler(new HeadContentStyleStrategy())
                .registerWriteHandler(new ExcelFillCellMergeStrategy(1, mergeColumnIndex))
                .build();
        excelWriter.write(dataList, writeSheet1);
        excelWriter.finish();
        response.flushBuffer();
    }

    @Override
    public QtDayPollingFirstVO pollingDefectItem(QtReportFirstPatrolDTO dto) {
        QtDayPollingFirstVO qtDayPollingFirstVO = new QtDayPollingFirstVO();
        LocalDateTime startTime = LocalDateUtil.getMorning(dto.getDate());
        LocalDateTime endTime = LocalDateUtil.getMorning(dto.getDate().plusDays(1));
        LambdaQueryWrapper<QtCheckTask> eq = new LambdaQueryWrapper<QtCheckTask>()
                .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())
                .eq(QtCheckTask::getTaskType, QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getCode())
                //大于等于开始   小于结束
                .ge(QtCheckTask::getSpotTime, startTime)
                .lt(QtCheckTask::getSpotTime, endTime)
                //产线
                .apply("JSON_EXTRACT(product_line, '$.id') IN (" + StrUtil.join(",", dto.getProductLineId()) + ")");
        List<QtCheckTask> list = qtCheckTaskService.list(eq);
        List<Long> taskIds = Lists.newArrayList();
        list.forEach(task -> taskIds.add(task.getId()));
        SteccnTableAssertUtil.notEmpty(list, "数据为空");
        List<QtDefectItemInfoVO> defectItemList = qtCheckTaskMapper.getDefectItemList(QtManagerConstant.DAY_REPORT_FIELD_APPARENT_CONDITION, taskIds);
        SteccnTableAssertUtil.notEmpty(defectItemList, "数据为空");
        // 根据缺陷项类别分组
        Map<String, List<QtDefectItemInfoVO>> map = defectItemList.stream().collect(Collectors.groupingBy(QtDefectItemInfoVO::getCategoryName));

        QtDayReportTableHeadersVO qtDayReportTableHeadersVO = new QtDayReportTableHeadersVO();
        List<QtSpcHeadTitleVO> commonHeadList = Lists.newArrayList();
        commonHeadList.add(new QtSpcHeadTitleVO("产线", "productLineName"));
        commonHeadList.add(new QtSpcHeadTitleVO("班次", "className"));
        commonHeadList.add(new QtSpcHeadTitleVO("班组", "classGroupName"));
        qtDayReportTableHeadersVO.setCommon(commonHeadList);
        //处理数据
        List<BaseReportHeaderVO> module = Lists.newArrayList();
        List<QtDayReportPollingDataVO> reportPollingList = Lists.newArrayList();

        Map<Integer, SysOfficeVO> sysOfficeVOMap = FeignOptional.of(() -> remoteOfficeFeign.listById(dto.getProductLineId()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<SysOfficeVO> productLineList = sysOfficeVOMap.values().stream().collect(Collectors.toList());
        productLineList.stream().forEach(tmp -> {
            tmp.setOfficeNamePrefix(tmp.getOfficeName().substring(0, 2));
        });
        productLineList = productLineList.stream().sorted(Comparator.comparing(SysOfficeVO::getOfficeNamePrefix).thenComparing(SysOfficeVO::getTreeSort)).collect(Collectors.toList());

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
        for (SysOfficeVO productLine : productLineList) {
            List<QtDefectItemInfoVO> defectItemProductLineList = defectItemList.stream().filter(v -> v.getProductLineId() != null && v.getProductLineId().equals(productLine.getId())).collect(Collectors.toList());
            //如果产线数据为空
            if (CollUtil.isEmpty(defectItemProductLineList)) {
                QtDayReportPollingDataVO qtDayReportPollingDataVO = new QtDayReportPollingDataVO();
                qtDayReportPollingDataVO.setProductLineName(productLine.getOfficeName());
                List<FurtherValueStrVO> valueList = Lists.newArrayList();
                qtDayReportPollingDataVO.setType(0);
                //根据类型处理数据对应表头数据
                for (Map.Entry<String, List<QtDefectItemInfoVO>> typeEntry : map.entrySet()) {
                    FurtherValueStrVO furtherValueStrVO = new FurtherValueStrVO();
                    List<String> value = Lists.newArrayList();
                    List<QtDefectItemInfoVO> collect = typeEntry.getValue().stream().collect(Collectors.collectingAndThen(Collectors.toCollection(()
                            -> new TreeSet<>(Comparator.comparing(QtDefectItemInfoVO::getId))), ArrayList::new));
                    for (QtDefectItemInfoVO qtDefectItemInfoVO : collect) {
                        value.add(null);
                    }
                    furtherValueStrVO.setValue(value);
                    valueList.add(furtherValueStrVO);
                }
                qtDayReportPollingDataVO.setModule(valueList);
                reportPollingList.add(qtDayReportPollingDataVO);
                continue;
            }
            // 根据班次类别分组
            Map<Integer, List<QtDefectItemInfoVO>> classMap = defectItemProductLineList.stream().collect(Collectors.groupingBy(QtDefectItemInfoVO::getClassesId));
            //根据班次处理表格数据
            for (Map.Entry<Integer, List<QtDefectItemInfoVO>> classEntry : classMap.entrySet()) {
                String classGroupName = "";
                if (StrUtil.isNotBlank(classEntry.getValue().get(0).getClassGroupName())) {
                    classGroupName = OaManagerConstant.OaTeamGroupMapping.getNameByOaName(classEntry.getValue().get(0).getClassGroupName());
                }
                QtDayReportPollingDataVO qtDayReportPollingDataVO = new QtDayReportPollingDataVO();
                qtDayReportPollingDataVO.setProductLineName(productLine.getOfficeName());
                qtDayReportPollingDataVO.setClassId(classEntry.getKey());
                qtDayReportPollingDataVO.setClassGroupName(classGroupName);
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
                    value.add(String.valueOf(getDefectItemQuantity(qtDefectItemInfoVO.getId(), defectItemProductLineList)));
                }
                furtherValueStrVO.setValue(value);
                totalList.add(furtherValueStrVO);
            }
            qtDayReportPollingDataVO.setModule(totalList);
            reportPollingList.add(qtDayReportPollingDataVO);

        }
        //处理总计
        QtDayReportPollingDataVO qtDayReportPollingDataVO = new QtDayReportPollingDataVO();
        qtDayReportPollingDataVO.setType(2);
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
        //1.获取早班和晚班时间
        LocalDateTime morning = LocalDateUtil.getMorning(date);
//        LocalDateTime night = LocalDateUtil.getNight(date).plusDays(1);
//        1）查询已检验的配料抽检任务中，取样时间在区间 [查询日期8:00:00, 查询日期20:00:00] 内的所有样本，记为集合T1（早班）
//        2）查询已检验的配料抽检任务中，取样时间在区间 [查询日期20:00:00, 查询日期+1 8:00:00] 内的所有样本，记为集合T2（晚班）
        LocalDateTime night = morning.plusDays(1);
        //2.获取原始数据
        List<QtDaySamplingDTO> sourceList = qtCheckTaskMapper.samplingList(morning, night);
        //3.如果原始数据集合不为空，则继续执行
        SteccnTableAssertUtil.notEmpty(sourceList, "配料抽检原始数据集合为空");
        //4.拆分时间并过滤
        //记录所有的检验项
        Set<Integer> itemIdSet = Sets.newHashSet();
        sourceList.forEach(ele -> itemIdSet.add(ele.getItemId()));
        Map<Integer, Integer> originalMap = qtCheckItemService.queryOriginal(itemIdSet);
        //将检验项替换成原照-屏蔽所有的快照可能
        sourceList.forEach(ele -> ele.setItemId(originalMap.get(ele.getItemId())));
        //查询所有原照的检验项
        Set<Integer> newItemIdSet = Sets.newHashSet();
        sourceList.forEach(ele -> newItemIdSet.add(ele.getItemId()));
        Map<Integer, QtCheckItem> itemMap = qtCheckItemService.mapCheckItem(Lists.newArrayList(newItemIdSet));
        //构造idxmap
        Map<Integer, Integer> idxMap = Maps.newHashMap();
        List<String> dynamicHeadList = Lists.newArrayList();
        //如果为百分比的，加上%号，其余的看measure_unit
        SysDictVO sysDictVO = FeignOptional.of(() -> remoteDictFeignApi.getByCode(SysDictConstant.CODE_2, SysDictConstant.TYPE_CHECK_DATA_TYPE), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

        //存放数据字典
        Set<Long> dictIdSet = Sets.newHashSet();
        int idx = 0;
        for (Map.Entry<Integer, QtCheckItem> itemEntry : itemMap.entrySet()) {
            dynamicHeadList.add(itemEntry.getValue().getName());
            idxMap.put(itemEntry.getKey(), idx++);
            //添加测量单位
            if (Objects.nonNull(itemEntry.getValue().getMeasureUnit())) {
                dictIdSet.add(itemEntry.getValue().getMeasureUnit());
            }
        }
        Map<Long, String> dictIdNameMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(dictIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

        QtDaySamplingVO resVo = new QtDaySamplingVO();
        resVo.setDynamicHeadList(dynamicHeadList);
        List<QtDaySamplingDataVO> dataList = Lists.newArrayList();
        Map<String, List<QtDaySamplingDTO>> taskMap = sourceList.stream().collect(Collectors.groupingBy(v -> v.getTaskId() + "_" + v.getSamplingTime() + "_" + v.getCopy()));

        for (Map.Entry<String, List<QtDaySamplingDTO>> taskEntry : taskMap.entrySet()) {
            String[] key = taskEntry.getKey().split("_");
            LocalDateTime samplingTime = LocalDateTime.parse(key[1]);
            QtDaySamplingDTO samplingDTO = taskEntry.getValue().get(0);
            QtDaySamplingDataVO data = new QtDaySamplingDataVO();
            BeanUtils.copyProperties(samplingDTO, data);
            //格式化时间 HH:mm
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            String format = dateTimeFormatter.format(samplingTime.toLocalTime());
            data.setSamplingTime(format);
            data.setSamplingTimeLdt(samplingDTO.getSamplingTime());
            data.setClassId(ClassUtil.classAndDate(samplingTime).getField2());
            data.setClassName(QtManagerConstant.QtClassesEnum.code(data.getClassId()).getName());
            //配料付数
            List<QtDaySamplingDTO> ingredientList = taskEntry.getValue().stream().filter(v -> Objects.nonNull(v.getIngredientNum())).collect(Collectors.toList());
            if (CollUtil.isEmpty(ingredientList)) {
                data.setIngredientNum(null);
            } else {
                int sum = ingredientList.stream().mapToInt(QtDaySamplingDTO::getIngredientNum).sum();
                data.setIngredientNum(sum);
            }
            dataList.add(data);
            List<String> itemList = new ArrayList<>();
            for (int i = 0; i < idx; i++) {
                itemList.add("-");
            }
            //构造检验项
            for (QtDaySamplingDTO qtDaySamplingDTO : taskEntry.getValue()) {
                Integer index = idxMap.get(qtDaySamplingDTO.getItemId());
                QtCheckItem qtCheckItem = itemMap.get(qtDaySamplingDTO.getItemId());
                if (Objects.nonNull(index) && Objects.nonNull(qtCheckItem)) {
                    String checkResult = null;
                    //如果检验结果为空，则默认赋值
                    if (Objects.isNull(qtDaySamplingDTO.getCheckResult()) || StrUtil.isBlank(qtDaySamplingDTO.getCheckResult())) {
                        checkResult = Strings.EMPTY;
                    } else {
                        checkResult = qtDaySamplingDTO.getCheckResult();
                        if (sysDictVO.getId().equals(qtCheckItem.getDataType())) {
                            checkResult += '%';
                        } else {
                            checkResult += dictIdNameMap.getOrDefault(qtCheckItem.getMeasureUnit(), Strings.EMPTY);
                        }
                    }
                    itemList.set(index, checkResult);
                }
            }
            data.setItemList(itemList);
        }
        //对dataList排序
        //1级排序：早班在上、晚班在下
        //2级排序：按取样时间升序排列（但时段显示格式为hh:mm，隐藏日期），例如9-21 23:00<9-22 1:00，则显示为23:00在上，1:00在下
        dataList.sort((o1, o2) -> {
            if (!o1.getClassId().equals(o2.getClassId())) {
                return o1.getClassId().compareTo(o2.getClassId());
            }
            return o1.getSamplingTimeLdt().compareTo(o2.getSamplingTimeLdt());
        });
        resVo.setDataList(dataList);
        return resVo;
    }

    @Override
    public void samplingDownload(LocalDate date, HttpServletResponse response) throws IOException {
        QtDaySamplingVO sampling = sampling(date);
        List<QtDaySamplingDataVO> sourceData = sampling.getDataList();
        SteccnTableAssertUtil.notEmpty(sourceData, "数据为空");
        //写表头
        List<String> dynamicHeadList = sampling.getDynamicHeadList();
        List<List<String>> list = new ArrayList<>();
        Arrays.asList("班次", "取样时间", "配料付数", "检验结果", "备注").forEach(ele -> list.add(Lists.newArrayList(ele)));
        dynamicHeadList.forEach(ele -> list.add(Lists.newArrayList(ele)));
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("配料抽检日报表", "UTF-8");
        fileName += LocalDateUtil.localDateFormat(LocalDate.now(), "yyyy-MM-dd");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        //数据
        List<List<Object>> dataList = Lists.newArrayList();
        sourceData.forEach(ele -> {
            List<Object> dataEle = Lists.newArrayList();
            dataEle.add(ele.getClassName());
            dataEle.add(ele.getSamplingTime());
            dataEle.add(ele.getIngredientNum());
            dataEle.add(ele.getResult());
            dataEle.add(ele.getNote());
            for (String item : ele.getItemList()) {
                dataEle.add(item);
            }
            dataList.add(dataEle);
        });
        int[] mergeColumnIndex = {0};
        ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).build();
        WriteSheet writeSheet1 = EasyExcel.writerSheet(0, "配料抽检日报表")
                .head(list)
                .registerWriteHandler(new RowHeightStyleStrategy(30, 20))
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .registerWriteHandler(new HeadContentStyleStrategy())
                .registerWriteHandler(new ExcelFillCellMergeStrategy(1, mergeColumnIndex))
                .build();
        excelWriter.write(dataList, writeSheet1);
        excelWriter.finish();
        response.flushBuffer();
    }

    /**
     * 损耗日报表
     *
     * @param dto
     * @return
     */
    @Override
    public QtDayReportLossVO loss(QtLossReportDTO dto) {
        //1、拿到全量数据表，若是月则修改数据
        if (dto.getType().equals(QtManagerConstant.LossReportType.MONTH.getCode())) {
            dto.setStartDate(dto.getStartDate().withDayOfMonth(1));
            dto.setEndDate(dto.getEndDate().withDayOfMonth(dto.getEndDate().lengthOfMonth()));
        }
        List<QtChartSubDetailDTO> allData = Lists.newArrayList();
        QtChartParamDTO chartParam = new QtChartParamDTO();
        //如果为空则传基片二车间下所有产线
        if (CollUtil.isEmpty(dto.getProductLineIds())) {
            SysOfficeParentDTO officeDTO = new SysOfficeParentDTO();
            officeDTO.setOfficeType(QtManagerConstant.NUM_9.toString());
            officeDTO.setId(QtManagerConstant.QT_JP_2_ID);
            List<SysOfficeVO> list = FeignOptional.of(() -> remoteOfficeFeign.listByParentId(officeDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Set<Integer> officeSet = Sets.newHashSet();
            list.forEach(tmp -> {
                officeSet.add(tmp.getId());
            });
            dto.setProductLineIds(officeSet);
        }
        for (Integer tmp : dto.getProductLineIds()) {
            chartParam.setProductLineId(tmp);
            chartParam.setDateStart(dto.getStartDate());
            chartParam.setDateEnd(dto.getEndDate());
            List<QtChartSubDetailDTO> qtChartSubDetailDTOS = qtChartGenerateService.generateSubstrateLossData(chartParam);
            allData.addAll(qtChartSubDetailDTOS);
        }
        allData.forEach(v -> v.setWorkOrderDate(LocalDate.parse(v.getWorkOrderTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
        int dayCount;
        if (!dto.getType().equals(QtManagerConstant.LossReportType.MONTH.getCode())) {
            dayCount = LocalDateTimeUtil.betweenPeriod(dto.getStartDate(), dto.getEndDate()).getDays();
        } else {
            dayCount = dto.getEndDate().getMonthValue() - dto.getStartDate().getMonthValue();
        }
        //为了进循环,如果是0,那么加1
//        if(!dto.getType().equals(QtManagerConstant.LossReportType.MONTH.getCode())&&dayCount==0){
//            dayCount = 1;
//        }
        //如果不是班次相关报表则合并数据
        if (!dto.getType().equals(QtManagerConstant.LossReportType.CLASS.getCode())) {
            //如果是月报表则把所有月份相同的数据聚合
            //1、修改值日期相同的归到同一个月份
            if (dto.getType().equals(QtManagerConstant.LossReportType.MONTH.getCode())) {
                for (int i = 0; i <= dayCount; i++) {
                    LocalDate start = dto.getStartDate().plusMonths(i);
                    allData.stream()
                            .filter(v -> v.getWorkOrderDate() != null
                                    && v.getWorkOrderDate().getMonthValue() == start.getMonthValue())
                            .forEach(v -> v.setWorkOrderDate(v.getWorkOrderDate().withDayOfMonth(1)));
                }
            }
            //2、开始合并,把产线相同，日期相同，原因相同的对象合并(日报表直接合并即可)
            allData = allData.stream()
                    .collect(Collectors.toMap(
                            v -> v.getProductLineName() + "-" + v.getWorkOrderDate() + "-" + v.getReason(),
                            Function.identity(),
                            (v1, v2) -> {
                                v1.setPullAmount(BigDecimalUtil.add(v1.getPullAmount(), v2.getPullAmount()));
                                v1.setLossWeight(BigDecimalUtil.add(v1.getLossWeight(), v2.getLossWeight()));
                                //方便数据计算，所有都设置成早班
                                v1.setWorkOrder(QtManagerConstant.QtClassesEnum.DAY.getName());
                                return v1;
                            }
                    ))
                    .values()
                    .stream()
                    .collect(Collectors.toList());
            //把只有晚班的数据也调成早班
            allData = allData.stream().map(tmp -> {
                if (!tmp.getWorkOrder().equals(QtManagerConstant.QtClassesEnum.DAY.getName())) {
                    tmp.setWorkOrder(QtManagerConstant.QtClassesEnum.DAY.getName());
                }
                return tmp;
            }).collect(Collectors.toList());
        }
        //判空
        SteccnTableAssertUtil.notEmpty(allData, "损耗数据为空");
        //2、组合报表
        QtDayReportLossVO chart = new QtDayReportLossVO();
        //表头
        QtDayReportTableHeadersVO headers = new QtDayReportTableHeadersVO();
        //静态表头
        List<QtSpcHeadTitleVO> common = Lists.newArrayList();
        //动态表头
        List<BaseReportHeaderVO> module = Lists.newArrayList();
        //数据
        List<QtDayReportLossDataVO> tableData = Lists.newArrayList();
        //3、先做数据准备和排序
        //缺陷和落板原因
        Set<String> defectAndLuo = Sets.newHashSet();
        allData.forEach(tmp -> {
            defectAndLuo.add(tmp.getReason());
            //转换localDate
            if(dto.getType().equals(QtManagerConstant.LossReportType.MONTH.getCode())){
                tmp.setWorkOrderTime(tmp.getWorkOrderDate().toString());
            }else {
                tmp.setWorkOrderDate(LocalDate.parse(tmp.getWorkOrderTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }
        });
        //拿到所有的缺陷项code
        LambdaQueryWrapper defectCondition = new LambdaQueryWrapper<QtDefectiveItem>()
                .select(QtDefectiveItem::getName, QtDefectiveItem::getCode,QtDefectiveItem::getCreateTime)
                .in(QtDefectiveItem::getName, defectAndLuo)
                .eq(QtDefectiveItem::getShowFlag, QtManagerConstant.NUM_1)
                .orderByAsc(QtDefectiveItem::getCode);
        List<QtDefectiveItem> defectiveItems = qtDefectiveItemService.list(defectCondition);
        //拿到所有落版原因
        List<SysDictVO> destroyReason = FeignOptional.of(() -> remoteDictFeignApi.listByType("sys_destory_reason"), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //根据sort排序
        destroyReason = destroyReason.stream().sorted(Comparator.comparing(SysDictVO::getSort)).collect(Collectors.toList());
        //拿到所有产线并排序
        Map<Integer, SysOfficeVO> sysOfficeVOMap = FeignOptional.of(() -> remoteOfficeFeign.listById(dto.getProductLineIds()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<SysOfficeVO> productLineList = sysOfficeVOMap.values().stream().collect(Collectors.toList());
        productLineList.stream().forEach(tmp -> {
            tmp.setOfficeNamePrefix(tmp.getOfficeName().substring(0, 2));
        });
        productLineList = productLineList.stream().sorted(Comparator.comparing(SysOfficeVO::getOfficeNamePrefix).thenComparing(SysOfficeVO::getTreeSort)).collect(Collectors.toList());

        //先根据日期分组
        Map<LocalDate, List<QtChartSubDetailDTO>> groupByDate = allData.stream().collect(Collectors.groupingBy(QtChartSubDetailDTO::getWorkOrderDate));
        //4、组表头
        //静态表头
        common.add(new QtSpcHeadTitleVO("产线", "productLineName"));
        if (dto.getType().equals(QtManagerConstant.LossReportType.CLASS.getCode())) {
            common.add(new QtSpcHeadTitleVO("班次", "className"));
        }
        common.add(new QtSpcHeadTitleVO("缺陷", "defectName"));
        headers.setCommon(common);
        //动态表头
        List<String> head = Lists.newArrayList();
        if (dto.getType().equals(QtManagerConstant.LossReportType.CLASS.getCode())) {
            head.add("班组");
        }
        head.add("重量(吨)");
        head.add("损耗率");
        if (!dto.getType().equals(QtManagerConstant.LossReportType.MONTH.getCode())) {
            for (int i = 0; i <= dayCount; i++) {
                LocalDate date = dto.getStartDate().plusDays(i);
                module.add(new BaseReportHeaderVO(date.getMonthValue() + "月" + date.getDayOfMonth() + "日", head));
            }
        } else {
            for (int i = 0; i <= dayCount; i++) {
                LocalDate date = dto.getStartDate().plusMonths(i);
                module.add(new BaseReportHeaderVO(date.getYear() + "年" + date.getMonthValue() + "月", head));
            }
        }
        //小计
        List<String> countHead = Lists.newArrayList();
        countHead.add("重量(吨)");
        countHead.add("损耗率");
        module.add(new BaseReportHeaderVO("小计", countHead));
        headers.setCommon(common);
        headers.setModule(module);
        chart.setHeaders(headers);
        //5、组数据
        for (SysOfficeVO productLine : productLineList) {
            //分早班晚班循环 0是早班 1是晚班
            for (int j = 0; j < QtManagerConstant.NUM_2; j++) {
                //拉引量
                QtDayReportLossDataVO qtDayReportLossDataPullAmount = new QtDayReportLossDataVO();
                //掰边损耗
                QtDayReportLossDataVO qtDayReportLossDataBai = new QtDayReportLossDataVO();
                //静态数据
                qtDayReportLossDataPullAmount.setProductLineName(productLine.getOfficeName());
                qtDayReportLossDataPullAmount.setType(QtManagerConstant.NUM_0);
                qtDayReportLossDataBai.setProductLineName(productLine.getOfficeName());
                qtDayReportLossDataBai.setType(QtManagerConstant.NUM_0);
                //算代表的班次
                String className = QtManagerConstant.QtClassesEnum.code(j).getName();
//                String classGroupName = allData.stream().filter(v -> v.getProductLineName().equals(productLine.getOfficeName())
//                        && v.getWorkOrder().equals(className)
//                        &&v.getWorkOrderTime().equals()).collect(Collectors.toList());
                //如果是班次报表则显示班次
                if (dto.getType().equals(QtManagerConstant.LossReportType.CLASS.getCode())) {
                    qtDayReportLossDataPullAmount.setClassName(className);
                    qtDayReportLossDataBai.setClassName(className);
                }
                //拉引量动态数据
                List<FurtherValueStrVO> modulePullTmp = Lists.newArrayList();
                //掰边损耗动态数据
                List<FurtherValueStrVO> moduleBaiTmp = Lists.newArrayList();
                //获取该产线下该班次的所有缺陷项和落版
                List<QtChartSubDetailDTO> dataTmp = allData.stream().filter(v -> v.getProductLineName().equals(productLine.getOfficeName()) && v.getWorkOrder().equals(className)).collect(Collectors.toList());
                List<String> reasonTmp = dataTmp.stream().map(QtChartSubDetailDTO::getReason).collect(Collectors.toList());
                //缺陷项排序数据
                List<QtDefectiveItem> defectiveItemsTmp = defectiveItems.stream().filter(v -> reasonTmp.contains(v.getName())).sorted(Comparator.comparing(QtDefectiveItem::getCreateTime)).collect(Collectors.toList());
                //落板排序数据
                List<SysDictVO> destroyReasonTmp = destroyReason.stream().filter(v -> reasonTmp.contains(v.getName())).sorted(Comparator.comparing(SysDictVO::getSort)).collect(Collectors.toList());
                //合并原因
                List<String> reasonStr = Lists.newArrayList();
                if (CollUtil.isNotEmpty(defectiveItemsTmp)) {
                    defectiveItemsTmp.forEach(v -> {
                        reasonStr.add(v.getName());
                    });
                }
                if (CollUtil.isNotEmpty(destroyReasonTmp)) {
                    destroyReasonTmp.forEach(v -> {
                        if(!reasonStr.contains(v.getName())){
                            reasonStr.add(v.getName());
                        }
                    });
                }
                //一天的损耗统计
                Map<LocalDate, BigDecimal> countLossData = Maps.newHashMap();
                //一天的拉引量统计
                Map<LocalDate, BigDecimal> countPullData = Maps.newHashMap();
                //先算拉引量和掰边损耗
                //根据日期
                //表示该产线是否有数据
                Boolean haveData = false;
                for (int i = 0; i <= dayCount; i++) {
                    List<QtChartSubDetailDTO> dayData = Lists.newArrayList();
                    //月报表则累加月份
                    if (dto.getType().equals(QtManagerConstant.LossReportType.MONTH.getCode())) {
                        dayData = groupByDate.get(dto.getStartDate().plusMonths(i));
                    } else {
                        dayData = groupByDate.get(dto.getStartDate().plusDays(i));
                    }
                    //获取当前产线
//                    if(Objects.nonNull(dayData)){
//                        dayData = dayData.stream().filter(v->v.getProductLineName().equals(productLine.getOfficeName())).collect(Collectors.toList());
//                    }
                    //获取当前产线并过滤班次数据
                    if (Objects.nonNull(dayData)) {
                        dayData = dayData.stream().filter(
                                v -> v.getProductLineName().equals(productLine.getOfficeName())
                                        && v.getWorkOrder() != null && v.getWorkOrder().equals(className)).collect(Collectors.toList());
                    }
                    //拉引量
                    qtDayReportLossDataPullAmount.setDefectName("拉引量");
                    //掰边损耗
                    qtDayReportLossDataBai.setDefectName("掰边损耗");
                    //拉引量
                    List<String> lineDataPullTmp = Lists.newArrayList();
                    //掰边损耗
                    List<String> lineDataLossTmp = Lists.newArrayList();
                    //如果是班次则放入班组
                    if (dto.getType().equals(QtManagerConstant.LossReportType.CLASS.getCode())) {
                        if (CollUtil.isEmpty(dayData)) {
                            lineDataPullTmp.add(null);
                            lineDataLossTmp.add(null);
                        } else {
                            lineDataPullTmp.add(dayData.get(0).getClassGroupName());
                            lineDataLossTmp.add(dayData.get(0).getClassGroupName());
                        }
                    }

                    //拉引量 损耗量
                    BigDecimal pullAmount = BigDecimal.ZERO;
                    BigDecimal lossAmount = BigDecimal.ZERO;
                    if (CollUtil.isEmpty(dayData)) {
                        lineDataPullTmp.add(null);
                        lineDataPullTmp.add("");
                        lineDataLossTmp.add("");
                    } else {
                        //算拉引量和掰边损耗
                        QtChartSubDetailDTO pullAndLoss = dayData.stream().filter(v -> v.getWorkOrder().equals(className) && v.getReason().equals("掰边损耗")).findFirst().orElse(null);
                        if (Objects.nonNull(pullAndLoss)) {
                            pullAmount = BigDecimalUtil.add(pullAmount, pullAndLoss.getPullAmount());
                            lossAmount = BigDecimalUtil.add(lossAmount, pullAndLoss.getLossWeight());
                            haveData = true;
                        }
                        lineDataPullTmp.add(pullAmount.toString());
                        lineDataPullTmp.add("");
                        lineDataLossTmp.add(lossAmount.toString());
                        countLossData.put(dto.getStartDate().plusDays(i), lossAmount);
                        countPullData.put(dto.getEndDate().plusDays(i), pullAmount);
                    }
                    //损耗
                    BigDecimal lossRate = BigDecimalUtil.divide(lossAmount, pullAmount, 4);
                    if (pullAmount.compareTo(BigDecimal.ZERO) == 0) {
                        lineDataLossTmp.add(null);
                    } else {
                        lineDataLossTmp.add(new DecimalFormat("0.00%").format(lossRate));
                    }
//                    if(CollUtil.isEmpty(dayData)){
//                        modulePullTmp.add(new FurtherValueStrVO(lineDataPullTmp));
//                    }else {
//                        modulePullTmp.add(new FurtherValueStrVO(lineDataPullTmp));
//                        moduleBaiTmp.add(new FurtherValueStrVO(lineDataLossTmp));
//                    }
                    modulePullTmp.add(new FurtherValueStrVO(lineDataPullTmp));
                    moduleBaiTmp.add(new FurtherValueStrVO(lineDataLossTmp));
                }
                //小计
                //拉引量
                List<String> lineDataPullTmp1 = Lists.newArrayList();
                List<String> lineDataLossTmp1 = Lists.newArrayList();
                //拉引量小计
                BigDecimal pullAmountAll = BigDecimal.ZERO;
                //掰边损耗小计
                BigDecimal baiLossAmountAll = BigDecimal.ZERO;
                //查看是否有数据
                if (!haveData) {
                    lineDataPullTmp1.add(null);
                    lineDataLossTmp1.add(null);
                } else {
                    pullAmountAll = countHorizontal(modulePullTmp);
                    baiLossAmountAll = countHorizontal(moduleBaiTmp);
                    lineDataPullTmp1.add(pullAmountAll.toString());
                    lineDataLossTmp1.add(baiLossAmountAll.toString());
                }
                //查看该产线是否有数据
                if (!haveData) {
                    lineDataLossTmp1.add(null);
                    lineDataPullTmp1.add("");
                } else {
                    lineDataLossTmp1.add(new DecimalFormat("0.00%").format(BigDecimalUtil.divide(baiLossAmountAll, pullAmountAll, 4)));
                    lineDataPullTmp1.add("");
                }
                modulePullTmp.add(new FurtherValueStrVO(lineDataPullTmp1));
                moduleBaiTmp.add(new FurtherValueStrVO(lineDataLossTmp1));
                qtDayReportLossDataPullAmount.setModule(modulePullTmp);
                qtDayReportLossDataBai.setModule(moduleBaiTmp);
                tableData.add(qtDayReportLossDataPullAmount);
                if (haveData) {
                    tableData.add(qtDayReportLossDataBai);
                }
                //后算缺陷项和落版原因
                if (CollUtil.isNotEmpty(reasonStr)) {
                    for (String tmp : reasonStr) {
                        QtDayReportLossDataVO defectLine = new QtDayReportLossDataVO();
                        //静态数据
                        defectLine.setProductLineName(productLine.getOfficeName());
                        defectLine.setType(QtManagerConstant.NUM_0);
                        defectLine.setDefectName(tmp);
                        //班次则加入班次
                        if (dto.getType().equals(QtManagerConstant.LossReportType.CLASS.getCode())) {
                            defectLine.setClassName(className);
                        }
                        //动态数据
                        List<FurtherValueStrVO> moduleTmp = Lists.newArrayList();
                        BigDecimal lossAmountAll = BigDecimal.ZERO;
                        for (int i = 0; i <= dayCount; i++) {
                            LocalDate tmpDate;
                            if (dto.getType().equals(QtManagerConstant.LossReportType.MONTH.getCode())) {
                                tmpDate = dto.getStartDate().plusMonths(i);
                            } else {
                                tmpDate = dto.getStartDate().plusDays(i);
                            }
                            List<QtChartSubDetailDTO> dayData = groupByDate.get(tmpDate);
                            if (Objects.nonNull(dayData)) {
                                dayData = dayData.stream().filter(v -> v.getProductLineName().equals(productLine.getOfficeName())).collect(Collectors.toList());
                            }
                            List<String> lineDataTmp = Lists.newArrayList();
                            //是班次则加入班组
                            if (dto.getType().equals(QtManagerConstant.LossReportType.CLASS.getCode())) {
                                //拿拉引量的班组
                                if (CollUtil.isEmpty(dayData)) {
                                    lineDataTmp.add(null);
                                }
//                                else {
//                                    lineDataTmp.add(dayData.get(0).getClassGroupName());
//                                }
                            }
                            //早班对象和晚班对象
                            if (CollUtil.isNotEmpty(dayData)) {
                                //是班次则加入班组
                                QtChartSubDetailDTO amountDay = dayData.stream().filter(v -> v.getWorkOrder().equals(className) && v.getReason().equals(tmp)).findFirst().orElse(null);
                                //找到当天的拉引量,同一天取相同的拉引量
                                QtChartSubDetailDTO pullAmountDay = dayData.stream().filter(v -> v.getWorkOrder().equals(className) && v.getReason().equals("掰边损耗")).findFirst().orElse(null);
                                BigDecimal lossAmount = BigDecimal.ZERO;
                                BigDecimal pullAmount = BigDecimal.ZERO;
                                if (Objects.nonNull(amountDay)) {
                                    if (dto.getType().equals(QtManagerConstant.LossReportType.CLASS.getCode())) {
                                        //特殊处理,与拉引量一致
                                        if(Objects.nonNull(pullAmountDay)&&Objects.nonNull(pullAmountDay.getClassGroupName())){
                                            lineDataTmp.add(pullAmountDay.getClassGroupName());
                                        }else{
                                            lineDataTmp.add(amountDay.getClassGroupName());
                                        }
                                    }
                                    lossAmount = BigDecimalUtil.add(lossAmount, amountDay.getLossWeight());
                                    //找到当天的拉引量,同一天取相同的拉引量
                                    if(Objects.nonNull(pullAmountDay)&&pullAmountDay.getPullAmount()!=null){
                                        pullAmount = BigDecimalUtil.add(pullAmount, pullAmountDay.getPullAmount());
                                    }else {
                                        pullAmount = BigDecimalUtil.add(pullAmount, amountDay.getPullAmount());
                                    }
                                }
                                //加入班组
//                                if (dto.getType().equals(QtManagerConstant.LossReportType.CLASS.getCode())) {
//                                    if(Objects.nonNull(pullAmountDay)&&Objects.nonNull(pullAmountDay.getClassGroupName())){
//                                        lineDataTmp.add(pullAmountDay.getClassGroupName());
//                                    }
//                                }
                                lineDataTmp.add(lossAmount.toString());
                                lineDataTmp.add(new DecimalFormat("0.00%").format(BigDecimalUtil.divide(lossAmount, pullAmount, 4)));
                            } else {
                                lineDataTmp.add(null);
                                lineDataTmp.add(null);
                            }
                            moduleTmp.add(new FurtherValueStrVO(lineDataTmp));
                        }
                        //小计
                        List<String> lineDataTmp = Lists.newArrayList();
                        lossAmountAll = countHorizontal(moduleTmp);
                        lineDataTmp.add(lossAmountAll.toString());
                        lineDataTmp.add(new DecimalFormat("0.00%").format(BigDecimalUtil.divide(lossAmountAll, pullAmountAll, 4)));
                        moduleTmp.add(new FurtherValueStrVO(lineDataTmp));
                        defectLine.setModule(moduleTmp);
                        tableData.add(defectLine);
                    }

                }
                //如果是班次报表则算损耗小计，否则算损耗合计
                if (haveData) {
                    List<QtDayReportLossDataVO> tableDataTmp = Lists.newArrayList();
                    if (dto.getType().equals(QtManagerConstant.LossReportType.CLASS.getCode())) {
                        tableDataTmp = tableData.stream().filter(v -> v.getProductLineName() != null && v.getClassName() != null
                                && v.getProductLineName().equals(productLine.getOfficeName())
                                && v.getClassName().equals(className)).collect(Collectors.toList());
                    } else {
                        tableDataTmp = tableData.stream().filter(v -> v.getProductLineName() != null
                                && v.getProductLineName().equals(productLine.getOfficeName())).collect(Collectors.toList());
                    }
                    //算总计
                    QtDayReportLossDataVO lossLine = countAmount(tableDataTmp, dto.getType());
                    //如果是班次则算损耗小计，否则算损耗合计
                    if (dto.getType().equals(QtManagerConstant.LossReportType.CLASS.getCode())) {
                        lossLine.setClassName(className);
                        lossLine.setProductLineName(productLine.getOfficeName());
                        lossLine.setType(QtManagerConstant.NUM_1);
                    } else {
                        lossLine.setType(QtManagerConstant.NUM_2);
                    }
                    tableData.add(lossLine);
                }
                //如果不是班次报表则跳出循环,日和月报表的全量数据表已经合并成早班
                if (!dto.getType().equals(QtManagerConstant.LossReportType.CLASS.getCode())) {
                    break;
                }
            }
            //如果是班次报表则算损耗合计
            if (dto.getType().equals(QtManagerConstant.LossReportType.CLASS.getCode())) {
                List<QtDayReportLossDataVO> tableDataTmp = Lists.newArrayList();
                //如果是班次报表则算损耗合计，否则算损耗总计
                //把拉引量和小计数据放入
                tableDataTmp = tableData.stream().filter(v -> (v.getDefectName() != null && v.getDefectName().equals("拉引量"))
                        && (v.getProductLineName() != null && v.getProductLineName().equals(productLine.getOfficeName()))
                        || (v.getType().equals(QtManagerConstant.NUM_1)
                        && (v.getProductLineName() != null && v.getProductLineName().equals(productLine.getOfficeName())))).collect(Collectors.toList());
                //size小于2说明没有数据
                if (tableDataTmp.size() > QtManagerConstant.NUM_2) {
                    QtDayReportLossDataVO lossLine = countAmount(tableDataTmp, dto.getType());
                    lossLine.setType(QtManagerConstant.NUM_2);
                    tableData.add(lossLine);
                }
            }
        }
        //总计
        //找到所有type=2的数据和拉引量的数据
        List<QtDayReportLossDataVO> type2 = Lists.newArrayList();
        type2 = tableData.stream().filter(v -> (v.getDefectName() != null
                && v.getDefectName().equals("拉引量")) || v.getType().equals(QtManagerConstant.NUM_2)).collect(Collectors.toList());
//        type2.addAll(tableData.stream().filter(v->v.getType().equals(QtManagerConstant.NUM_2)).collect(Collectors.toList()));
        //只有大于车间的大小才有总计
        if (type2.size() > productLineList.size()) {
            //算损耗总计
            QtDayReportLossDataVO lossAllLine = countAmount(type2, dto.getType());
            lossAllLine.setDefectName("损耗总计");
            lossAllLine.setProductLineName("损耗总计");
            lossAllLine.setType(QtManagerConstant.NUM_3);
            //算拉引量总计
            List<BigDecimal> laAmountAll = countColumn(tableData.stream().filter(v -> v.getDefectName() != null && v.getDefectName().equals("拉引量")).collect(Collectors.toList()));
            QtDayReportLossDataVO laAmountAllLine = new QtDayReportLossDataVO();
            laAmountAllLine.setDefectName("拉引量总计");
            laAmountAllLine.setProductLineName("拉引量总计");
            laAmountAllLine.setType(QtManagerConstant.NUM_3);
            List<FurtherValueStrVO> laAmountModule = Lists.newArrayList();
            for(int i = 0; i < laAmountAll.size(); i++){
                if(dto.getType().equals(QtManagerConstant.LossReportType.CLASS.getCode())&&i!=laAmountAll.size()-1){
                    laAmountModule.add(new FurtherValueStrVO(Lists.newArrayList("",laAmountAll.get(i).toString(),"")));
                }else {
                    laAmountModule.add(new FurtherValueStrVO(Lists.newArrayList(laAmountAll.get(i).toString(),"")));
                }
            }
//            laAmountAll.forEach(tmp->{
//                //如果是班次报表则需要空一行
//                if(dto.getType().equals(QtManagerConstant.LossReportType.CLASS.getCode())){
//                    laAmountModule.add(new FurtherValueStrVO(Lists.newArrayList("",tmp.toString(),"")));
//                }else {
//                    laAmountModule.add(new FurtherValueStrVO(Lists.newArrayList(tmp.toString(),"")));
//                }
//            });
            laAmountAllLine.setModule(laAmountModule);
            tableData.add(laAmountAllLine);
            //算其他损耗
            List<String> defectList = tableData.stream()
                    .filter(v -> v.getDefectName() != null && !v.getDefectName().equals("拉引量") && !v.getDefectName().equals("拉引量总计"))
                    .map(QtDayReportLossDataVO::getDefectName).distinct().collect(Collectors.toList());
            defectList.forEach(tmp -> {
                QtDayReportLossDataVO defectLine = new QtDayReportLossDataVO();
                defectLine.setDefectName(tmp+"总计");
                defectLine.setProductLineName(tmp+"总计");
                defectLine.setType(QtManagerConstant.NUM_3);
                List<BigDecimal> defectAmountAll = countColumn(tableData.stream().filter(v -> v.getDefectName() != null && v.getDefectName().equals(tmp)).collect(Collectors.toList()));
                List<FurtherValueStrVO> defectModule = Lists.newArrayList();
                for(int i = 0; i < defectAmountAll.size(); i++){
                    BigDecimal decimal = defectAmountAll.get(i);
                    BigDecimal percent = BigDecimalUtil.percent(decimal, laAmountAll.get(i), 2);
                    if(Objects.isNull(percent)){
                        percent = BigDecimal.ZERO;
                    }
                    //如果是班次报表则需要空一行
                    if(dto.getType().equals(QtManagerConstant.LossReportType.CLASS.getCode())&&i!=defectAmountAll.size()-1){
                        defectModule.add(new FurtherValueStrVO(Lists.newArrayList("",decimal.toString(),percent.toString()+"%")));
                    }else {
                        defectModule.add(new FurtherValueStrVO(Lists.newArrayList(decimal.toString(),percent.toString()+"%")));
                    }
                }
                defectLine.setModule(defectModule);
                tableData.add(defectLine);
            });
            tableData.add(lossAllLine);

        }
        //如果是班次报表则处理班组数据,和拉引量一致
//        if(dto.getType().equals(QtManagerConstant.LossReportType.CLASS.getCode())){
//            //拿到拉引量的班组
//            QtDayReportLossDataVO tableDataLaTmp = tableData.stream().filter(v -> v.getDefectName() != null && v.getDefectName().equals("拉引量")).findFirst().orElse(null);
//            if(Objects.nonNull(tableDataLaTmp)){
//                tableData.stream().filter(v->v.getType().equals(OaConstant.NO)).forEach(v -> {
//                    for(int i = 0;i<v.getModule().size();i++){
//                        if(i!=v.getModule().size()-1){
//                            v.getModule().get(i).getValue().set(0,tableDataLaTmp.getModule().get(i).getValue().get(0));
//                        }
//                    }
//                });
//            }
//
//        }
        chart.setTableData(tableData);
        return chart;
    }

    @Override
    public void lossDownload(QtLossReportDTO dto, HttpServletResponse response) throws IOException {
        lossClassDownload(dto, response);
    }


    /**
     * 下载
     * @param dto
     * @param response
     * @throws IOException
     */
    private void lossClassDownload(QtLossReportDTO dto, HttpServletResponse response) throws IOException {
        QtDayReportLossVO loss = loss(dto);
        List<QtDayReportLossDataVO> tableData = loss.getTableData();
        SteccnTableAssertUtil.notEmpty(tableData, "数据为空");
        //写表头
        List<List<String>> list = new ArrayList<>();
        QtDayReportTableHeadersVO headers = loss.getHeaders();
        headers.getCommon().forEach(ele -> list.add(Lists.newArrayList(ele.getLabel())));
        headers.getModule().forEach(ele -> ele.getValue().forEach(aa -> list.add(Lists.newArrayList(ele.getLabel(),aa))));
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("损耗报表", "UTF-8");
        fileName += LocalDateUtil.localDateFormat(LocalDate.now(), "yyyy-MM-dd");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        //数据
        List<List<Object>> dataList = Lists.newArrayList();
        tableData.forEach(ele -> {
            List<Object> dataEle = Lists.newArrayList();
            //0正常元素  1合计 2总计 3小计
            if(ele.getType() == 1) {
                dataEle.add(ele.getProductLineName());
                if (dto.getType() == 0) {
                    dataEle.add("损耗小计(单班次)");
                }
                dataEle.add("损耗小计(单班次)");
            }else if(ele.getType() ==2){
                dataEle.add("损耗合计(单产线)");
                if (dto.getType() == 0) {
                    dataEle.add("损耗合计(单产线)");
                }
                dataEle.add("损耗合计(单产线)");
            } else if (ele.getType() == 3) {
                dataEle.add(ele.getProductLineName());
                if (dto.getType() == 0) {
                    dataEle.add(ele.getProductLineName());
                }
                dataEle.add(ele.getProductLineName());
            } else {
                dataEle.add(ele.getProductLineName());
                if (dto.getType() == 0) {
                    dataEle.add(ele.getClassName());
                }
                dataEle.add(ele.getDefectName());
            }
            List<FurtherValueStrVO> module = ele.getModule();
            module.forEach(arr -> arr.getValue().forEach(val -> dataEle.add(val)));
            dataList.add(dataEle);
        });
        int[] mergeColumnIndex = {0, 1};
        int[] mergeColumnIndex2 = {0, 1, 2};
        if(dto.getType() ==0){
            mergeColumnIndex = mergeColumnIndex2;
        }
        ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).build();
        WriteSheet writeSheet1 = EasyExcel.writerSheet(0, "损耗报表")
                .head(list)
                .registerWriteHandler(new RowHeightStyleStrategy(30, 20))
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .registerWriteHandler(new HeadContentStyleStrategy())
                .registerWriteHandler(new ExcelFillCellMergeStrategy(1, mergeColumnIndex))
                .build();
        excelWriter.write(dataList, writeSheet1);
        excelWriter.finish();
        response.flushBuffer();
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

    /**
     * 横向累加算损耗量
     *
     * @return
     */
    private BigDecimal countHorizontal(List<FurtherValueStrVO> horizon) {
        BigDecimal result = BigDecimal.ZERO;
        if (CollUtil.isNotEmpty(horizon)) {
            for (FurtherValueStrVO tmp : horizon) {
                String strTmp = tmp.getValue().get(0);
                if (StrUtil.isNotBlank(strTmp)) {
                    if (!NumberUtils.isCreatable(strTmp)) {
                        strTmp = tmp.getValue().get(1);
                    }
//                    if(!StrUtil.isNumeric(strTmp)){
//                        //如果不是数字则跳过这一次循环
//                        continue;
//                    }
                    result = BigDecimalUtil.add(result, new BigDecimal(strTmp));
                }
            }
        }
        return result;
    }

    /**
     * 列相加
     *
     * @param tableData
     * @return
     */
    private List<BigDecimal> countColumn(List<QtDayReportLossDataVO> tableData) {
        List<BigDecimal> bigDecimalList = Lists.newArrayList();
        if (CollUtil.isNotEmpty(tableData)) {
            //先统计每个module中value的情况
            int valueSize1 = tableData.get(0).getModule().get(0).getValue().size();
            int valueSize2 = tableData.get(0).getModule().get(tableData.get(0).getModule().size() - 1).getValue().size();
            //有班组的情况
            List<String> classNameList = Lists.newArrayList();
            if (valueSize1 != valueSize2) {
                tableData.forEach(tableTmp -> {
                    tableTmp.getModule().forEach(moduleTmp -> {
                        if (moduleTmp.getValue().size() == valueSize1) {
                            classNameList.add(moduleTmp.getValue().remove(0));
                        }
                    });
                });
            }
            //列
            int col = tableData.get(0).getModule().size();
            //行
            int row = tableData.size();
            //列不动动行
            for (int i = 0; i < col; i++) {
                BigDecimal count = BigDecimal.ZERO;
                for (int j = 0; j < row; j++) {
                    String numStr = "";
                    //为空时是总计
                    if (tableData.get(j).getModule().get(i).getValue().get(0) != null && !tableData.get(j).getModule().get(i).getValue().get(0).equals("")) {
                        numStr = tableData.get(j).getModule().get(i).getValue().get(0);
                    } else {
                        numStr = tableData.get(j).getModule().get(i).getValue().get(1);
                    }
                    //空串说明是班组那一列
                    if (StrUtil.isNotBlank(numStr)) {
                        count = BigDecimalUtil.add(count, new BigDecimal(numStr));
                    }
                }
                bigDecimalList.add(count);
            }
            //复原tableData
            if (valueSize1 != valueSize2) {
                String classNameTmp = "";
                for(QtDayReportLossDataVO tableTmp:tableData){
                    int moduleSize = tableTmp.getModule().size();
                    //不包括最有一个
                    for (int i = 0; i < moduleSize - 1; i++) {
                        if(classNameList.size()!=0){
                            classNameTmp = classNameList.remove(0);
                        }
                        tableTmp.getModule().get(i).getValue().add(0, classNameTmp);
                    }
                }
//                tableData.forEach(tableTmp -> {

//                    tableTmp.getModule().forEach(moduleTmp->{
//                        if(moduleTmp.getValue().size()==valueSize1){
//
//                            classNameList.add(moduleTmp.getValue().remove(0));
//                        }
//                    });
//                });
            }
        }
        return bigDecimalList;
    }

    /**
     * 算合计一行
     *
     * @param tableDataTmp
     * @param reportType   报表类型，如果是班次报表则需要做特殊处理
     * @return
     */
    private QtDayReportLossDataVO countAmount(List<QtDayReportLossDataVO> tableDataTmp, Integer reportType) {
        QtDayReportLossDataVO lossLine = new QtDayReportLossDataVO();
        //拿到该行的拉引量
        List<QtDayReportLossDataVO> pullAmountData = tableDataTmp.stream().filter(v -> v.getDefectName() != null
                && v.getDefectName().equals("拉引量")).collect(Collectors.toList());
        //过滤掉拉引量
        tableDataTmp = tableDataTmp.stream().filter(v -> StrUtil.isBlank(v.getDefectName()) || !v.getDefectName().equals("拉引量")).collect(Collectors.toList());
        //损耗动态数据
        List<FurtherValueStrVO> lossLineModule = Lists.newArrayList();
        //算列相加
        List<BigDecimal> bigDecimalList = countColumn(tableDataTmp);
        //算拉引量列相加
        List<BigDecimal> pullAmountBigDecimal = countColumn(pullAmountData);
        for (int i = 0; i < bigDecimalList.size(); i++) {
            List<String> lossAmount = Lists.newArrayList();
            //如果是班次报表则需要空出班组列
            if (reportType.equals(QtManagerConstant.LossReportType.CLASS.getCode()) && i != bigDecimalList.size() - 1) {
                lossAmount.add("");
            }
            lossAmount.add(bigDecimalList.get(i).toString());
            if (pullAmountBigDecimal.get(i).compareTo(BigDecimal.ZERO) == 0) {
                lossAmount.add(null);
            } else {
                lossAmount.add(new DecimalFormat("0.00%").format(BigDecimalUtil.divide(bigDecimalList.get(i), pullAmountBigDecimal.get(i), 4)));
            }
            lossLineModule.add(new FurtherValueStrVO(lossAmount));
        }
        //小计
//        List<String> lossLineCount = Lists.newArrayList();
        //横向数据累加
//        BigDecimal lossAmount = countHorizontal(lossLineModule);
//        lossLineCount.add(lossAmount.toString());
//        String pullAmountAllStr = pullLine.getModule().get(pullLine.getModule().size()).getValue().get(0);
//        BigDecimal pullAmountAll = new BigDecimal(pullAmountAllStr);
//        lossLineCount.add(new DecimalFormat("0.00%").format(BigDecimalUtil.divide(lossAmount,pullAmountAll,4)));
//        lossLineModule.add(new FurtherValueStrVO(lossLineCount));
        lossLine.setModule(lossLineModule);
        return lossLine;
    }

}
