package com.steccn.ems.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.ResultCode;
import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.dto.StackGraphDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.*;
import com.steccn.common.vo.R;
import com.steccn.ems.service.CeCarbonReportService;
import com.steccn.ems.service.CeElectricSettingService;
import com.steccn.ems.service.CeFossilFuelSettingService;
import com.steccn.ems.service.CeProduceIngredientSettingService;
import com.steccn.ems.vo.CeCarbonSumReportListVO;
import com.steccn.ems.vo.CeCarbonSumReportVO;
import com.steccn.platform.constant.SysOfficeConstant;
import com.steccn.platform.dto.SysOfficeTypeKeyDTO;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.dto.PmsOperateDriverDTO;
import com.steccn.pms.feign.RemoteCompleteStorageFeignApi;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.util.MysqlDateKeyUtil;
import com.steccn.qms.util.XAxisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author yequan
 * @desc 碳排放报表接口实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CeCarbonReportServiceImpl implements CeCarbonReportService {

    private final CeElectricSettingService ceElectricSettingService;
    private final CeProduceIngredientSettingService ceProduceIngredientSettingService;
    private final CeFossilFuelSettingService ceFossilFuelSettingService;
    private final RemoteCompleteStorageFeignApi remoteCompleteStorageFeignApi;
    private final RemoteOfficeFeign remoteOfficeFeign;

    @Override
    public CeCarbonSumReportVO carbonSumReport(LocalDate start, LocalDate end, Integer granularityInt) {
        //获取有效期内的
        Map<LocalDate, BigDecimal> electricMap = ceElectricSettingService.carbonSumReportDay(start, end);
        List<TupleDTO<String, Map<LocalDate, BigDecimal>>> valueList2 = ceProduceIngredientSettingService.carbonSumReportDay(start, end);
        List<TupleDTO<String, Map<LocalDate, BigDecimal>>> valueList3 = ceFossilFuelSettingService.carbonSumReportDay(start, end);

        //根据开始日期和结束日期划分粒度
        //获取粒度
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(granularityInt);
        Assert.notNull(granularity, "粒度不存在");
        List<LocalDate> dateList = ShardTimeUtil.cutDate(granularity.getType(), start, end);
        List<String> headList = Lists.newArrayList();
        CeCarbonSumReportVO resVo = new CeCarbonSumReportVO();
        List<CeCarbonSumReportListVO> contentList = Lists.newArrayList();
        resVo.setContentList(contentList);
        for (int i = 1; i < dateList.size(); i++) {
            LocalDate startArea = dateList.get(i - 1);
            String convert = XAxisUtil.convert(granularity, startArea);
            headList.add(convert);
        }
        resVo.setHeadList(headList);
        contentList.addAll(convert(dateList, "化石燃料", valueList2));
        contentList.addAll(convert(dateList, "生产原料", valueList3));
        contentList.addAll(convert(dateList, "电力", Lists.newArrayList(new TupleDTO<>("净购入电力", electricMap))));
        //添加总计
        if (CollUtil.isNotEmpty(contentList)) {
            List<BigDecimal> sumValueList = Lists.newArrayList();
            for (int i = 1; i < dateList.size(); i++) {
                int finalI = i;
                List<BigDecimal> collect = contentList.stream().filter(ele -> ele.getType() != 0).flatMap(ele -> Stream.of(ele.getValueList().get(finalI - 1))).collect(Collectors.toList());
                sumValueList.add(BigDecimalUtil.setScale(BigDecimalUtil.sum(collect), 2));
            }
            CeCarbonSumReportListVO sum = new CeCarbonSumReportListVO();
            sum.setCategoryName("总计（吨）");
            sum.setType(3);
            sum.setDetailName("总计（吨）");
            sum.setValueList(sumValueList);
            sum.setSum(BigDecimalUtil.setScale(BigDecimalUtil.sum(sumValueList), 2));
            contentList.add(sum);
        }
        return resVo;
    }

    @Override
    public CeCarbonSumReportVO carbonStrongReport(LocalDate start, LocalDate end, Integer granularity) {
        CeCarbonSumReportVO resVo = new CeCarbonSumReportVO();
        CeCarbonSumReportVO sumVo = carbonSumReport(start, end, granularity);
        resVo.setHeadList(sumVo.getHeadList());
        List<CeCarbonSumReportListVO> contentList = Lists.newArrayList();
        resVo.setContentList(contentList);
        if (CollUtil.isNotEmpty(sumVo.getContentList())) {
            CeCarbonSumReportListVO sumRow = sumVo.getContentList().get(sumVo.getContentList().size() - 1);
            sumRow.setCategoryName("碳排放总量（吨）");
            sumRow.setDetailName("碳排放总量（吨）");
            contentList.add(sumRow);
        }
        //获取深加工制造部的officeId
        SysOfficeVO department = FeignOptional.of(() -> remoteOfficeFeign.infoCode(SysOfficeConstant.DEPARTMENT, "STEC005"), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(department, "深加工制造部节点不存在");
        PmsOperateDriverDTO driverDTO = new PmsOperateDriverDTO();
        driverDTO.setGranularity(granularity);
        driverDTO.setStart(start);
        driverDTO.setEnd(end);
        driverDTO.setUnit(PmManagerConstant.MEASURE_UNIT_TON);
        driverDTO.setOfficeId(department.getId());
        Map<String, BigDecimal> pmMap = FeignOptional.of(() -> remoteCompleteStorageFeignApi.sjgInStorageSum(driverDTO), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
        CeCarbonSumReportListVO pmRow = new CeCarbonSumReportListVO();
        List<BigDecimal> pmList = Lists.newArrayList();
        QtManagerConstant.QtGranularityEnum granularityEnum = QtManagerConstant.QtGranularityEnum.getByCode(granularity);
        List<LocalDate> dateList = ShardTimeUtil.cutDate(granularityEnum.getType(), start, end);
        for (int i = 1; i < dateList.size(); i++) {
            LocalDate startArea = dateList.get(i - 1);
            String convert = MysqlDateKeyUtil.convert(granularityEnum, startArea);
            pmList.add(BigDecimalUtil.divide(pmMap.get(convert), new BigDecimal("500"), 2));
        }
        pmRow.setValueList(pmList);
        pmRow.setCategoryName("总产量（万重箱）");
        pmRow.setDetailName("总产量（万重箱）");
        pmRow.setSum(BigDecimalUtil.sum(pmList));
        contentList.add(pmRow);
        //计算总计
        //添加总计
        List<BigDecimal> sumValueList = Lists.newArrayList();
        CeCarbonSumReportListVO one = contentList.get(0);
        CeCarbonSumReportListVO two = contentList.get(1);
        for (int i = 0; i < sumVo.getHeadList().size(); i++) {
            BigDecimal oneValue = one.getValueList().get(i);
            BigDecimal twoValue = two.getValueList().get(i);
            sumValueList.add(BigDecimalUtil.divide(oneValue, twoValue, 2));
        }
        CeCarbonSumReportListVO sum = new CeCarbonSumReportListVO();
        sum.setCategoryName("碳排放强度（吨/万重箱）");
        sum.setDetailName("碳排放强度（吨/万重箱）");
        sum.setValueList(sumValueList);
        sum.setSum(BigDecimalUtil.divide(one.getSum(), two.getSum(), 2));
        contentList.add(sum);

        return resVo;
    }

    @Override
    public List<BigDecimal> analysisCarbonStrongValue(LocalDate start, LocalDate end, Integer granularity) {
        List<BigDecimal> resList = Lists.newArrayList();
        CeCarbonSumReportVO reportVO = carbonStrongReport(start, end, granularity);
        for (CeCarbonSumReportListVO vo : reportVO.getContentList()) {
            resList.add(vo.getSum());
        }
        while (resList.size() < 3) {
            resList.add(null);
        }
        return resList;
    }

    @Override
    public QtLineChartBarDTO analysisSumLine(LocalDate start, LocalDate end, Integer granularity) {
        return line(start, end, granularity, 0, "碳排放量（吨）");
    }

    @Override
    public QtLineChartBarDTO analysisPmLine(LocalDate start, LocalDate end, Integer granularity) {
        return line(start, end, granularity, 1, "碳排放量（吨）");
    }

    @Override
    public QtLineChartBarDTO analysisUnitLine(LocalDate start, LocalDate end, Integer granularity) {
        return line(start, end, granularity, 2, "碳排放强度（吨/万重箱）");
    }

    /**
     * 折线图
     *
     * @param start       开始日期
     * @param end         结束日期
     * @param granularity 粒度
     * @param idx         索引
     * @param hint        提示语
     * @return 折线
     */
    private QtLineChartBarDTO line(LocalDate start, LocalDate end, Integer granularity, Integer idx, String hint) {
        //定义数据集合
        List<StackGraphDTO<String, BigDecimal>> stackGraphDTOList = Lists.newArrayList();
        //获取数据
        CeCarbonSumReportVO reportVO = carbonStrongReport(start, end, granularity);
        if (CollUtil.isNotEmpty(reportVO.getContentList())) {
            CeCarbonSumReportListVO row = reportVO.getContentList().get(idx);
            List<String> headList = reportVO.getHeadList();
            for (int i = 0; i < row.getValueList().size(); i++) {
                BigDecimal value = row.getValueList().get(i);
                StackGraphDTO<String, BigDecimal> stackGraphDTO = new StackGraphDTO<>();
                stackGraphDTO.setXAxis(headList.get(i));
                List<TupleDTO<String, BigDecimal>> dataList = Lists.newArrayList();
                dataList.add(new TupleDTO<>(hint, value));
                stackGraphDTO.setData(dataList);
                stackGraphDTOList.add(stackGraphDTO);
            }
        }
        //自定义转换工具
        return StackGraphUtil.convertData(stackGraphDTOList);
    }

    /**
     * 转换数据
     *
     * @param dateList     日期集合
     * @param categoryName 类别集合
     * @param dataList     数据集合
     * @return
     */
    public List<CeCarbonSumReportListVO> convert(List<LocalDate> dateList, String categoryName, List<TupleDTO<String, Map<LocalDate, BigDecimal>>> dataList) {
        List<CeCarbonSumReportListVO> resList = Lists.newArrayList();
        if (CollUtil.isNotEmpty(dataList)) {
            for (TupleDTO<String, Map<LocalDate, BigDecimal>> data : dataList) {
                CeCarbonSumReportListVO resVo = new CeCarbonSumReportListVO();
                List<BigDecimal> valueList = Lists.newArrayList();
                resVo.setType(0);
                if (categoryName.contains("电力")) {
                    resVo.setType(2);
                }
                resVo.setValueList(valueList);
                resVo.setCategoryName(categoryName);
                resVo.setDetailName(data.getField1());
                Map<LocalDate, BigDecimal> dataMap = data.getField2();
                for (int i = 1; i < dateList.size(); i++) {
                    LocalDate startArea = dateList.get(i - 1);
                    LocalDate endArea = dateList.get(i);
                    BigDecimal value = null;
                    while (LocalDateUtil.lt(startArea, endArea)) {
                        value = BigDecimalUtil.add(value, dataMap.get(startArea));
                        startArea = startArea.plusDays(1);
                    }
                    valueList.add(BigDecimalUtil.setScale(value, 2));
                }
                resVo.setSum(BigDecimalUtil.setScale(BigDecimalUtil.sum(valueList), 2));
                resList.add(resVo);
            }
        }
        //如果超过1条，则添加合计
        if (resList.size() > 1) {
            CeCarbonSumReportListVO total = new CeCarbonSumReportListVO();
            Map<Integer, BigDecimal> totalDateMap = Maps.newHashMap();
            resList.forEach(ele -> {
                List<BigDecimal> valueList = ele.getValueList();
                for (int i = 0; i < valueList.size(); i++) {
                    MapUtil.add(totalDateMap, i, valueList.get(i), BigDecimalUtil::add);
                }
            });
            List<BigDecimal> totalValueList = Lists.newArrayList();
            for (int i = 1; i < dateList.size(); i++) {
                totalValueList.add(BigDecimalUtil.setScale(totalDateMap.get(i - 1), 2));
            }
            total.setType(1);
            total.setCategoryName("合计（单类别）");
            total.setDetailName("合计（单类别）");
            total.setValueList(totalValueList);
            total.setSum(BigDecimalUtil.setScale(BigDecimalUtil.sum(totalValueList), 2));
            resList.add(total);
        }
        return resList;
    }

}
