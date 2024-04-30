package com.steccn.ems.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.util.MapUtil;
import com.steccn.common.util.ShardTimeUtil;
import com.steccn.ems.service.CeCarbonReportService;
import com.steccn.ems.service.CeElectricSettingService;
import com.steccn.ems.service.CeFossilFuelSettingService;
import com.steccn.ems.service.CeProduceIngredientSettingService;
import com.steccn.ems.vo.CeCarbonSumReportListVO;
import com.steccn.ems.vo.CeCarbonSumReportVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.util.XAxisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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

        return resVo;
    }

    public List<CeCarbonSumReportListVO> convert(List<LocalDate> dateList, String categoryName, List<TupleDTO<String, Map<LocalDate, BigDecimal>>> dataList) {
        List<CeCarbonSumReportListVO> resList = Lists.newArrayList();
        if (CollUtil.isNotEmpty(dataList)) {
            for (TupleDTO<String, Map<LocalDate, BigDecimal>> data : dataList) {
                CeCarbonSumReportListVO resVo = new CeCarbonSumReportListVO();
                List<BigDecimal> valueList = Lists.newArrayList();
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
            total.setCategoryName("合计（单类别）");
            total.setDetailName("合计（单类别）");
            total.setValueList(totalValueList);
            total.setSum(BigDecimalUtil.setScale(BigDecimalUtil.sum(totalValueList), 2));
            resList.add(total);
        }
        return resList;
    }

}
