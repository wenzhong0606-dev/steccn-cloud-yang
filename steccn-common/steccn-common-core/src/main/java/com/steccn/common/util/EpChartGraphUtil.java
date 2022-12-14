package com.steccn.common.util;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.steccn.common.dto.*;
import com.steccn.common.vo.EpChartGraphVO;
import com.steccn.common.vo.EpChartGraphXAxisVO;
import com.steccn.common.vo.EpChartUnitConsumeAlarmVO;
import com.steccn.common.vo.StandardVO;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author yequan
 * @desc 能源绩效图表封装-需要考虑到单耗预警
 */
@Slf4j
public class EpChartGraphUtil {

    private final static Random random = new Random();

    private static final String STR_STACK = "Total";
    private static final String STR_TYPE = "bar";
    public static final String STR_DEFAULT = "DEFAULT";

    /**
     * 数据转换
     *
     * @param epChartGraphDTO 数据对象
     * @param onlyStack       图例，显示堆叠true ， 显示x轴false
     * @return 对应结构
     */
    public static EpChartGraphVO<EpChartGraphXAxisVO, BigDecimal> convert(EpChartGraphDTO<Long, String, BigDecimal> epChartGraphDTO, Boolean onlyStack) {
        EpChartGraphVO<EpChartGraphXAxisVO, BigDecimal> resDTO = new EpChartGraphVO<>();

        //第一个x轴
        List<String> x1List = Lists.newArrayList();
        //第二个x轴
        List<String> x2List = Lists.newArrayList();
        //图例-堆叠图，图例就是堆叠
        List<String> legend = Lists.newArrayList();
        //图例的标识
        boolean onlyX = !onlyStack;

        Map<Long, String> officeMap = epChartGraphDTO.getOfficeMap();
        List<EpChartUnitConsumeAlarmVO> alarmList = Lists.newArrayList();
        resDTO.setAlarmList(alarmList);

        //存放数据的map
        Map<Integer, Map<Integer, List<BigDecimal>>> dataMap = new LinkedHashMap<>();

        //遍历x轴的所有数据  xData表示一个x轴的点-需要考虑是否需要预警
        int idx = 0;
        for (EpChartGraphxAxisDTO<Long, String, BigDecimal> x1 : epChartGraphDTO.getDataList()) {
            x1List.add(x1.getXAxis());
            //获取x轴一个点下的所有柱子
            List<TupleDTO<Long, List<TupleDTO<String, BigDecimal>>>> x2Data = x1.getData();

            //存放第一个x轴的数据
            dataMap.put(idx, Maps.newLinkedHashMap());
            //柱子的下标
            int xPostIdx = 0;
            for (TupleDTO<Long, List<TupleDTO<String, BigDecimal>>> x2 : x2Data) {
                //获取第一个x轴的数据
                Map<Integer, List<BigDecimal>> xDataMap = dataMap.get(idx);
                xDataMap.put(xPostIdx, Lists.newArrayList());
                //x2表示x轴上一根柱子
                x2List.add(officeMap.get(x2.getField1()));
                //获取每根柱子的图例
                //获取单根柱子的所有堆叠
                List<TupleDTO<String, BigDecimal>> stackList = x2.getField2();
                BigDecimal sum = BigDecimal.ZERO;
                for (TupleDTO<String, BigDecimal> stack : stackList) {
                    if (onlyStack) {
                        legend.add(stack.getField1());
                    }
                    sum = BigDecimalUtil.add(sum, stack.getField2());
                    xDataMap.get(xPostIdx).add(stack.getField2());
                }
                //如果单耗超过标准，则添加信息
                checkStandard(x2.getField1(), sum, epChartGraphDTO, alarmList);

                //如果不是堆叠图，则图例为x轴所有的点
                onlyStack = Boolean.FALSE;
                if (onlyX) {
                    legend.add(officeMap.get(x2.getField1()));
                }
                xPostIdx++;
            }
            onlyX = Boolean.FALSE;
            idx++;
        }
        //定义数据集合
        List<QtSeriesDataDTO> seriesDataVO = Lists.newArrayList();
        for (Map.Entry<Integer, Map<Integer, List<BigDecimal>>> entry1 : dataMap.entrySet()) {
            //代表第一个x轴的个数
            for (Map.Entry<Integer, List<BigDecimal>> entry2 : entry1.getValue().entrySet()) {
                //entry2代表柱子的个数
                for (int i = 0; i < entry2.getValue().size(); i++) {
                    //一个图例
                    if (seriesDataVO.size() <= i) {
                        QtSeriesDataDTO seriesDataDTO = new QtSeriesDataDTO();
                        seriesDataDTO.setName(legend.get(i));
                        seriesDataDTO.setData(Lists.newArrayList());
                        seriesDataDTO.setStack(STR_STACK);
                        seriesDataDTO.setType(STR_TYPE);
                        seriesDataVO.add(i, seriesDataDTO);
                    }
                    QtSeriesDataDTO dataDTO = seriesDataVO.get(i);
                    dataDTO.getData().add(entry2.getValue().get(i));
                }
            }
        }
        resDTO.setLegend(legend);
        resDTO.setSeriesDataVO(seriesDataVO);
        resDTO.setStandard(epChartGraphDTO.getStandardList());
        EpChartGraphXAxisVO xAxisVO = new EpChartGraphXAxisVO();
        xAxisVO.setData(x1List);
        xAxisVO.setData2(x2List);
        resDTO.setXData(xAxisVO);
        return resDTO;
    }

