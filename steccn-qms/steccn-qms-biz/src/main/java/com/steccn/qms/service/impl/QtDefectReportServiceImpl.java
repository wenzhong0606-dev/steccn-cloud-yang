package com.steccn.qms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import com.steccn.common.util.LocalDateUtil;
import com.steccn.oa.constant.OaManagerConstant;
import com.steccn.oa.dto.OaGroupCountDTO;
import com.steccn.oa.feign.OaInfoService;
import com.steccn.platform.feign.*;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysSpareUnitConversionVO;
import com.steccn.pms.dto.PmsDefectDayOrderBasicDTO;
import com.steccn.pms.feign.RemoteCompleteStorageFeignApi;
import com.steccn.pms.feign.RemotePmsFeignApi;
import com.steccn.pms.feign.RemoteReportWorkFeignApi;
import com.steccn.pms.vo.BaseReportHeaderVO;
import com.steccn.qms.util.XAxisUtil;
import com.steccn.qms.vo.DefectDayRowVO;
import com.steccn.qms.vo.DefectDayVO;
import com.steccn.pms.vo.FurtherValueStrVO;
import com.steccn.qms.constant.DefectDayConstant;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.QtCheckItem;
import com.steccn.qms.domain.QtDefectiveItem;
import com.steccn.qms.domain.QtItemCategory;
import com.steccn.qms.dto.QtChartParamDTO;
import com.steccn.qms.dto.QtCheckItemMaterialAvgDTO;
import com.steccn.qms.dto.QtDefectReportDTO;
import com.steccn.qms.dto.QtDestoryInfoFallDTO;
import com.steccn.qms.mapper.QtCheckTaskMapper;
import com.steccn.qms.service.*;
import com.steccn.qms.util.ShardTimeUtil;
import com.steccn.qms.util.UnitConvertUtil;
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
    private final QtItemCategoryService qtItemCategoryService;
    private final RemoteReportWorkFeignApi reportWorkFeignApi;
    private final RemotePmsFeignApi remotePmsFeignApi;
    private final RemoteSpareFeignApi remoteSpareFeignApi;
    private final RemoteSysConfigurationFeignApi remoteSysConfigurationFeignApi;
    private final RemoteCompleteStorageFeignApi remoteCompleteStorageFeignApi;
    private final RemoteSpareUnitConvertFeignApi remoteSpareUnitConvertFeignApi;
    private final RemoteDictFeignApi remoteDictFeignApi;

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
                row.setType(0);
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
        dataList.add(count(2, "总计", "总计", allValueMap, defectIdList));
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
        //去配置文件中查询
        String itemHd = itemCodeFromCf("QMS_DEFECT_JYX", "ITEM_HD");
        Assert.notNull(itemHd, "厚度检验项配置未配置");
        List<QtCheckItem> thickItemList = qtCheckItemService.list(new LambdaQueryWrapper<QtCheckItem>().eq(QtCheckItem::getCode, itemHd).select(QtCheckItem::getId));
        SteccnTableAssertUtil.notEmpty(thickItemList, "厚度平均值检验项不存在");
        QtCheckItemMultiAvgVO resVo = getQtCheckItemAvgVO(dto, thickItemList);
        return resVo;
    }

    @Override
    public QtCheckItemMultiAvgVO rbwAvg(QtDefectReportDTO dto) {
        String itemMbk = itemCodeFromCf("QMS_DEFECT_JYX", "ITEM_MBK");
        Assert.notNull(itemMbk, "毛边宽检验项未配置");
        List<QtCheckItem> thickItemList = qtCheckItemService.list(new LambdaQueryWrapper<QtCheckItem>().eq(QtCheckItem::getCode, itemMbk).select(QtCheckItem::getId));
        SteccnTableAssertUtil.notEmpty(thickItemList, "毛边宽检验项不存在");
        QtCheckItemMultiAvgVO resVo = getQtCheckItemAvgVO(dto, thickItemList);
        return resVo;
    }

    @Override
    public QtCheckItemSingleAvgVO roughness(QtDefectReportDTO dto) {
        String itemCcd = itemCodeFromCf("QMS_DEFECT_JYX", "ITEM_CCD");
        Assert.notNull(itemCcd, "粗糙度检验项未配置");
        List<QtCheckItem> roughnessItemList = qtCheckItemService.list(new LambdaQueryWrapper<QtCheckItem>().eq(QtCheckItem::getCode, itemCcd).select(QtCheckItem::getId));
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

    @Override
    public QtCheckItemTransmitVO transmittance(QtDefectReportDTO dto) {
        QtCheckItemTransmitVO resVo = new QtCheckItemTransmitVO();
        List<QtCheckItemTransmitDataVO> rowList = Lists.newArrayList();
        setTransmitHeader(resVo);
        resVo.setDataList(rowList);
        LocalDateTime start = LocalDateUtil.getMorning(dto.getStartDate());
        LocalDateTime end = LocalDateUtil.getMorning(dto.getEndDate().plusDays(1));
        List<String> checkItemCodeList = Arrays.asList("JYX000154", "JYX000155", "JYX000037", "JYX000036");
        List<QtCheckItemMaterialAvgDTO> dbList = qtCheckTaskMapper.transmittance(dto.getProductLineList(), start, end, checkItemCodeList);
        SteccnTableAssertUtil.notEmpty(dbList, "透光率数据集合为空");
        Map<Integer, String> lineIdNameMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(Sets.newHashSet(dto.getProductLineList())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<LocalDateTime> shardTimeList = ShardTimeUtil.cutDateRangeNew(QtManagerConstant.LossReportType.CLASS.getType(), start, end);
        //获取班组的集合
        TupleDTO<LocalDate, LocalDate> timeTupleDTO = new TupleDTO<>(dto.getStartDate(), dto.getEndDate());
        Map<String, OaGroupCountDTO> classGroupMap = FeignOptional.of(() -> oaInfoService.getGroupInfo(timeTupleDTO), FeignConstant.STECCN_OA_SERVICE_NAME).get();
        for (Integer line : dto.getProductLineList()) {
            String lineName = lineIdNameMap.get(line);
            //班次日期
            for (int i = 1; i < shardTimeList.size(); i++) {
                LocalDateTime begin = shardTimeList.get(i - 1);
                LocalDate classDate = LocalDateUtil.getClassDate(begin, false);
                QtCheckItemTransmitDataVO row = new QtCheckItemTransmitDataVO();
                row.setProductLineName(lineName);
                row.setClassDateStr(LocalDateUtil.localDateFormat(classDate, DatePattern.NORM_DATE_PATTERN));
                Integer classId = LocalDateUtil.getClassId(begin, false);
                QtManagerConstant.QtClassesEnum classEnum = QtManagerConstant.QtClassesEnum.code(classId);
                row.setClassName(Objects.nonNull(classEnum) ? classEnum.getName() : Strings.EMPTY);
                OaGroupCountDTO oaGroupCountDTO = classGroupMap.getOrDefault(classDate + ":" + row.getClassName(), new OaGroupCountDTO());
                row.setClassGroupName(oaGroupCountDTO.getGroupName());
                List<QtCheckItemMaterialAvgDTO> filterList = dbList.stream().filter(ele -> line.equals(ele.getProductLineId()) && classDate.isEqual(ele.getClassDate()) && ele.getClassGroupId().equals(oaGroupCountDTO.getGroupId())).collect(Collectors.toList());
                Map<String, BigDecimal> dataMap = Maps.newHashMap();
                if (CollUtil.isNotEmpty(filterList)) {
                    filterList.forEach(ele -> dataMap.put(ele.getCode(), ele.getValue()));
                }
                row.setFigureDepth(BigDecimalUtil.format(dataMap.get(checkItemCodeList.get(0))));
                BigDecimal thick = dataMap.get(checkItemCodeList.get(1));
                row.setThickness(BigDecimalUtil.format(thick, BigDecimalUtil.SCALE_2));
                BigDecimal oneYValue = dataMap.get(checkItemCodeList.get(2));
                BigDecimal oneTValue = dataMap.get(checkItemCodeList.get(3));
                String oneYValueStr = BigDecimalUtil.format(oneYValue, BigDecimalUtil.SCALE_2);
                String oneTValueStr = BigDecimalUtil.format(oneTValue, BigDecimalUtil.SCALE_2);
                List<FurtherValueStrVO> multiList = Lists.newArrayList();
                row.setModule(multiList);
                multiList.add(new FurtherValueStrVO(Arrays.asList(oneYValueStr, oneTValueStr)));
                //POWER(所在行“平均值-Y值” / (0.96 * 0.96 * 100), 3.2 / 所在行“厚度”) * 100 * 0.96 * 0.96，小数点后最多2位
                //POWER(所在行“平均值-T值” / (0.96 * 0.96 * 100), 3.2 / 所在行“厚度”) * 100 * 0.96 * 0.96，小数点后最多2位
                BigDecimal twoYValue = avgYtThick(oneYValue, new BigDecimal(3.0), thick);
                BigDecimal twoTValue = avgYtThick(oneTValue, new BigDecimal(3.0), thick);
                String twoYValueStr = BigDecimalUtil.format(twoYValue, BigDecimalUtil.SCALE_2);
                String twoTValueStr = BigDecimalUtil.format(twoTValue, BigDecimalUtil.SCALE_2);
                multiList.add(new FurtherValueStrVO(Arrays.asList(twoYValueStr, twoTValueStr)));
                //POWER(所在行“平均值-Y值” / (0.96 * 0.96 * 100), 3 / 所在行“厚度”) * 100 * 0.96 * 0.96，小数点后最多2位
                //POWER(所在行“平均值-T值” / (0.96 * 0.96 * 100), 3 / 所在行“厚度”) * 100 * 0.96 * 0.96，小数点后最多2位
                BigDecimal threeYValue = avgYtThick(oneYValue, new BigDecimal(3.2), thick);
                BigDecimal threeTValue = avgYtThick(oneTValue, new BigDecimal(3.2), thick);
                String threeYValueStr = BigDecimalUtil.format(threeYValue, BigDecimalUtil.SCALE_2);
                String threeTValueStr = BigDecimalUtil.format(threeTValue, BigDecimalUtil.SCALE_2);
                multiList.add(new FurtherValueStrVO(Arrays.asList(threeYValueStr, threeTValueStr)));
                rowList.add(row);
            }
        }
        return resVo;
    }

    @Override
    public QtMicroBubbleVO microBubble(QtDefectReportDTO dto) {
        LocalDateTime start = LocalDateUtil.getMorning(dto.getStartDate());
        LocalDateTime end = LocalDateUtil.getMorning(dto.getEndDate().plusDays(1));
        String itemQp = itemCodeFromCf("QMS_DEFECT_JYX", "ITEM_QP");
        Assert.notNull(itemQp, "粗糙度检验项未配置");
        List<QtCheckItem> qpQtCheckItemList = qtCheckItemService.list(new LambdaQueryWrapper<QtCheckItem>().eq(QtCheckItem::getCode, itemQp).select(QtCheckItem::getId));
        Assert.notEmpty(qpQtCheckItemList, "气泡检验项为空");
        List<Integer> qpItemIdList = qpQtCheckItemList.stream().map(QtCheckItem::getId).collect(Collectors.toList());
        List<QtCheckItemMaterialAvgDTO> timeList1 = qtCheckTaskMapper.microBubble(dto.getProductLineList(), start, end, qpItemIdList, LocalTime.of(8, 0), LocalTime.of(12, 0), 0);
        List<QtCheckItemMaterialAvgDTO> timeList2 = qtCheckTaskMapper.microBubble(dto.getProductLineList(), start, end, qpItemIdList, LocalTime.of(12, 0), LocalTime.of(16, 0), 0);
        List<QtCheckItemMaterialAvgDTO> timeList3 = qtCheckTaskMapper.microBubble(dto.getProductLineList(), start, end, qpItemIdList, LocalTime.of(16, 0), LocalTime.of(20, 0), 0);
        List<QtCheckItemMaterialAvgDTO> timeList4 = qtCheckTaskMapper.microBubble(dto.getProductLineList(), start, end, qpItemIdList, LocalTime.of(20, 0), null, 1);
        List<QtCheckItemMaterialAvgDTO> timeList5 = qtCheckTaskMapper.microBubble(dto.getProductLineList(), start, end, qpItemIdList, LocalTime.of(0, 0), LocalTime.of(4, 0), 1);
        List<QtCheckItemMaterialAvgDTO> timeList6 = qtCheckTaskMapper.microBubble(dto.getProductLineList(), start, end, qpItemIdList, LocalTime.of(4, 0), LocalTime.of(8, 0), 1);

        QtMicroBubbleVO resVo = new QtMicroBubbleVO();
        List<LocalDateTime> shardTimeList = ShardTimeUtil.cutDateRangeNew(QtManagerConstant.LossReportType.DAY.getType(), start, end);
        List<String> dateList = Lists.newArrayList();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM月dd日");
        for (int i = 0; i < shardTimeList.size() - 1; i++) {
            dateList.add(dateTimeFormatter.format(shardTimeList.get(i)));
        }
        resVo.setDateStrList(dateList);
        List<QtMicroBubbleListVO> dataList = Lists.newArrayList();
        resVo.setDataList(dataList);
        Map<Integer, String> lineIdNameMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(Sets.newHashSet(dto.getProductLineList())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

        for (Integer line : dto.getProductLineList()) {
            String lineName = lineIdNameMap.get(line);
            //遍历早班和晚班
            for (QtManagerConstant.QtClassesEnum classesEnum : QtManagerConstant.QtClassesEnum.values()) {
                for (String site : Arrays.asList("南", "北")) {
                    QtMicroBubbleListVO row = new QtMicroBubbleListVO();
                    dataList.add(row);
                    row.setLineSite(site);
                    row.setProductLineName(lineName);
                    row.setClassName(classesEnum.getName());
                    List<List<String>> dateDataList = Lists.newArrayList();
                    row.setDateDataList(dateDataList);
                    for (int i = 1; i < shardTimeList.size(); i++) {
                        List<String> dateValueList = Lists.newArrayList();
                        dateDataList.add(dateValueList);
                        LocalDateTime startLdt = shardTimeList.get(i - 1);
                        LocalDate classDate = LocalDateUtil.getClassDate(startLdt, false);
                        //根据classDate去查询
                        if (classesEnum.equals(QtManagerConstant.QtClassesEnum.DAY)) {
                            //time1 , time2 ,time3
                            Optional<QtCheckItemMaterialAvgDTO> first = timeList1.stream().filter(ele -> classDate.equals(ele.getClassDate()) && line.equals(ele.getProductLineId())).findFirst();
                            Optional<QtCheckItemMaterialAvgDTO> second = timeList2.stream().filter(ele -> classDate.equals(ele.getClassDate()) && line.equals(ele.getProductLineId())).findFirst();
                            Optional<QtCheckItemMaterialAvgDTO> third = timeList3.stream().filter(ele -> classDate.equals(ele.getClassDate()) && line.equals(ele.getProductLineId())).findFirst();
                            dateValueList.add(first.isPresent() ? String.valueOf(first.get().getValue()) : null);
                            dateValueList.add(second.isPresent() ? String.valueOf(second.get().getValue()) : null);
                            dateValueList.add(third.isPresent() ? String.valueOf(third.get().getValue()) : null);
                        } else {
                            //time4, time5 ,time6
                            Optional<QtCheckItemMaterialAvgDTO> forth = timeList4.stream().filter(ele -> classDate.equals(ele.getClassDate()) && line.equals(ele.getProductLineId())).findFirst();
                            Optional<QtCheckItemMaterialAvgDTO> fifth = timeList5.stream().filter(ele -> classDate.equals(ele.getClassDate()) && line.equals(ele.getProductLineId())).findFirst();
                            Optional<QtCheckItemMaterialAvgDTO> sixth = timeList6.stream().filter(ele -> classDate.equals(ele.getClassDate()) && line.equals(ele.getProductLineId())).findFirst();
                            dateValueList.add(forth.isPresent() ? String.valueOf(forth.get().getValue()) : null);
                            dateValueList.add(fifth.isPresent() ? String.valueOf(fifth.get().getValue()) : null);
                            dateValueList.add(sixth.isPresent() ? String.valueOf(sixth.get().getValue()) : null);
                        }
                    }
                }
            }
        }
        return resVo;
    }

    @Override
    public OriginalDefectVO originalDefect(QtDefectReportDTO dto) {
        LocalDateTime start = LocalDateUtil.getMorning(dto.getStartDate());
        LocalDateTime end = LocalDateUtil.getMorning(dto.getEndDate().plusDays(1));
        QtItemCategory category = qtItemCategoryService.getOne(new LambdaQueryWrapper<QtItemCategory>().eq(QtItemCategory::getName, "原片不良"));
        Assert.notNull(category, "原片不良类别不存在");
        OriginalDefectVO resVo = null;
        if (QtManagerConstant.LossReportType.DAY.getCode().equals(dto.getGranularity())) {
            resVo = getOriginalDefectDay(dto, start, end, category);
        } else {
            resVo = getOriginalDefectMonth(dto, start, end, category);
        }
        return resVo;
    }

    @Override
    public CoatingFilmVO coatingFilm(QtDefectReportDTO dto) {
        QtItemCategory category = qtItemCategoryService.getOne(new LambdaQueryWrapper<QtItemCategory>().eq(QtItemCategory::getName, "镀膜不良"));
        Assert.notNull(category, "镀膜不良类别不存在");
        List<QtDefectiveItem> itemList = qtDefectiveItemService.list(new QueryWrapper<QtDefectiveItem>()
                .select("code, any_value(name) as name")
                .lambda()
                .eq(QtDefectiveItem::getCategoryId, category.getId())
                .eq(QtDefectiveItem::getShowFlag, MpConstant.YES)
                .groupBy(QtDefectiveItem::getCode));
        Assert.notEmpty(itemList, "镀膜不良类别下暂无缺陷项");
        CoatingFilmVO resVo = new CoatingFilmVO();
        List<String> headerList = Lists.newArrayList();
        resVo.setHeaderList(headerList);
        itemList.forEach(ele -> {
            headerList.add(ele.getName());
            headerList.add(ele.getName() + "不良率");
        });
        LocalDateTime start = LocalDateUtil.getMorning(dto.getStartDate());
        LocalDateTime end = LocalDateUtil.getMorning(dto.getEndDate().plusDays(1));
        List<QtCheckItemMaterialAvgDTO> dbList = qtCheckTaskMapper.coatingFilmMonth(dto.getProductLineList(), start, end, category.getId());
        SteccnTableAssertUtil.notEmpty(dbList, "镀膜不良数据为空");

        List<LocalDateTime> shardTimeList = ShardTimeUtil.cutDateRangeNew(QtManagerConstant.LossReportType.MONTH.getType(), start, end);

        List<CoatingFilmRowVO> rowList = Lists.newArrayList();
        resVo.setRowList(rowList);

        Map<Integer, String> lineIdNameMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(Sets.newHashSet(dto.getProductLineList())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

        //替换取自生产管理->产量质量报表中每日的“原料耗用量”片数累加 key ->
        Map<String, BigDecimal> useCountMap = FeignOptional.of(() -> reportWorkFeignApi.getUseCount(new TripleDTO<>(dto.getProductLineList(), start, end)), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DatePattern.NORM_MONTH_PATTERN);
        Map<String, BigDecimal> sumMap = Maps.newHashMap();
        //先遍历月份
        for (int i = 1; i < shardTimeList.size(); i++) {
            LocalDateTime begin = shardTimeList.get(i - 1);
            String monthStr = formatter.format(begin);
            //合计（每月）
            Map<String, BigDecimal> countMap = Maps.newHashMap();
            for (Integer line : dto.getProductLineList()) {
                String lineName = lineIdNameMap.get(line);
                CoatingFilmRowVO row = new CoatingFilmRowVO();
                row.setType(0);
                List<BigDecimal> valueList = Lists.newArrayList();
                row.setValueList(valueList);
                row.setUseCount(BigDecimalUtil.setScale(useCountMap.get(monthStr + "_" + line), 2));
                rowList.add(row);
                row.setProductLineName(lineName);
                row.setMonthStr(monthStr);
                BigDecimal rowSum = null;
                for (QtDefectiveItem defectItem : itemList) {
                    Optional<QtCheckItemMaterialAvgDTO> dataOp = dbList.stream().filter(ele -> line.equals(ele.getProductLineId()) && defectItem.getCode().equals(ele.getCode()) && monthStr.equals(ele.getClassMonth())).findFirst();
                    BigDecimal value = dataOp.isPresent() ? dataOp.get().getValue() : null;
                    valueList.add(value);
                    BigDecimalUtil.accumulate(countMap, defectItem.getCode(), value);
                    BigDecimalUtil.accumulate(sumMap, defectItem.getCode(), value);
                    rowSum = BigDecimalUtil.add(value, rowSum);
                    valueList.add(BigDecimalUtil.divide(value, row.getUseCount(), 2));
                }
                row.setDefectSum(BigDecimalUtil.setScale(rowSum, 0));
                row.setDefectRate(BigDecimalUtil.divide(rowSum, row.getUseCount(), 2));
                BigDecimalUtil.accumulate(countMap, "-1", row.getUseCount());
                BigDecimalUtil.accumulate(countMap, "-2", row.getDefectSum());
                BigDecimalUtil.accumulate(sumMap, "-1", row.getUseCount());
                BigDecimalUtil.accumulate(sumMap, "-2", row.getDefectSum());
            }
            //月份合计
            rowList.add(countRow(itemList, countMap, 1, monthStr, "合计（单月）"));
        }
        //总计
        rowList.add(countRow(itemList, sumMap, 2, null, "合计"));
        return resVo;
    }

    @Override
    public DefectDayVO defectDay(QtDefectReportDTO dto) {
        LocalDateTime begin = LocalDateUtil.getMorning(dto.getStartDate());
        LocalDateTime over = LocalDateUtil.getMorning(dto.getEndDate().plusDays(1));
        List<PmsDefectDayOrderBasicDTO> pmsList = FeignOptional.of(() -> remotePmsFeignApi.defectDay(new TripleDTO<>(dto.getProductLineList(), begin, over)), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
        SteccnTableAssertUtil.notEmpty(pmsList, "暂无符合条件的订单");

        DefectDayVO resVo = new DefectDayVO();
        //获取符合条件的表头
        Map<String, List<String>> codeMap = getDefectDayHead(resVo);
        List<QtCheckItemMaterialAvgDTO> dataList = qtCheckTaskMapper.defectDay(dto.getProductLineList(), begin, over);

        //存放订单的基础信息
        Map<String, PmsDefectDayOrderBasicDTO> orderBasicMap = Maps.newHashMap();
        //根据开始日期和结束日期
        List<LocalDateTime> timeList = ShardTimeUtil.cutDateRangeNew(QtManagerConstant.LossReportType.CLASS.getType(), begin, over);
        List<DefectDayRowVO> rowList = Lists.newArrayList();
        //获取物料规格
        Set<Long> materialIdSet = Sets.newHashSet();
        Set<Integer> officeIdSet = Sets.newHashSet(dto.getProductLineList());
        pmsList.forEach(ele -> {
            officeIdSet.add(ele.getCustomerId());
            materialIdSet.add(ele.getMaterialId());
            orderBasicMap.put(ele.getProduceOrderCode(), ele);
        });

        SysDictVO dict1 = FeignOptional.of(() -> remoteDictFeignApi.getByCode("片", SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysDictVO dict2 = FeignOptional.of(() -> remoteDictFeignApi.getByCode("㎡", SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

        Map<Integer, String> lineIdNameMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(officeIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Map<Long, EqpSpareVO> spareMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(new ArrayList<>(materialIdSet)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

        //获取原料耗用量
        Map<String, BigDecimal> useCountMap = FeignOptional.of(() -> reportWorkFeignApi.useCountPerOrder(new TripleDTO<>(dto.getProductLineList(), begin, over)), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
        //获取产量
        Map<String, BigDecimal> inStorageMap = FeignOptional.of(() -> remoteCompleteStorageFeignApi.inStorage(new TripleDTO<>(dto.getProductLineList(), begin, over)), FeignConstant.STECCN_PMS_SERVICE_NAME).get();

        Map<Long, List<SysSpareUnitConversionVO>> convertMap = FeignOptional.of(() -> remoteSpareUnitConvertFeignApi.convert(materialIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

        TupleDTO<LocalDate, LocalDate> timeTupleDTO = new TupleDTO<>(dto.getStartDate(), dto.getEndDate());
        Map<String, OaGroupCountDTO> classGroupMap = FeignOptional.of(() -> oaInfoService.getGroupInfo(timeTupleDTO), FeignConstant.STECCN_OA_SERVICE_NAME).get();

        //总计
        Map<String, BigDecimal> sumMap = Maps.newHashMap();
        for (Integer line : dto.getProductLineList()) {
            Boolean flag = Boolean.FALSE;
            Map<String, BigDecimal> countMap = Maps.newHashMap();
            String lineName = lineIdNameMap.get(line);
            for (int i = 1; i < timeList.size(); i++) {
                LocalDateTime startTmp = timeList.get(i - 1);
                LocalDate classDate = LocalDateUtil.getClassDate(startTmp, Boolean.FALSE);
                Integer classId = LocalDateUtil.getClassId(startTmp, Boolean.FALSE);
                //获取满足条件的批次
                Set<String> orderNoSet = pmsList.stream().filter(ele -> LocalDateUtil.isRange(startTmp, ele.getStartTime(), ele.getEndTime(), Boolean.TRUE, Boolean.FALSE)).map(PmsDefectDayOrderBasicDTO::getProduceOrderCode).collect(Collectors.toSet());
                List<String> orderNoList = new ArrayList<>(orderNoSet);
                Collections.sort(orderNoList);
                String classDateStr = XAxisUtil.convert(QtManagerConstant.QtGranularityEnum.DAY, startTmp);
                for (String order : orderNoList) {
                    flag = Boolean.TRUE;
                    PmsDefectDayOrderBasicDTO orderBasicInfo = orderBasicMap.get(order);
                    DefectDayRowVO row = new DefectDayRowVO();
                    row.setProductLineName(lineName);
                    row.setClassDateStr(classDateStr);
                    row.setClassName(QtManagerConstant.QtClassesEnum.code(classId).getName());
                    row.setMaterialName(orderBasicInfo.getMaterialName());
                    row.setProduceOrderNo(orderBasicInfo.getProduceOrderCode());
                    row.setType(0);
                    row.setClassGroupName(classGroupMap.getOrDefault(classDateStr + ":" + row.getClassName(), new OaGroupCountDTO()).getGroupName());
                    EqpSpareVO eqpSpareVO = spareMap.getOrDefault(orderBasicInfo.getMaterialId(), new EqpSpareVO());
                    //物料信息
                    List<FurtherValueStrVO> module = new LinkedList<>();
                    module.add(new FurtherValueStrVO(Arrays.asList(BigDecimalUtil.format(eqpSpareVO.getLengthQuantity(), 2), BigDecimalUtil.format(eqpSpareVO.getWideQuantity(), 2), BigDecimalUtil.format(eqpSpareVO.getHighQuantity(), 2))));
                    //使用量
                    BigDecimal useCount = useCountMap.get(line + "_" + classDate + "_" + classId + "_" + orderBasicInfo.getProduceOrderCode());
                    BigDecimal useCountConvert = UnitConvertUtil.convert(convertMap, eqpSpareVO.getId(), Objects.nonNull(dict1) ? dict1.getId() : null, Objects.nonNull(dict2) ? dict2.getId() : null, useCount);
                    module.add(new FurtherValueStrVO(Arrays.asList(BigDecimalUtil.format(useCount, 2), BigDecimalUtil.format(useCountConvert, 2))));
                    BigDecimalUtil.accumulate(countMap, DefectDayConstant.SYL_0, useCount);
                    BigDecimalUtil.accumulate(countMap, DefectDayConstant.SYL_1, useCountConvert);
                    BigDecimalUtil.accumulate(sumMap, DefectDayConstant.SYL_0, useCount);
                    BigDecimalUtil.accumulate(sumMap, DefectDayConstant.SYL_1, useCountConvert);

                    //入库量
                    BigDecimal inStorage = inStorageMap.get(line + "_" + classDate + "_" + classId + "_" + orderBasicInfo.getProduceOrderId());
                    BigDecimal inStorageConvert = UnitConvertUtil.convert(convertMap, eqpSpareVO.getId(), Objects.nonNull(dict1) ? dict1.getId() : null, Objects.nonNull(dict2) ? dict2.getId() : null, useCount);
                    module.add(new FurtherValueStrVO(Arrays.asList(BigDecimalUtil.format(inStorage, 2), BigDecimalUtil.format(inStorageConvert, 2))));
                    BigDecimalUtil.accumulate(countMap, DefectDayConstant.RKS_0, inStorage);
                    BigDecimalUtil.accumulate(countMap, DefectDayConstant.RKS_1, inStorageConvert);
                    BigDecimalUtil.accumulate(sumMap, DefectDayConstant.RKS_0, inStorage);
                    BigDecimalUtil.accumulate(sumMap, DefectDayConstant.RKS_1, inStorageConvert);

                    //遍历map
                    BigDecimal defectSum = null;
                    //原片不良小计
                    BigDecimal ypblSum = null;
                    for (Map.Entry<String, List<String>> codeEntry : codeMap.entrySet()) {
                        List<String> valueList = Lists.newArrayList();
                        BigDecimal sum = null;
                        for (String code : codeEntry.getValue()) {
                            Optional<QtCheckItemMaterialAvgDTO> first = dataList.stream().filter(ele -> line.equals(ele.getProductLineId()) && classDate.isEqual(ele.getClassDate()) && classId.equals(ele.getClassId()) && order.equals(ele.getProductOrderNo()) && code.equals(ele.getCode())).findFirst();
                            if (first.isPresent()) {
                                BigDecimal currentValue = first.get().getValue();
                                valueList.add(String.valueOf(currentValue));
                                sum = BigDecimalUtil.add(sum, currentValue);
                                //产线总计
                                defectSum = BigDecimalUtil.add(defectSum, currentValue);
                                BigDecimalUtil.accumulate(countMap, codeEntry.getKey() + "-" + code, currentValue);
                                BigDecimalUtil.accumulate(sumMap, codeEntry.getKey() + "-" + code, currentValue);
                            } else {
                                valueList.add(null);
                            }
                        }
                        //产线总计
                        BigDecimalUtil.accumulate(countMap, codeEntry.getKey(), sum);
                        BigDecimalUtil.accumulate(sumMap, codeEntry.getKey(), sum);
                        if ("原片不良".equals(codeEntry.getKey())) {
                            ypblSum = sum;
                        }
                        valueList.add(BigDecimalUtil.format(sum, 0));
                        module.add(new FurtherValueStrVO(valueList));
                    }
                    //不良品
                    BigDecimal defectArea = BigDecimalUtil.multiply(defectSum, eqpSpareVO.getLengthQuantity());
                    defectArea = BigDecimalUtil.multiply(defectArea, eqpSpareVO.getWideQuantity());
                    defectArea = BigDecimalUtil.divide(defectArea, new BigDecimal("1000000"));
                    BigDecimalUtil.accumulate(countMap, DefectDayConstant.BLP_0, defectSum);
                    BigDecimalUtil.accumulate(countMap, DefectDayConstant.BLP_1, defectArea);
                    BigDecimalUtil.accumulate(sumMap, DefectDayConstant.BLP_0, defectSum);
                    BigDecimalUtil.accumulate(sumMap, DefectDayConstant.BLP_1, defectArea);

                    module.add(3, new FurtherValueStrVO(Arrays.asList(BigDecimalUtil.format(defectSum, 0), BigDecimalUtil.format(defectArea, 2))));
                    //不良率 ( 所在行不良品（片）- 所在行原片不良小计 ) / 所在行 使用数（片）* 100%，小数点后保留2位
                    BigDecimal defectRate = BigDecimalUtil.subtract(defectSum, ypblSum);
                    defectRate = BigDecimalUtil.divide(defectRate, useCount, 2);
                    //所在行开口泡 + 所在行气线泡 + 所在行夹杂/结石
                    Set<String> louSet = Sets.newHashSet("QXX000070", "QXX000071", "QXX000074");
                    List<BigDecimal> louList = dataList.stream().filter(ele -> line.equals(ele.getProductLineId()) && classDate.isEqual(ele.getClassDate()) && classId.equals(ele.getClassId()) && order.equals(ele.getProductOrderNo()) && louSet.contains(ele.getCode())).map(QtCheckItemMaterialAvgDTO::getValue).collect(Collectors.toList());
                    BigDecimal louSum = BigDecimalUtil.sum(louList);
                    BigDecimal louArea = BigDecimalUtil.multiply(louSum, eqpSpareVO.getLengthQuantity());
                    louArea = BigDecimalUtil.multiply(louArea, eqpSpareVO.getWideQuantity());
                    louArea = BigDecimalUtil.divide(louArea, new BigDecimal("1000000"));
                    BigDecimal louRate = BigDecimalUtil.percent(louArea, useCountConvert, 2);
                    module.add(4, new FurtherValueStrVO(Arrays.asList(BigDecimalUtil.format(louSum, 0), BigDecimalUtil.format(louArea, 2), BigDecimalUtil.format(louRate, 2))));
                    BigDecimalUtil.accumulate(countMap, DefectDayConstant.LOU_0, louSum);
                    BigDecimalUtil.accumulate(countMap, DefectDayConstant.LOU_1, louArea);
                    BigDecimalUtil.accumulate(sumMap, DefectDayConstant.LOU_0, louSum);
                    BigDecimalUtil.accumulate(sumMap, DefectDayConstant.LOU_1, louArea);
                    row.setDefectRate(defectRate);
                    row.setModule(module);
                    rowList.add(row);
                }
            }
            if (flag) {
                rowList.add(countSum(1, lineName, "合计（单产线）", countMap, codeMap));
            }
        }
        rowList.add(countSum(2, null, "总计", sumMap, codeMap));
        resVo.setRowList(rowList);
        return resVo;
    }

    @Override
    public List<DefectCountRowVO> defectWeek(QtDefectReportDTO dto) {
        List<DefectCountRowVO> rowList = Lists.newArrayList();
        //查询缺陷类别
        List<String> categoryNameList = Arrays.asList("原片不良", "磨边不良", "钻孔不良", "丝印不良", "镀膜不良", "钢化不良");
        List<QtItemCategory> categoryList = qtItemCategoryService.list(new LambdaQueryWrapper<QtItemCategory>().in(QtItemCategory::getName, categoryNameList).eq(QtItemCategory::getShowFlag, MpConstant.YES));
        Assert.notEmpty(categoryList, "缺陷类别不可为空");
        Set<Integer> categoryIdSet = categoryList.stream().map(QtItemCategory::getId).collect(Collectors.toSet());
        Map<Integer, Integer> categoryMap = Maps.newHashMap();
        for (QtItemCategory category : categoryList) {
            int idx = categoryNameList.indexOf(category.getName());
            categoryMap.put(idx, category.getId());
        }
        //时间
        LocalDateTime start = LocalDateUtil.getMorning(dto.getStartDate());
        LocalDateTime end = LocalDateUtil.getMorning(dto.getEndDate().plusDays(1));
        List<QtCheckItemMaterialAvgDTO> dataList = qtCheckTaskMapper.defectWeek(categoryIdSet, dto.getProductLineList(), start, end);
        SteccnTableAssertUtil.notEmpty(dataList, "缺陷类别暂无数据");
        //查询产线
        Map<Integer, String> lineIdNameMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(Sets.newHashSet(dto.getProductLineList())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

        //查询每周的原片耗用量和入库量
        Map<String, BigDecimal> inStorageMap = FeignOptional.of(() -> remoteCompleteStorageFeignApi.inStorageWeek(new TripleDTO<>(dto.getProductLineList(), start, end)), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
        Map<String, BigDecimal> useCountMap = FeignOptional.of(() -> reportWorkFeignApi.useCountWeek(new TripleDTO<>(dto.getProductLineList(), start, end)), FeignConstant.STECCN_PMS_SERVICE_NAME).get();

        //根据开始时间和结束时间 区分不同周
        Map<Integer, BigDecimal> sumMap = Maps.newHashMap();
        List<LocalDateTime> timeList = ShardTimeUtil.cutDateRangeNew(QtManagerConstant.QtGranularityEnum.WEEK.getType(), start, end);
        for (int i = 1; i < timeList.size(); i++) {
            Map<Integer, BigDecimal> countMap = Maps.newHashMap();
            LocalDateTime begin = timeList.get(i - 1);
            String weekStr = ShardTimeUtil.date2Week(begin.toLocalDate());
            String xAxis = XAxisUtil.convert(QtManagerConstant.QtGranularityEnum.WEEK, begin);
            for (Integer line : dto.getProductLineList()) {
                DefectCountRowVO row = new DefectCountRowVO();
                row.setType(0);
                row.setTimeStr(xAxis);
                row.setProductLineName(lineIdNameMap.get(line));
                //获取使用量和成品数
                row.setUseCountNum(useCountMap.get(line + "_" + weekStr));
                row.setInStorageNum(inStorageMap.get(line + "_" + weekStr));
                BigDecimalUtil.accumulate(countMap, 6, row.getUseCountNum());
                BigDecimalUtil.accumulate(sumMap, 6, row.getUseCountNum());
                BigDecimalUtil.accumulate(countMap, 7, row.getInStorageNum());
                BigDecimalUtil.accumulate(sumMap, 7, row.getInStorageNum());
                List<QtCheckItemMaterialAvgDTO> filterDataList = dataList.stream().filter(ele -> weekStr.equals(ele.getClassMonth()) && line.equals(ele.getProductLineId())).collect(Collectors.toList());
                row.setOriginDefectNum(getDefectNum(filterDataList, categoryMap.get(0)));
                row.setOriginDefectRate(BigDecimalUtil.percent(row.getOriginDefectNum(), row.getUseCountNum(), 2));
                BigDecimalUtil.accumulate(countMap, 0, row.getOriginDefectNum());
                BigDecimalUtil.accumulate(sumMap, 0, row.getOriginDefectNum());
                row.setEdgeDefectNum(getDefectNum(filterDataList, categoryMap.get(1)));
                row.setEdgeDefectRate(BigDecimalUtil.percent(row.getEdgeDefectNum(), row.getUseCountNum(), 2));
                BigDecimalUtil.accumulate(countMap, 1, row.getEdgeDefectNum());
                BigDecimalUtil.accumulate(sumMap, 1, row.getEdgeDefectNum());
                row.setDrillDefectNum(getDefectNum(filterDataList, categoryMap.get(2)));
                row.setDrillDefectRate(BigDecimalUtil.percent(row.getDrillDefectNum(), row.getUseCountNum(), 2));
                BigDecimalUtil.accumulate(countMap, 2, row.getDrillDefectNum());
                BigDecimalUtil.accumulate(sumMap, 2, row.getDrillDefectNum());
                row.setSilkDefectNum(getDefectNum(filterDataList, categoryMap.get(3)));
                row.setSilkDefectRate(BigDecimalUtil.percent(row.getSilkDefectNum(), row.getUseCountNum(), 2));
                BigDecimalUtil.accumulate(countMap, 3, row.getSilkDefectNum());
                BigDecimalUtil.accumulate(sumMap, 3, row.getSilkDefectNum());
                row.setCoatDefectNum(getDefectNum(filterDataList, categoryMap.get(4)));
                row.setCoatDefectRate(BigDecimalUtil.percent(row.getCoatDefectNum(), row.getUseCountNum(), 2));
                BigDecimalUtil.accumulate(countMap, 4, row.getCoatDefectNum());
                BigDecimalUtil.accumulate(sumMap, 4, row.getCoatDefectNum());
                row.setTemperDefectNum(getDefectNum(filterDataList, categoryMap.get(5)));
                row.setTemperDefectRate(BigDecimalUtil.percent(row.getTemperDefectNum(), row.getUseCountNum(), 2));
                BigDecimalUtil.accumulate(countMap, 5, row.getTemperDefectNum());
                BigDecimalUtil.accumulate(sumMap, 5, row.getTemperDefectNum());
                BigDecimal sum1 = BigDecimalUtil.sum(Arrays.asList(row.getOriginDefectNum(), row.getEdgeDefectNum(), row.getDrillDefectNum(), row.getSilkDefectNum(), row.getCoatDefectNum(), row.getTemperDefectNum()));
                BigDecimal sum2 = BigDecimalUtil.sum(Arrays.asList(row.getEdgeDefectNum(), row.getDrillDefectNum(), row.getSilkDefectNum(), row.getCoatDefectNum(), row.getTemperDefectNum()));
                row.setTotalDefectNum(sum1);
                row.setTotalDeepDefectNum(sum2);
                BigDecimalUtil.accumulate(countMap, 8, row.getTotalDefectNum());
                BigDecimalUtil.accumulate(sumMap, 8, row.getTotalDefectNum());
                BigDecimalUtil.accumulate(countMap, 9, row.getTotalDeepDefectNum());
                BigDecimalUtil.accumulate(sumMap, 9, row.getTotalDeepDefectNum());
                row.setTotalDefectRate(BigDecimalUtil.percent(row.getTotalDefectNum(), row.getUseCountNum(), 2));
                row.setTotalDeepDefectRate(BigDecimalUtil.percent(row.getTotalDeepDefectNum(), row.getUseCountNum(), 2));
                rowList.add(row);
            }
            rowList.add(countWm(xAxis, 1, "合计（单周）", countMap));
        }
        rowList.add(countWm("总计", 2, "总计", sumMap));
        return rowList;
    }

    @Override
    public List<DefectCountRowVO> defectMonth(QtDefectReportDTO dto) {
        List<DefectCountRowVO> rowList = Lists.newArrayList();
        //查询缺陷类别
        List<String> categoryNameList = Arrays.asList("原片不良", "磨边不良", "钻孔不良", "丝印不良", "镀膜不良", "钢化不良");
        List<QtItemCategory> categoryList = qtItemCategoryService.list(new LambdaQueryWrapper<QtItemCategory>().in(QtItemCategory::getName, categoryNameList).eq(QtItemCategory::getShowFlag, MpConstant.YES));
        Assert.notEmpty(categoryList, "缺陷类别不可为空");
        Set<Integer> categoryIdSet = categoryList.stream().map(QtItemCategory::getId).collect(Collectors.toSet());
        Map<Integer, Integer> categoryMap = Maps.newHashMap();
        for (QtItemCategory category : categoryList) {
            int idx = categoryNameList.indexOf(category.getName());
            categoryMap.put(idx, category.getId());
        }
        //时间
        LocalDateTime start = LocalDateUtil.getMorning(dto.getStartDate());
        LocalDateTime end = LocalDateUtil.getMorning(dto.getEndDate().plusDays(1));
        List<QtCheckItemMaterialAvgDTO> dataList = qtCheckTaskMapper.defectMonth(categoryIdSet, dto.getProductLineList(), start, end);
        SteccnTableAssertUtil.notEmpty(dataList, "缺陷类别暂无数据");
        //查询产线
        Map<Integer, String> lineIdNameMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(Sets.newHashSet(dto.getProductLineList())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

        //查询每周的原片耗用量和入库量
        Map<String, BigDecimal> inStorageMap = FeignOptional.of(() -> remoteCompleteStorageFeignApi.inStorageMonth(new TripleDTO<>(dto.getProductLineList(), start, end)), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
        Map<String, BigDecimal> useCountMap = FeignOptional.of(() -> reportWorkFeignApi.useCountMonth(new TripleDTO<>(dto.getProductLineList(), start, end)), FeignConstant.STECCN_PMS_SERVICE_NAME).get();

        //根据开始时间和结束时间 区分不同周
        Map<Integer, BigDecimal> sumMap = Maps.newHashMap();
        List<LocalDateTime> timeList = ShardTimeUtil.cutDateRangeNew(QtManagerConstant.QtGranularityEnum.MONTH.getType(), start, end);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
        for (int i = 1; i < timeList.size(); i++) {
            Map<Integer, BigDecimal> countMap = Maps.newHashMap();
            LocalDateTime begin = timeList.get(i - 1);
            String timeStr = formatter.format(begin);
            String xAxis = XAxisUtil.convert(QtManagerConstant.QtGranularityEnum.MONTH, begin);
            for (Integer line : dto.getProductLineList()) {
                DefectCountRowVO row = new DefectCountRowVO();
                row.setType(0);
                row.setTimeStr(xAxis);
                row.setProductLineName(lineIdNameMap.get(line));
                //获取使用量和成品数
                row.setUseCountNum(useCountMap.get(line + "_" + timeStr));
                row.setInStorageNum(inStorageMap.get(line + "_" + timeStr));
                BigDecimalUtil.accumulate(countMap, 6, row.getUseCountNum());
                BigDecimalUtil.accumulate(sumMap, 6, row.getUseCountNum());
                BigDecimalUtil.accumulate(countMap, 7, row.getInStorageNum());
                BigDecimalUtil.accumulate(sumMap, 7, row.getInStorageNum());
                List<QtCheckItemMaterialAvgDTO> filterDataList = dataList.stream().filter(ele -> timeStr.equals(ele.getClassMonth()) && line.equals(ele.getProductLineId())).collect(Collectors.toList());
                row.setOriginDefectNum(getDefectNum(filterDataList, categoryMap.get(0)));
                row.setOriginDefectRate(BigDecimalUtil.percent(row.getOriginDefectNum(), row.getUseCountNum(), 2));
                BigDecimalUtil.accumulate(countMap, 0, row.getOriginDefectNum());
                BigDecimalUtil.accumulate(sumMap, 0, row.getOriginDefectNum());
                row.setEdgeDefectNum(getDefectNum(filterDataList, categoryMap.get(1)));
                row.setEdgeDefectRate(BigDecimalUtil.percent(row.getEdgeDefectNum(), row.getUseCountNum(), 2));
                BigDecimalUtil.accumulate(countMap, 1, row.getEdgeDefectNum());
                BigDecimalUtil.accumulate(sumMap, 1, row.getEdgeDefectNum());
                row.setDrillDefectNum(getDefectNum(filterDataList, categoryMap.get(2)));
                row.setDrillDefectRate(BigDecimalUtil.percent(row.getDrillDefectNum(), row.getUseCountNum(), 2));
                BigDecimalUtil.accumulate(countMap, 2, row.getDrillDefectNum());
                BigDecimalUtil.accumulate(sumMap, 2, row.getDrillDefectNum());
                row.setSilkDefectNum(getDefectNum(filterDataList, categoryMap.get(3)));
                row.setSilkDefectRate(BigDecimalUtil.percent(row.getSilkDefectNum(), row.getUseCountNum(), 2));
                BigDecimalUtil.accumulate(countMap, 3, row.getSilkDefectNum());
                BigDecimalUtil.accumulate(sumMap, 3, row.getSilkDefectNum());
                row.setCoatDefectNum(getDefectNum(filterDataList, categoryMap.get(4)));
                row.setCoatDefectRate(BigDecimalUtil.percent(row.getCoatDefectNum(), row.getUseCountNum(), 2));
                BigDecimalUtil.accumulate(countMap, 4, row.getCoatDefectNum());
                BigDecimalUtil.accumulate(sumMap, 4, row.getCoatDefectNum());
                row.setTemperDefectNum(getDefectNum(filterDataList, categoryMap.get(5)));
                row.setTemperDefectRate(BigDecimalUtil.percent(row.getTemperDefectNum(), row.getUseCountNum(), 2));
                BigDecimalUtil.accumulate(countMap, 5, row.getTemperDefectNum());
                BigDecimalUtil.accumulate(sumMap, 5, row.getTemperDefectNum());
                BigDecimal sum1 = BigDecimalUtil.sum(Arrays.asList(row.getOriginDefectNum(), row.getEdgeDefectNum(), row.getDrillDefectNum(), row.getSilkDefectNum(), row.getCoatDefectNum(), row.getTemperDefectNum()));
                BigDecimal sum2 = BigDecimalUtil.sum(Arrays.asList(row.getEdgeDefectNum(), row.getDrillDefectNum(), row.getSilkDefectNum(), row.getCoatDefectNum(), row.getTemperDefectNum()));
                row.setTotalDefectNum(sum1);
                row.setTotalDeepDefectNum(sum2);
                BigDecimalUtil.accumulate(countMap, 8, row.getTotalDefectNum());
                BigDecimalUtil.accumulate(sumMap, 8, row.getTotalDefectNum());
                BigDecimalUtil.accumulate(countMap, 9, row.getTotalDeepDefectNum());
                BigDecimalUtil.accumulate(sumMap, 9, row.getTotalDeepDefectNum());
                row.setTotalDefectRate(BigDecimalUtil.percent(row.getTotalDefectNum(), row.getUseCountNum(), 2));
                row.setTotalDeepDefectRate(BigDecimalUtil.percent(row.getTotalDeepDefectNum(), row.getUseCountNum(), 2));
                rowList.add(row);
            }
            rowList.add(countWm(xAxis, 1, "合计（单月）", countMap));
        }
        rowList.add(countWm("总计", 2, "总计", sumMap));
        return rowList;
    }

    /**
     * 不良周|月报表 合计 | 总计
     *
     * @param timeStr  周
     * @param type     类型
     * @param lineName 产线名称
     * @param countMap 统计
     * @return
     */
    private DefectCountRowVO countWm(String timeStr, int type, String lineName, Map<Integer, BigDecimal> countMap) {
        DefectCountRowVO row = new DefectCountRowVO();
        row.setTimeStr(timeStr);
        row.setType(type);
        row.setProductLineName(lineName);
        row.setOriginDefectNum(countMap.get(0));
        row.setEdgeDefectNum(countMap.get(1));
        row.setDrillDefectNum(countMap.get(2));
        row.setSilkDefectNum(countMap.get(3));
        row.setCoatDefectNum(countMap.get(4));
        row.setTemperDefectNum(countMap.get(5));
        row.setUseCountNum(countMap.get(6));
        row.setInStorageNum(countMap.get(7));
        row.setTotalDefectNum(countMap.get(8));
        row.setTotalDeepDefectNum(countMap.get(9));
        row.setOriginDefectRate(BigDecimalUtil.percent(row.getOriginDefectNum(), row.getUseCountNum(), 2));
        row.setEdgeDefectRate(BigDecimalUtil.percent(row.getEdgeDefectNum(), row.getUseCountNum(), 2));
        row.setDrillDefectRate(BigDecimalUtil.percent(row.getDrillDefectNum(), row.getUseCountNum(), 2));
        row.setSilkDefectRate(BigDecimalUtil.percent(row.getSilkDefectNum(), row.getUseCountNum(), 2));
        row.setCoatDefectRate(BigDecimalUtil.percent(row.getCoatDefectNum(), row.getUseCountNum(), 2));
        row.setTemperDefectRate(BigDecimalUtil.percent(row.getTemperDefectRate(), row.getUseCountNum(), 2));
        row.setTotalDefectRate(BigDecimalUtil.percent(row.getTotalDefectNum(), row.getUseCountNum(), 2));
        row.setTotalDeepDefectRate(BigDecimalUtil.percent(row.getTotalDeepDefectNum(), row.getUseCountNum(), 2));
        return row;
    }

    /**
     * 获取缺陷总数
     *
     * @param dataList   数据集合
     * @param categoryId 类别id
     * @return
     */
    private BigDecimal getDefectNum(List<QtCheckItemMaterialAvgDTO> dataList, Integer categoryId) {
        Optional<QtCheckItemMaterialAvgDTO> first = dataList.stream().filter(ele -> ele.getCategoryId().equals(categoryId)).findFirst();
        if (first.isPresent()) {
            return first.get().getValue();
        }
        return null;
    }

    /**
     * 行合计 总计
     *
     * @param type     类型
     * @param lineName 产线名称
     * @param countMap 汇总map
     * @param codeMap  动态表头
     * @return 行
     */
    private DefectDayRowVO countSum(int type, String lineName, String classDateStr, Map<String, BigDecimal> countMap, Map<String, List<String>> codeMap) {
        DefectDayRowVO countRow = new DefectDayRowVO();
        countRow.setType(type);
        countRow.setProductLineName(lineName);
        countRow.setClassDateStr(classDateStr);
        List<FurtherValueStrVO> module = Lists.newArrayList();
        countRow.setModule(module);
        //规格
        module.add(new FurtherValueStrVO(Arrays.asList("", "", "")));
        BigDecimal syl0 = countMap.get(DefectDayConstant.SYL_0);
        BigDecimal syl1 = countMap.get(DefectDayConstant.SYL_1);
        module.add(new FurtherValueStrVO(Arrays.asList(BigDecimalUtil.format(syl0, 0), BigDecimalUtil.format(syl1, 0))));
        BigDecimal rks0 = countMap.get(DefectDayConstant.RKS_0);
        BigDecimal rks1 = countMap.get(DefectDayConstant.RKS_1);
        module.add(new FurtherValueStrVO(Arrays.asList(BigDecimalUtil.format(rks0, 0), BigDecimalUtil.format(rks1, 0))));
        BigDecimal blp0 = countMap.get(DefectDayConstant.BLP_0);
        BigDecimal blp1 = countMap.get(DefectDayConstant.BLP_1);
        module.add(new FurtherValueStrVO(Arrays.asList(BigDecimalUtil.format(blp0, 0), BigDecimalUtil.format(blp1, 0))));

        BigDecimal lou0 = countMap.get(DefectDayConstant.LOU_0);
        BigDecimal lou1 = countMap.get(DefectDayConstant.LOU_1);
        //所在行漏检（㎡）/ 所在行使用量（㎡） * 100%，小数点后保留2位
        BigDecimal lou2 = BigDecimalUtil.percent(lou1, syl1, 2);
        module.add(new FurtherValueStrVO(Arrays.asList(BigDecimalUtil.format(lou0, 0), BigDecimalUtil.format(lou0, 0), BigDecimalUtil.format(lou2, 2))));
        BigDecimal defectSum = null;
        for (Map.Entry<String, List<String>> codeEntry : codeMap.entrySet()) {
            List<String> valueList = Lists.newArrayList();
            for (String value : codeEntry.getValue()) {
                valueList.add(BigDecimalUtil.format(countMap.get(codeEntry.getKey() + "-" + value), 0));
            }
            BigDecimal total = countMap.get(codeEntry.getKey());
            if ("原片不良".equals(codeEntry.getKey())) {
                defectSum = total;
            }
            valueList.add(BigDecimalUtil.format(countMap.get(codeEntry.getKey()), 0));
            module.add(new FurtherValueStrVO(valueList));
        }
        //( 所在行不良品（片）- 所在行原片不良小计 ) / 所在行 使用数（片）* 100%，小数点后保留2位
        BigDecimal defectRate = BigDecimalUtil.subtract(blp0, defectSum);
        defectRate = BigDecimalUtil.percent(defectRate, syl0, 2);
        countRow.setDefectRate(defectRate);
        return countRow;
    }

    /**
     * 获取不良日报表表头
     *
     * @param resVo 请求参数
     * @return
     */
    private Map<String, List<String>> getDefectDayHead(DefectDayVO resVo) {
        //考虑返回
        Map<String, List<String>> codeMap = Maps.newLinkedHashMap();
        List<BaseReportHeaderVO> headList = Lists.newArrayList();
        headList.add(new BaseReportHeaderVO("规格", Arrays.asList("长", "宽", "厚")));
        headList.add(new BaseReportHeaderVO("使用量", Arrays.asList("片", "㎡")));
        headList.add(new BaseReportHeaderVO("入库数", Arrays.asList("片", "㎡")));
        headList.add(new BaseReportHeaderVO("不良数", Arrays.asList("片", "㎡")));
        headList.add(new BaseReportHeaderVO("漏检", Arrays.asList("片", "㎡", "漏检率%")));
        List<String> categoryNameList = Arrays.asList("原片不良", "磨边不良", "钻孔不良", "丝印不良", "镀膜不良", "钢化不良");
        List<QtItemCategory> categoryList = qtItemCategoryService.list(new LambdaQueryWrapper<QtItemCategory>().in(QtItemCategory::getName, categoryNameList).eq(QtItemCategory::getShowFlag, MpConstant.YES));
        Assert.notEmpty(categoryList, "缺陷类别不可为空");
        Set<Integer> categoryIdSet = categoryList.stream().map(QtItemCategory::getId).collect(Collectors.toSet());
        //根据类别查询缺陷项
        List<QtDefectiveItem> defectItemList = qtDefectiveItemService.list(new LambdaQueryWrapper<QtDefectiveItem>()
                .in(QtDefectiveItem::getCategoryId, categoryIdSet)
                .eq(QtDefectiveItem::getShowFlag, MpConstant.YES));
        Assert.notEmpty(defectItemList, "缺陷项不可为空");
        Map<String, QtItemCategory> categoryMap = Maps.newHashMap();
        categoryList.forEach(ele -> categoryMap.put(ele.getName(), ele));
        for (String cn : categoryNameList) {
            QtItemCategory category = categoryMap.get(cn);
            if (Objects.nonNull(category)) {
                List<QtDefectiveItem> defectList = defectItemList.stream().filter(ele -> category.getId().equals(ele.getCategoryId())).collect(Collectors.toList());
                if (CollUtil.isNotEmpty(defectList)) {
                    List<String> nameList = defectList.stream().map(QtDefectiveItem::getName).collect(Collectors.toList());
                    List<String> codeList = defectList.stream().map(QtDefectiveItem::getCode).collect(Collectors.toList());
                    codeList.add("小计");
                    codeMap.put(cn, codeList);
                    headList.add(new BaseReportHeaderVO(cn, nameList));
                }
            }
        }
        resVo.setHeaderList(headList);
        return codeMap;
    }

    /**
     * 合计行
     *
     * @param itemList 缺陷项
     * @param map      统计数据
     * @param type     类型
     * @param hintName 提示语
     * @return
     */
    private CoatingFilmRowVO countRow(List<QtDefectiveItem> itemList, Map<String, BigDecimal> map, int type, String monthStr, String hintName) {
        CoatingFilmRowVO row = new CoatingFilmRowVO();
        row.setType(type);
        row.setMonthStr(monthStr);
        row.setProductLineName(hintName);
        row.setDefectSum(BigDecimalUtil.sum(map.values()));
        List<BigDecimal> valueList = Lists.newArrayList();
        for (QtDefectiveItem defectItem : itemList) {
            BigDecimal defectValue = map.get(defectItem.getCode());
            valueList.add(BigDecimalUtil.setScale(defectValue, 0));
            valueList.add(BigDecimalUtil.divide(defectValue, row.getUseCount(), 2));
        }
        row.setValueList(valueList);
        row.setUseCount(BigDecimalUtil.setScale(map.get("-1"), 0));
        row.setDefectSum(BigDecimalUtil.setScale(map.get("-2"), 0));
        row.setDefectRate(BigDecimalUtil.divide(row.getDefectSum(), row.getUseCount(), 2));
        return row;
    }

    /**
     * 原片不良 - 天
     *
     * @param dto      请求
     * @param start    开始时间
     * @param end      结束时间
     * @param category 类别
     * @return vo
     */
    private OriginalDefectVO getOriginalDefectDay(QtDefectReportDTO dto, LocalDateTime start, LocalDateTime end, QtItemCategory category) {
        List<QtCheckItemMaterialAvgDTO> dbList = qtCheckTaskMapper.originalDefectDay(dto.getProductLineList(), start, end, category.getId());
        SteccnTableAssertUtil.notEmpty(dbList, "原片不良数据为空");
        OriginalDefectVO resVo = new OriginalDefectVO();
        List<LocalDateTime> shardTimeList = ShardTimeUtil.cutDateRangeNew(QtManagerConstant.LossReportType.DAY.getType(), start, end);
        List<String> dateList = Lists.newArrayList();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM月dd日");
        for (int i = 0; i < shardTimeList.size() - 1; i++) {
            dateList.add(dateTimeFormatter.format(shardTimeList.get(i)));
        }
        resVo.setDateStrList(dateList);
        Map<Integer, String> lineIdNameMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(Sets.newHashSet(dto.getProductLineList())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

        List<OriginalDefectRowVO> rowList = Lists.newArrayList();
        resVo.setRowList(rowList);
        //每列的合计map
        Map<LocalDate, BigDecimal> columnSumMap = Maps.newHashMap();
        for (Integer line : dto.getProductLineList()) {
            String lineName = lineIdNameMap.get(line);
            OriginalDefectRowVO row = new OriginalDefectRowVO();
            row.setType(0);
            List<String> dateDataList = Lists.newArrayList();
            BigDecimal sum = null;
            for (int i = 1; i < shardTimeList.size(); i++) {
                LocalDateTime startLdt = shardTimeList.get(i - 1);
                LocalDate classDate = LocalDateUtil.getClassDate(startLdt, false);
                List<BigDecimal> filterDataList = dbList.stream().filter(ele -> line.equals(ele.getProductLineId()) && classDate.isEqual(ele.getClassDate())).map(QtCheckItemMaterialAvgDTO::getValue).collect(Collectors.toList());
                BigDecimal value = BigDecimalUtil.sum(filterDataList);
                sum = BigDecimalUtil.add(sum, value);
                columnSumMap.put(classDate, BigDecimalUtil.add(columnSumMap.get(classDate), value));
                dateDataList.add(String.valueOf(value));
            }
            row.setProductLineName(lineName);
            row.setSum(BigDecimalUtil.format(sum, 0));
            row.setDateDataList(dateDataList);
            rowList.add(row);
        }
        OriginalDefectRowVO sumRow = new OriginalDefectRowVO();
        sumRow.setType(1);
        sumRow.setProductLineName("合计");
        List<String> dateDataList = Lists.newArrayList();
        sumRow.setDateDataList(dateDataList);
        for (int i = 1; i < shardTimeList.size(); i++) {
            LocalDateTime startLdt = shardTimeList.get(i - 1);
            LocalDate classDate = LocalDateUtil.getClassDate(startLdt, false);
            dateDataList.add(BigDecimalUtil.format(columnSumMap.get(classDate), 0));
        }
        BigDecimal allSum = BigDecimalUtil.sum(columnSumMap.values().stream().collect(Collectors.toList()));
        sumRow.setSum(BigDecimalUtil.format(allSum, 0));
        rowList.add(sumRow);
        return resVo;
    }

    /**
     * 原片不良 - 天
     *
     * @param dto      请求
     * @param start    开始时间
     * @param end      结束时间
     * @param category 类别
     * @return vo
     */
    private OriginalDefectVO getOriginalDefectMonth(QtDefectReportDTO dto, LocalDateTime start, LocalDateTime end, QtItemCategory category) {
        List<QtCheckItemMaterialAvgDTO> dbList = qtCheckTaskMapper.originalDefectMonth(dto.getProductLineList(), start, end, category.getId());
        SteccnTableAssertUtil.notEmpty(dbList, "原片不良数据为空");
        OriginalDefectVO resVo = new OriginalDefectVO();
        List<LocalDateTime> shardTimeList = ShardTimeUtil.cutDateRangeNew(QtManagerConstant.LossReportType.MONTH.getType(), start, end);
        List<String> dateList = Lists.newArrayList();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月");
        for (int i = 0; i < shardTimeList.size() - 1; i++) {
            dateList.add(dateTimeFormatter.format(shardTimeList.get(i)));
        }
        resVo.setDateStrList(dateList);
        Map<Integer, String> lineIdNameMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(Sets.newHashSet(dto.getProductLineList())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

        List<OriginalDefectRowVO> rowList = Lists.newArrayList();
        resVo.setRowList(rowList);
        //每列的合计map
        Map<String, BigDecimal> columnSumMap = Maps.newHashMap();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DatePattern.NORM_MONTH_PATTERN);

        for (Integer line : dto.getProductLineList()) {
            String lineName = lineIdNameMap.get(line);
            OriginalDefectRowVO row = new OriginalDefectRowVO();
            row.setType(0);
            List<String> dateDataList = Lists.newArrayList();
            BigDecimal sum = null;
            for (int i = 1; i < shardTimeList.size(); i++) {
                LocalDateTime startLdt = shardTimeList.get(i - 1);
                String classMonth = formatter.format(startLdt);
                List<BigDecimal> filterDataList = dbList.stream().filter(ele -> line.equals(ele.getProductLineId()) && classMonth.equals(ele.getClassMonth())).map(QtCheckItemMaterialAvgDTO::getValue).collect(Collectors.toList());
                BigDecimal value = BigDecimalUtil.sum(filterDataList);
                sum = BigDecimalUtil.add(sum, value);
                columnSumMap.put(classMonth, BigDecimalUtil.add(columnSumMap.get(classMonth), value));
                dateDataList.add(String.valueOf(value));
            }
            row.setProductLineName(lineName);
            row.setSum(BigDecimalUtil.format(sum, 0));
            row.setDateDataList(dateDataList);
            rowList.add(row);
        }
        OriginalDefectRowVO sumRow = new OriginalDefectRowVO();
        sumRow.setType(1);
        sumRow.setProductLineName("合计");
        List<String> dateDataList = Lists.newArrayList();
        sumRow.setDateDataList(dateDataList);
        for (int i = 1; i < shardTimeList.size(); i++) {
            LocalDateTime startLdt = shardTimeList.get(i - 1);
            String classMonth = formatter.format(startLdt);
            dateDataList.add(BigDecimalUtil.format(columnSumMap.get(classMonth), 0));
        }
        BigDecimal allSum = BigDecimalUtil.sum(columnSumMap.values().stream().collect(Collectors.toList()));
        sumRow.setSum(BigDecimalUtil.format(allSum, 0));
        rowList.add(sumRow);
        return resVo;
    }


    /**
     * 计算折成3.2mm 3.0mm的Y,T值
     *
     * @param val   平均值-Y值 | 平均值-T值
     * @param x     3.2, 3.0
     * @param thick 所在行“厚度”
     * @return 值
     */
    private BigDecimal avgYtThick(BigDecimal val, BigDecimal x, BigDecimal thick) {
        //POWER(所在行“平均值-Y值” / (0.96 * 0.96 * 100), 3.2 / 所在行“厚度”) * 100 * 0.96 * 0.96，小数点后最多2位
        BigDecimal first = BigDecimalUtil.divide(val, new BigDecimal("92.16"));
        BigDecimal second = BigDecimalUtil.divide(x, thick);
        double pow = 0;
        if (Objects.nonNull(first) && Objects.nonNull(second) && BigDecimalUtil.gt(first, BigDecimal.ZERO) && BigDecimalUtil.gt(second, BigDecimal.ZERO)) {
            pow = Math.pow(first.doubleValue(), second.doubleValue());
            pow = pow * 92.16;
        }
        return BigDecimal.valueOf(pow).setScale(2, BigDecimal.ROUND_HALF_UP);
    }


    /**
     * 设置透光率表头
     *
     * @param resVo 返回对象
     */
    private void setTransmitHeader(QtCheckItemTransmitVO resVo) {
        List<QtSpcHeadTitleVO> headerLeft = Lists.newArrayList();
        headerLeft.add(new QtSpcHeadTitleVO("产线", "productLineName"));
        headerLeft.add(new QtSpcHeadTitleVO("日期", "classDateStr"));
        headerLeft.add(new QtSpcHeadTitleVO("班次", "className"));
        headerLeft.add(new QtSpcHeadTitleVO("班组", "classGroupName"));
        headerLeft.add(new QtSpcHeadTitleVO("花纹深度", "figureDepth"));
        headerLeft.add(new QtSpcHeadTitleVO("厚度", "thickness"));
        List<BaseReportHeaderVO> headerRight = Lists.newArrayList();
        headerRight.add(new BaseReportHeaderVO("平均值", Arrays.asList("Y值", "T值")));
        headerRight.add(new BaseReportHeaderVO("折成3.2mm", Arrays.asList("Y值", "T值")));
        headerRight.add(new BaseReportHeaderVO("折成3.0mm", Arrays.asList("Y值", "T值")));
        resVo.setHeaderLeft(headerLeft);
        resVo.setHeaderRight(headerRight);
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
        List<QtCheckItemMaterialAvgDTO> thickAvgDTOList = qtCheckTaskMapper.checkItemMaterialAvg(dto.getProductLineList(), thickItemIdList, start, end, 2);
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

    /**
     * 从配置文件中获取数据
     *
     * @param key1 键1
     * @param key2 键2
     * @return
     */
    private String itemCodeFromCf(String key1, String key2) {
        Map<String, List<String>> eleMap = FeignOptional.of(() -> remoteSysConfigurationFeignApi.confListString(new TripleDTO<>(key1, Sets.newHashSet(key2), 2)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<String> valueList = eleMap.get(key2);
        if (CollUtil.isNotEmpty(valueList)) {
            return valueList.get(0);
        }
        return null;
    }
}
