package com.steccn.qms.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.oa.constant.OaManagerConstant;
import com.steccn.oa.dto.OaGroupCountDTO;
import com.steccn.oa.feign.OaInfoService;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.QtCheckItem;
import com.steccn.qms.domain.QtDefectiveItem;
import com.steccn.qms.dto.QtChartParamDTO;
import com.steccn.qms.dto.QtCheckItemMaterialAvgDTO;
import com.steccn.qms.dto.QtDefectReportDTO;
import com.steccn.qms.dto.QtDestoryInfoFallDTO;
import com.steccn.qms.mapper.QtCheckTaskMapper;
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
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @desc 不良报表实现类
 * @date 2023/7/11
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QtDefectReportServiceImpl implements QtDefectReportService {

    private final QtDefectiveReportService qtDefectiveReportService;
    private final QtDefectiveItemService qtDefectiveItemService;
    private final RemoteOfficeFeign remoteOfficeFeign;
    private final OaInfoService oaInfoService;
    private final QtCheckTaskDestoryInfoService qtCheckTaskDestoryInfoService;
    private final QtCheckItemService qtCheckItemService;
    private final QtCheckTaskMapper qtCheckTaskMapper;

    @Override
    public QtDefectReportVO defectList(QtDefectReportDTO dto) {
        QtDefectReportVO resVo = new QtDefectReportVO();
        List<QtDefectReportDataVO> dataList = Lists.newArrayList();
        resVo.setDataList(dataList);
        // 查询满足以下条件的在线检任务，记为集合T：状态为已检验 产线为所在行产线 缺陷记录时段属于所在行班次日期&班次
        QtChartParamDTO request = new QtChartParamDTO();
        request.setDateStart(dto.getStartDate());
        request.setDateEnd(dto.getEndDate());
        request.setProductLineIdList(dto.getProductLineList());
        //获取人工采集的缺陷集合+获取机器采集的缺陷集合
        request.setCheckType(QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode());
        //是否加入数采数据
        request.setJoinAcquisition(0);
        List<QtCheckTaskDetailReportVO> sourceDataList = qtDefectiveReportService.defectiveDataFlat(request);
        //根据缺陷数据查询动态表头
        Set<Integer> defectIdSet = sourceDataList.stream().map(QtCheckTaskDetailReportVO::getDefectId).collect(Collectors.toSet());
        //根据快照id查询缺陷的集合-需要按照id排序
        Map<Integer, QtDefectiveItem> defectIdEntityMap = qtDefectiveItemService.listById(defectIdSet);
        List<QtDefectiveItem> defectEntityList = defectIdEntityMap.values().stream().sorted(Comparator.comparing(QtDefectiveItem::getCode)).collect(Collectors.toList());
        List<String> defectNameList = defectEntityList.stream().map(QtDefectiveItem::getName).collect(Collectors.toList());
        List<Integer> defectIdList = defectEntityList.stream().map(QtDefectiveItem::getId).collect(Collectors.toList());
        resVo.setValueHeadList(defectNameList);
        //获取班次日期
        LocalDateTime start = LocalDateUtil.getMorning(dto.getStartDate());
        LocalDateTime end = LocalDateUtil.getMorning(dto.getEndDate().plusDays(1));
        //获取粒度
        QtManagerConstant.LossReportType granularityEntity = QtManagerConstant.LossReportType.getByCode(dto.getGranularity());
        Assert.notNull(granularityEntity, "粒度不存在");
        List<LocalDateTime> shardTimeList = ShardTimeUtil.cutDateRangeNew(granularityEntity.getType(), start, end);
        //获取产线的中文
        Map<Integer, String> lineIdNameMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(Sets.newHashSet(dto.getProductLineList())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //获取班组-只有粒度为班组，才会查询班组信息
        Map<String, OaGroupCountDTO> classGroupMap = null;
        if (QtManagerConstant.LossReportType.CLASS.getCode().equals(dto.getGranularity())) {
            TupleDTO<LocalDate, LocalDate> timeTupleDTO = new TupleDTO<>(dto.getStartDate(), dto.getEndDate());
            classGroupMap = FeignOptional.of(() -> oaInfoService.getGroupInfo(timeTupleDTO), FeignConstant.STECCN_OA_SERVICE_NAME).get();
        }
        Map<String, List<QtCheckTaskDetailReportVO>> sourceMap = sourceDataList.stream().collect(Collectors.groupingBy(v -> v.getProductLineId() + "_" + v.getClassesDate() + "_" + v.getClassId() + "_" + v.getDefectId()));
        Map<Integer, Integer> allValueMap = Maps.newHashMap();

        for (Integer line : dto.getProductLineList()) {
            String lineName = lineIdNameMap.get(line);
            //合计（单产线）
            Map<Integer, Integer> lineValueMap = Maps.newHashMap();
            for (int i = 1; i < shardTimeList.size(); i++) {
                LocalDateTime begin = shardTimeList.get(i - 1);
                //根据缺陷记录时段来过滤数据集
                QtDefectReportDataVO row = new QtDefectReportDataVO();
                LocalDate classDate = LocalDateUtil.getClassDate(begin, false);
                row.setType(1);
                row.setClassDateStr(LocalDateUtil.localDateFormat(classDate, DatePattern.NORM_DATE_PATTERN));
                row.setProductLineName(lineName);
                Integer classId = LocalDateUtil.getClassId(begin, false);
                if (QtManagerConstant.LossReportType.CLASS.getCode().equals(dto.getGranularity())) {
                    QtManagerConstant.QtClassesEnum classEnum = QtManagerConstant.QtClassesEnum.code(classId);
                    row.setClassName(Objects.nonNull(classEnum) ? classEnum.getName() : Strings.EMPTY);
                    OaGroupCountDTO oaGroupCountDTO = classGroupMap.getOrDefault(classDate + ":" + row.getClassName(), new OaGroupCountDTO());
                    row.setClassGroupName(oaGroupCountDTO.getGroupName());
                }
                List<String> valueList = Lists.newArrayList();
                row.setValueList(valueList);
                for (Integer defectId : defectIdList) {
                    int sum;
                    if (QtManagerConstant.LossReportType.CLASS.getCode().equals(dto.getGranularity())) {
                        sum = sourceMap.getOrDefault(key(line, classDate, classId, defectId), Lists.newArrayList()).stream().mapToInt(QtCheckTaskDetailReportVO::getDefectNum).sum();
                    } else {
                        int sum1 = sourceMap.getOrDefault(key(line, classDate, QtManagerConstant.QtClassesEnum.DAY.getCode(), defectId), Lists.newArrayList()).stream().mapToInt(QtCheckTaskDetailReportVO::getDefectNum).sum();
                        int sum2 = sourceMap.getOrDefault(key(line, classDate, QtManagerConstant.QtClassesEnum.NIGHT.getCode(), defectId), Lists.newArrayList()).stream().mapToInt(QtCheckTaskDetailReportVO::getDefectNum).sum();
                        sum = sum2 + sum1;
                    }
                    valueList.add(String.valueOf(sum));
                    lineValueMap.put(defectId, lineValueMap.getOrDefault(defectId, 0) + sum);
                    allValueMap.put(defectId, allValueMap.getOrDefault(defectId, 0) + sum);
                }
                dataList.add(row);
            }
            dataList.add(count(1, lineName, "合计（单产线）", lineValueMap, defectIdList));
        }
        dataList.add(count(1, "总计", "总计", allValueMap, defectIdList));
        return resVo;
    }

    @Override
    public List<QtDefectFallingVO> falling(QtDefectReportDTO dto) {
        List<QtDefectFallingVO> resList = Lists.newArrayList();
        //查询所有的落板记录
        LocalDateTime start = LocalDateUtil.getMorning(dto.getStartDate());
        LocalDateTime end = LocalDateUtil.getMorning(dto.getEndDate().plusDays(1));
        List<QtDestoryInfoFallDTO> fallDTOList = qtCheckTaskDestoryInfoService.falling(dto.getProductLineList(), start, end);
        SteccnTableAssertUtil.notEmpty(fallDTOList, "落板记录集合为空");
        //处理数据
        List<QtDestoryInfoFallDTO> dealReasonDataList = dealReasonData(fallDTOList);
        //获取产线的中文
        Map<Integer, String> lineIdNameMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(Sets.newHashSet(dto.getProductLineList())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //获取粒度
        QtManagerConstant.LossReportType granularityEntity = QtManagerConstant.LossReportType.getByCode(dto.getGranularity());
        Assert.notNull(granularityEntity, "粒度不存在");
        List<LocalDateTime> shardTimeList = ShardTimeUtil.cutDateRangeNew(granularityEntity.getType(), start, end);

        TupleDTO<LocalDate, LocalDate> timeTupleDTO = new TupleDTO<>(dto.getStartDate(), dto.getEndDate());
        Map<String, OaGroupCountDTO> classGroupMap = FeignOptional.of(() -> oaInfoService.getGroupInfo(timeTupleDTO), FeignConstant.STECCN_OA_SERVICE_NAME).get();
        //合计（单产线）
        Map<Integer, BigDecimal> countMap = Maps.newHashMap();

        for (Integer line : dto.getProductLineList()) {
            //合计（单产线）
            Map<Integer, BigDecimal> lineCountMap = Maps.newHashMap();
            List<QtDestoryInfoFallDTO> sourceList = dealReasonDataList.stream().filter(ele -> line.equals(ele.getProductLineId())).collect(Collectors.toList());
            String lineName = lineIdNameMap.get(line);
            for (int i = 1; i < shardTimeList.size(); i++) {
                QtDefectFallingVO row = new QtDefectFallingVO();
                LocalDateTime begin = shardTimeList.get(i - 1);
                LocalDate classDate = LocalDateUtil.getClassDate(begin, false);
                Integer classId = LocalDateUtil.getClassId(begin, false);
                row.setType(0);
                row.setClassDateStr(LocalDateUtil.localDateFormat(classDate, DatePattern.NORM_DATE_PATTERN));
                row.setProductLineName(lineName);
                QtManagerConstant.QtClassesEnum classEnum = QtManagerConstant.QtClassesEnum.code(classId);
                row.setClassName(Objects.nonNull(classEnum) ? classEnum.getName() : Strings.EMPTY);
                OaGroupCountDTO oaGroupCountDTO = classGroupMap.getOrDefault(classDate + ":" + row.getClassName(), new OaGroupCountDTO());
                row.setClassGroupName(oaGroupCountDTO.getGroupName());
                List<QtDestoryInfoFallDTO> tempSourceList = sourceList.stream().filter(ele -> classDate.isEqual(ele.getClassTime().toLocalDate()) && classId.equals(ele.getClassId())).collect(Collectors.toList());
                List<List<QtDefectFallingDetailVO>> rowList = getDetailList(countMap, lineCountMap, tempSourceList);
                row.setDetailList(rowList);
                resList.add(row);
            }
            //增加合计（单产线）
            resList.add(reasonCount(1, "合计（单产线）", lineCountMap));
        }
        //增加总计
        resList.add(reasonCount(2, "总计", countMap));
        return resList;
    }

    @Override
    public QtCheckItemMultiAvgVO thickAvg(QtDefectReportDTO dto) {
        List<QtCheckItem> thickItemList = qtCheckItemService.list(new LambdaQueryWrapper<QtCheckItem>().eq(QtCheckItem::getCode, "JYX000100").select(QtCheckItem::getId));
        SteccnTableAssertUtil.notEmpty(thickItemList, "厚度平均值检验项不存在");
        QtCheckItemMultiAvgVO resVo = getQtCheckItemAvgVO(dto, thickItemList);
        return resVo;
    }

    @Override
    public QtCheckItemMultiAvgVO rbwAvg(QtDefectReportDTO dto) {
        List<QtCheckItem> thickItemList = qtCheckItemService.list(new LambdaQueryWrapper<QtCheckItem>().eq(QtCheckItem::getCode, "JYX000072").select(QtCheckItem::getId));
        SteccnTableAssertUtil.notEmpty(thickItemList, "毛边宽检验项不存在");
        QtCheckItemMultiAvgVO resVo = getQtCheckItemAvgVO(dto, thickItemList);
        return resVo;
    }

    @Override
    public QtCheckItemSingleAvgVO roughness(QtDefectReportDTO dto) {
        List<QtCheckItem> roughnessItemList = qtCheckItemService.list(new LambdaQueryWrapper<QtCheckItem>().eq(QtCheckItem::getCode, "JYX000105").select(QtCheckItem::getId));
        SteccnTableAssertUtil.notEmpty(roughnessItemList, "粗糙度检验项不存在");
        LocalDateTime start = LocalDateUtil.getMorning(dto.getStartDate());
        LocalDateTime end = LocalDateUtil.getMorning(dto.getEndDate().plusDays(1));
        //获取粒度
        QtManagerConstant.LossReportType granularityEntity = QtManagerConstant.LossReportType.getByCode(dto.getGranularity());
        Assert.notNull(granularityEntity, "粒度不存在");
        String type = granularityEntity.getType();
        if (QtManagerConstant.LossReportType.CLASS.equals(granularityEntity)) {
            type = QtManagerConstant.LossReportType.DAY.getType();
        }
        List<LocalDateTime> shardTimeList = ShardTimeUtil.cutDateRangeNew(type, start, end);
        List<Integer> roughnessIdList = roughnessItemList.stream().map(QtCheckItem::getId).collect(Collectors.toList());
        //获取产线的中文
        Map<Integer, String> lineIdNameMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(Sets.newHashSet(dto.getProductLineList())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

        List<QtCheckItemMaterialAvgDTO> dbList;
        List<QtCheckItemSingleAvgListVO> rowList = Lists.newArrayList();
        List<String> dateList = Lists.newArrayList();

        if (QtManagerConstant.LossReportType.CLASS.equals(granularityEntity)) {
            dbList = qtCheckTaskMapper.checkItemAvgClassGroup(dto.getProductLineList(), roughnessIdList, start, end, 3);
            SteccnTableAssertUtil.notEmpty(dbList, "粗糙检验项暂无数据");
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM月dd日");
            for (int i = 0; i < shardTimeList.size() - 1; i++) {
                dateList.add(dateTimeFormatter.format(shardTimeList.get(i)));
            }
            //遍历产线
            for (Integer line : dto.getProductLineList()) {
                String lineName = lineIdNameMap.get(line);
                for (OaManagerConstant.OaTeamGroupMapping classGroup : OaManagerConstant.OaTeamGroupMapping.values()) {
                    QtCheckItemSingleAvgListVO row = new QtCheckItemSingleAvgListVO();
                    rowList.add(row);
                    row.setProductLineName(lineName);
                    row.setClassGroupName(classGroup.getName());
                    List<BigDecimal> valueList = Lists.newArrayList();
                    row.setValueList(valueList);
                    for (int i = 1; i < shardTimeList.size(); i++) {
                        LocalDateTime begin = shardTimeList.get(i - 1);
                        LocalDate classDate = LocalDateUtil.getClassDate(begin, false);
                        Optional<BigDecimal> first = dbList.stream().filter(ele -> line.equals(ele.getProductLineId()) && classDate.isEqual(ele.getClassDate()) && classGroup.getCode().equals(ele.getClassGroupId())).map(QtCheckItemMaterialAvgDTO::getValue).findFirst();
                        if (first.isPresent()) {
                            valueList.add(first.get());
                        } else {
                            valueList.add(null);
                        }
                    }
                }
            }
        } else if (QtManagerConstant.LossReportType.DAY.equals(granularityEntity)) {
            dbList = qtCheckTaskMapper.checkItemAvgDay(dto.getProductLineList(), roughnessIdList, start, end, 3);
            SteccnTableAssertUtil.notEmpty(dbList, "粗糙检验项暂无数据");
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM月dd日");
            for (int i = 0; i < shardTimeList.size() - 1; i++) {
                dateList.add(dateTimeFormatter.format(shardTimeList.get(i)));
            }
            //遍历产线
            for (Integer line : dto.getProductLineList()) {
                String lineName = lineIdNameMap.get(line);
                QtCheckItemSingleAvgListVO row = new QtCheckItemSingleAvgListVO();
                rowList.add(row);
                row.setProductLineName(lineName);
                List<BigDecimal> valueList = Lists.newArrayList();
                row.setValueList(valueList);
                for (int i = 1; i < shardTimeList.size(); i++) {
                    LocalDateTime begin = shardTimeList.get(i - 1);
                    LocalDate classDate = LocalDateUtil.getClassDate(begin, false);
                    Optional<BigDecimal> first = dbList.stream().filter(ele -> line.equals(ele.getProductLineId()) && classDate.isEqual(ele.getClassDate())).map(QtCheckItemMaterialAvgDTO::getValue).findFirst();
                    if (first.isPresent()) {
                        valueList.add(first.get());
                    } else {
                        valueList.add(null);
                    }
                }
            }
        } else {
            dbList = qtCheckTaskMapper.checkItemAvgMonth(dto.getProductLineList(), roughnessIdList, start, end, 3);
            SteccnTableAssertUtil.notEmpty(dbList, "粗糙检验项暂无数据");
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月");
            for (int i = 0; i < shardTimeList.size() - 1; i++) {
                dateList.add(dateTimeFormatter.format(shardTimeList.get(i)));
            }
            //遍历产线
            for (Integer line : dto.getProductLineList()) {
                String lineName = lineIdNameMap.get(line);
                QtCheckItemSingleAvgListVO row = new QtCheckItemSingleAvgListVO();
                rowList.add(row);
                row.setProductLineName(lineName);
                List<BigDecimal> valueList = Lists.newArrayList();
                row.setValueList(valueList);
                for (int i = 1; i < shardTimeList.size(); i++) {
                    LocalDateTime begin = shardTimeList.get(i - 1);
                    String classMonth = LocalDateUtil.format(begin, "yyyy-MM");
                    Optional<BigDecimal> first = dbList.stream().filter(ele -> line.equals(ele.getProductLineId()) && classMonth.equals(ele.getClassMonth())).map(QtCheckItemMaterialAvgDTO::getValue).findFirst();
                    if (first.isPresent()) {
                        valueList.add(first.get());
                    } else {
                        valueList.add(null);
                    }
                }
            }
        }
        QtCheckItemSingleAvgVO resVo = new QtCheckItemSingleAvgVO();
        resVo.setRowList(rowList);
        resVo.setDateList(dateList);
        return resVo;
    }

    /**
     * 获取所有检验项参数
     *
     * @param dto           请求参数
     * @param checkItemList 检验项集合
     * @return vo
     */
    private QtCheckItemMultiAvgVO getQtCheckItemAvgVO(QtDefectReportDTO dto, List<QtCheckItem> checkItemList) {
        LocalDateTime start = LocalDateUtil.getMorning(dto.getStartDate());
        LocalDateTime end = LocalDateUtil.getMorning(dto.getEndDate().plusDays(1));
        List<Integer> thickItemIdList = checkItemList.stream().map(QtCheckItem::getId).collect(Collectors.toList());
        List<QtCheckItemMaterialAvgDTO> thickAvgDTOList = qtCheckTaskMapper.checkItemMaterialAvg(dto.getProductLineList(), thickItemIdList, start, end, 1);
        SteccnTableAssertUtil.notEmpty(thickAvgDTOList, "暂无符合条件的记录");
        //获取产线集合
        Map<Integer, String> lineIdNameMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(Sets.newHashSet(dto.getProductLineList())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<QtCheckItemMultiAvgListVO> rowList = Lists.newArrayList();
        //将start -> end 切分成班次日期
        List<LocalDateTime> shardTimeList = ShardTimeUtil.cutDateRangeNew(QtManagerConstant.QtGranularityEnum.DAY.getType(), start, end);

        QtCheckItemMultiAvgVO resVo = new QtCheckItemMultiAvgVO();
        resVo.setRowList(rowList);
        List<String> dateHeadList = Lists.newArrayList();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM月dd日");
        shardTimeList.forEach(ele -> dateHeadList.add(dateTimeFormatter.format(ele)));
        dateHeadList.remove(dateHeadList.size() - 1);
        resVo.setDateList(dateHeadList);
        for (Integer line : dto.getProductLineList()) {
            String lineName = lineIdNameMap.get(line);
            for (OaManagerConstant.OaTeamGroupMapping classGroup : OaManagerConstant.OaTeamGroupMapping.values()) {
                QtCheckItemMultiAvgListVO row = new QtCheckItemMultiAvgListVO();
                row.setProductLineName(lineName);
                row.setClassGroupName(classGroup.getName());
                rowList.add(row);
                List<List<BigDecimal>> dataList = Lists.newArrayList();
                for (int i = 1; i < shardTimeList.size(); i++) {
                    LocalDateTime begin = shardTimeList.get(i - 1);
                    LocalDate classDate = LocalDateUtil.getClassDate(begin, false);
                    List<QtCheckItemMaterialAvgDTO> filterDataList = thickAvgDTOList.stream().filter(ele -> line.equals(ele.getProductLineId()) && classDate.isEqual(ele.getClassDate()) && classGroup.getCode().equals(ele.getClassGroupId())).collect(Collectors.toList());
                    List<BigDecimal> valueList = filterDataList.stream().map(QtCheckItemMaterialAvgDTO::getValue).collect(Collectors.toList());
                    dataList.add(valueList);
                }
                row.setValueList(dataList);
            }
        }
        return resVo;
    }

    /**
     * 获取明细集合
     *
     * @param countMap     总计map
     * @param lineCountMap 产线map
     * @param sourceList   元数据集合
     * @return 明细集合
     */
    private List<List<QtDefectFallingDetailVO>> getDetailList(Map<Integer, BigDecimal> countMap, Map<Integer, BigDecimal> lineCountMap, List<QtDestoryInfoFallDTO> sourceList) {
        List<List<QtDefectFallingDetailVO>> rowList = Lists.newArrayList();

        List<List<QtDestoryInfoFallDTO>> tempList = Lists.newArrayList();
        for (Integer category : Arrays.asList(0, 1)) {
            for (Integer station : Arrays.asList(1, 2)) {
                List<QtDestoryInfoFallDTO> collect = sourceList.stream().filter(ele -> category.equals(ele.getReasonCategory()) && station.equals(ele.getDestoryWorkStation())).collect(Collectors.toList());
                tempList.add(collect);
            }
        }
        //获取tempList中集合最多的元素
        Integer maxSize = 1;
        for (List<QtDestoryInfoFallDTO> dataList : tempList) {
            if (dataList.size() > maxSize) {
                maxSize = dataList.size();
            }
        }
        //遍历行数数据
        for (int rowCt = 0; rowCt < maxSize; rowCt++) {
            //定义四列数据
            List<QtDefectFallingDetailVO> columnList = Lists.newArrayList();
            for (int idx = 0; idx < 4; idx++) {
                QtDefectFallingDetailVO column = new QtDefectFallingDetailVO();
                List<QtDestoryInfoFallDTO> dataList = tempList.get(idx);
                if (rowCt < dataList.size()) {
                    QtDestoryInfoFallDTO qtDestoryInfoFallDTO = dataList.get(rowCt);
                    column.setBeginStr(LocalDateUtil.format(qtDestoryInfoFallDTO.getDestoryStartTime(), "HH:mm"));
                    column.setOverStr(LocalDateUtil.format(qtDestoryInfoFallDTO.getDestoryEndTime(), "HH:mm"));
                    LocalDateTime ldt1 = qtDestoryInfoFallDTO.getDestoryStartTime().withSecond(0).withNano(0);
                    LocalDateTime ldt2 = qtDestoryInfoFallDTO.getDestoryEndTime().withSecond(0).withNano(0);
                    long between = LocalDateTimeUtil.between(ldt1, ldt2, ChronoUnit.MINUTES);
                    column.setIntervalHour(BigDecimalUtil.divide(BigDecimal.valueOf(between), BigDecimal.valueOf(60), 2));
                    //合计（单产线）
                    lineCountMap.put(idx, BigDecimalUtil.add(column.getIntervalHour(), lineCountMap.get(idx), 2));
                    countMap.put(idx, BigDecimalUtil.add(column.getIntervalHour(), countMap.get(idx), 2));
                    long minutes = between % 60;
                    String str = String.valueOf(minutes);
                    if (minutes < 10) {
                        str = 0 + str;
                    }
                    column.setInterval(between / 60 + ":" + str);
                    BeanUtils.copyProperties(qtDestoryInfoFallDTO, column);
                }
                columnList.add(column);
            }
            rowList.add(columnList);
        }
        return rowList;
    }

    /**
     * 获取合计行
     *
     * @param type         类型
     * @param lineName     产线名称
     * @param lineCountMap 数据合计
     * @return 合计行
     */
    private QtDefectFallingVO reasonCount(Integer type, String lineName, Map<Integer, BigDecimal> lineCountMap) {
        QtDefectFallingVO countRow = new QtDefectFallingVO();
        countRow.setType(type);
        countRow.setProductLineName(lineName);
        List<List<QtDefectFallingDetailVO>> detailCount = Lists.newArrayList();
        List<QtDefectFallingDetailVO> detailList = Lists.newArrayList();
        detailCount.add(detailList);
        for (int i = 0; i < 4; i++) {
            QtDefectFallingDetailVO detailVO = new QtDefectFallingDetailVO();
            BigDecimal bigDecimal = lineCountMap.get(i);
            detailVO.setIntervalHour(bigDecimal);
            detailList.add(detailVO);
        }
        countRow.setDetailList(detailCount);
        return countRow;
    }


    /**
     * 处理数据
     *
     * @param fallDTOList 落板原因
     * @return
     */
    private List<QtDestoryInfoFallDTO> dealReasonData(List<QtDestoryInfoFallDTO> fallDTOList) {
        List<QtDestoryInfoFallDTO> dealDataList = Lists.newArrayList();
        Set<String> hs = Sets.newHashSet("改尺寸", "切割不良");
        fallDTOList.forEach(ele -> {
            //落板原因属于所在列对应 的落板原因（落板原因“改尺寸”和“切割不良”对应“改尺寸、切割不良”一列，其他所有落板原因对应“厚度、外观不良”一列）
            if (hs.contains(ele.getDestoryReason())) {
                ele.setReasonCategory(1);
            } else {
                ele.setReasonCategory(0);
            }
            if (ele.getDestoryWorkStation() == 3) {
                QtDestoryInfoFallDTO model1 = new QtDestoryInfoFallDTO();
                BeanUtils.copyProperties(ele, model1);
                model1.setDestoryWorkStation(1);
                QtDestoryInfoFallDTO model2 = new QtDestoryInfoFallDTO();
                BeanUtils.copyProperties(ele, model2);
                model2.setDestoryWorkStation(2);
                dealDataList.add(model1);
                dealDataList.add(model2);
            } else {
                QtDestoryInfoFallDTO model = new QtDestoryInfoFallDTO();
                BeanUtils.copyProperties(ele, model);
                dealDataList.add(ele);
            }
        });
        return dealDataList;
    }

    /**
     * 统计合计（单产线） | 总计
     *
     * @param type         类型
     * @param lineName     产线名称
     * @param otherName    其他名称
     * @param valueMap     缺陷总计
     * @param defectIdList 缺陷排序
     * @return 行合计
     */
    private QtDefectReportDataVO count(Integer type, String lineName, String otherName, Map<Integer, Integer> valueMap, List<Integer> defectIdList) {
        QtDefectReportDataVO countRow = new QtDefectReportDataVO();
        countRow.setType(type);
        countRow.setProductLineName(lineName);
        countRow.setClassGroupName(otherName);
        countRow.setClassName(otherName);
        countRow.setClassDateStr(otherName);
        List<String> valueList = Lists.newArrayList();
        for (Integer defectId : defectIdList) {
            //为空，则给0
            int num = valueMap.getOrDefault(defectId, 0);
            valueList.add(String.valueOf(num));
        }
        countRow.setValueList(valueList);
        return countRow;
    }


    /**
     * 获取map的key
     *
     * @param line      产线
     * @param classDate 班次日期
     * @param classId   班次
     * @param defectId  缺陷id
     * @return
     */
    private String key(Integer line, LocalDate classDate, Integer classId, Integer defectId) {
        StringBuilder sb = new StringBuilder();
        sb.append(line);
        sb.append("_");
        sb.append(classDate);
        sb.append("_");
        sb.append(classId);
        sb.append("_");
        sb.append(defectId);
        return sb.toString();
    }

}