    /**
     * 检查是否超过标准
     *
     * @param value           数值
     * @param epChartGraphDTO 请求dto
     * @param alarmList       预警集合
     */
    private static void checkStandard(Long officeId, BigDecimal value, EpChartGraphDTO<Long, String, BigDecimal> epChartGraphDTO, List<EpChartUnitConsumeAlarmVO> alarmList) {
        //行业标准和企业标准
        if (CollUtil.isNotEmpty(epChartGraphDTO.getStandardList())) {
            for (StandardVO<BigDecimal> standardVO : epChartGraphDTO.getStandardList()) {
                if (Objects.nonNull(standardVO.getValue()) && Objects.nonNull(value) && standardVO.getValue().compareTo(value) < 0) {
                    int type = 1;
                    if (standardVO.getStandardTypeName().indexOf("行业") != -1) {
                        type = 0;
                    }
                    EpChartUnitConsumeAlarmVO vo = setAlarm(officeId, value, epChartGraphDTO, standardVO.getValue(), type);
                    alarmList.add(vo);
                }
            }
        }

        //部门标准
        if (CollUtil.isNotEmpty(epChartGraphDTO.getStandardMap())) {
            BigDecimal officeStandardValue = epChartGraphDTO.getStandardMap().get(officeId);
            if (Objects.nonNull(officeStandardValue) && Objects.nonNull(value) && officeStandardValue.compareTo(value) < 0) {
                EpChartUnitConsumeAlarmVO vo = setAlarm(officeId, value, epChartGraphDTO, officeStandardValue, 2);
                alarmList.add(vo);
            }
        }
    }

    /**
     * 设置预警
     *
     * @param officeId        部门id
     * @param value           值
     * @param epChartGraphDTO dto
     * @param standardValue   标准值
     * @param standardType    标准类型 0行业标准 1企业标准 2部门标准
     * @return
     */
    private static EpChartUnitConsumeAlarmVO setAlarm(Long officeId, BigDecimal value, EpChartGraphDTO<Long, String, BigDecimal> epChartGraphDTO, BigDecimal standardValue, Integer standardType) {
        EpChartUnitConsumeAlarmVO vo = new EpChartUnitConsumeAlarmVO();
        vo.setEnergyType(epChartGraphDTO.getEnergyType());
        vo.setOfficeId(officeId);
        vo.setOfficeName(epChartGraphDTO.getOfficeMap().get(officeId));
        vo.setStandardValue(standardValue);
        vo.setStandardType(standardType);
        vo.setCurrentValue(value);
        vo.setAlarmDateTime(LocalDateTime.now());
        vo.setConditionDate(epChartGraphDTO.getConditionDate());
        return vo;
    }

