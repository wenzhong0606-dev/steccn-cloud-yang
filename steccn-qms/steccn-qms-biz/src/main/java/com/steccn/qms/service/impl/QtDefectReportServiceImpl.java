package com.steccn.qms.service.impl;

import cn.hutool.core.date.DatePattern;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.oa.dto.OaGroupCountDTO;
import com.steccn.oa.feign.OaInfoService;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.QtDefectiveItem;
import com.steccn.qms.dto.QtChartParamDTO;
import com.steccn.qms.dto.QtDefectReportDTO;
import com.steccn.qms.service.QtDefectReportService;
import com.steccn.qms.service.QtDefectiveItemService;
import com.steccn.qms.service.QtDefectiveReportService;
import com.steccn.qms.util.ShardTimeUtil;
import com.steccn.qms.vo.QtCheckTaskDetailReportVO;
import com.steccn.qms.vo.QtDefectReportDataVO;
import com.steccn.qms.vo.QtDefectReportVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
        //粒度来获取班次日期
        LocalDateTime start = LocalDateUtil.getMorning(dto.getStartDate());
        LocalDateTime end = LocalDateUtil.getMorning(dto.getEndDate().plusDays(1));
        String granularityStr;
        if (QtManagerConstant.LossReportType.MONTH.getCode().equals(dto.getGranularity())) {
            granularityStr = QtManagerConstant.QtGranularityEnum.MONTH.getType();
        } else if (QtManagerConstant.LossReportType.DAY.getCode().equals(dto.getGranularity())) {
            granularityStr = QtManagerConstant.QtGranularityEnum.DAY.getType();
        } else {
            granularityStr = QtManagerConstant.QtGranularityEnum.CLASS.getType();
        }
        List<LocalDateTime> shardTimeList = ShardTimeUtil.cutDateRangeNew(granularityStr, start, end);

        //获取产线的中文
        Map<Integer, String> lineIdNameMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(Sets.newHashSet(dto.getProductLineList())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

        //获取班组
        TupleDTO<LocalDate, LocalDate> timeTupleDTO = new TupleDTO<>(dto.getStartDate(), dto.getEndDate());
        Map<String, OaGroupCountDTO> classGroupMap = FeignOptional.of(() -> oaInfoService.getGroupInfo(timeTupleDTO), FeignConstant.STECCN_OA_SERVICE_NAME).get();

        //性能优化
//        Map<String, Integer> defectNumMap = Maps.newHashMap();
//        sourceDataList.forEach(ele -> {
//            String key = key(ele.getProductLineId(), ele.getClassesDate(), ele.getClassId(), ele.getDefectId());
//            defectNumMap.put(key, ele.getDefectNum() + defectNumMap.getOrDefault(key, 0));
//        });

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
                row.setClassId(LocalDateUtil.getClassId(begin, false));
                QtManagerConstant.QtClassesEnum classEnum = QtManagerConstant.QtClassesEnum.code(row.getClassId());
                row.setClassName(Objects.nonNull(classEnum) ? classEnum.getName() : Strings.EMPTY);
                row.setProductLine(line);
                row.setProductLineName(lineName);
                OaGroupCountDTO oaGroupCountDTO = classGroupMap.getOrDefault(classDate + ":" + row.getClassName(), new OaGroupCountDTO());
                row.setClassGroupId(oaGroupCountDTO.getGroupId());
                row.setClassGroupName(oaGroupCountDTO.getGroupName());
                List<String> valueList = Lists.newArrayList();
                row.setValueList(valueList);
                for (Integer defectId : defectIdList) {
                    int sum = sourceMap.getOrDefault(key(line, classDate, row.getClassId(), defectId), Lists.newArrayList()).stream().mapToInt(QtCheckTaskDetailReportVO::getDefectNum).sum();
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
            Integer num = valueMap.get(defectId);
            valueList.add(Objects.isNull(num) ? null : String.valueOf(num));
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