    /**
     * 生成测试数据
     *
     * @return
     */
    public static EpChartGraphDTO<Integer, String, BigDecimal> generateData() {
        EpChartGraphDTO<Integer, String, BigDecimal> resVo = new EpChartGraphDTO<>();

        Map<Integer, String> officeMap = Maps.newHashMap();
        officeMap.put(76, "基片二车间");
        officeMap.put(79, "深加工二车间");
        officeMap.put(80, "深加工三车间");
        resVo.setOfficeMap(officeMap);

        List<String> x1List = Lists.newArrayList("08-25~08-28", "08-29~09-04", "09-05~09-11", "09-12~09-18", "09-19~09-23");
        List<Integer> x2List = Lists.newArrayList(76, 79, 80);

        List<EpChartGraphxAxisDTO<Integer, String, BigDecimal>> dataList = Lists.newArrayList();
        for (String x1 : x1List) {
            EpChartGraphxAxisDTO<Integer, String, BigDecimal> xData = xData(x1, x2List);
            log.info("{}", xData);
            dataList.add(xData);
        }
        resVo.setDataList(dataList);
        StandardVO<BigDecimal> standardVO1 = new StandardVO();
        standardVO1.setName("企业标准");
        standardVO1.setValue(BigDecimal.valueOf(random.nextInt(100)));
        standardVO1.setStandardType(1L);
        standardVO1.setStandardTypeName("企业标准");
        StandardVO<BigDecimal> standardVO2 = new StandardVO();
        standardVO2.setName("行业标准");
        standardVO2.setValue(BigDecimal.valueOf(random.nextInt(100)));
        standardVO2.setStandardType(0L);
        standardVO2.setStandardTypeName("行业标准");
        resVo.setStandardList(Lists.newArrayList(standardVO1, standardVO2));
        resVo.setEnergyType(0);

        Map<Integer, BigDecimal> officeStandardMap = Maps.newHashMap();
        officeStandardMap.put(76, BigDecimal.valueOf(random.nextInt(100)));
        officeStandardMap.put(79, BigDecimal.valueOf(random.nextInt(100)));
        officeStandardMap.put(80, BigDecimal.valueOf(random.nextInt(100)));
        resVo.setStandardMap(officeStandardMap);

        resVo.setConditionDate(LocalDate.now());
        return resVo;
    }

    /**
     * 生成x轴数据
     *
     * @param xStr
     * @param x2List
     * @return
     */
    private static EpChartGraphxAxisDTO<Integer, String, BigDecimal> xData(String xStr, List<Integer> x2List) {
        EpChartGraphxAxisDTO<Integer, String, BigDecimal> xAxis1 = new EpChartGraphxAxisDTO();
        xAxis1.setXAxis(xStr);
        List<TupleDTO<Integer, List<TupleDTO<String, BigDecimal>>>> xDataList = Lists.newArrayList();
        for (Integer x2 : x2List) {
            List<TupleDTO<String, BigDecimal>> tupleDTOS = Lists.newArrayList(
                    new TupleDTO<>("2.0mm", BigDecimal.valueOf(random.nextInt(100))),
                    new TupleDTO<>("3.2mm", BigDecimal.valueOf(random.nextInt(100))),
                    new TupleDTO<>("4.0mm", BigDecimal.valueOf(random.nextInt(100))));
            xDataList.add(new TupleDTO<>(x2, tupleDTOS));
        }
        xAxis1.setData(xDataList);
        return xAxis1;
    }

//    public static void main(String[] args) {
//        EpChartGraphDTO<Integer, String, BigDecimal> vo = generateData();
//        EpChartGraphVO<EpChartGraphXAxisVO, BigDecimal> convert = convert(vo, Boolean.TRUE);
//        log.info("{}", convert);
//    }

}
