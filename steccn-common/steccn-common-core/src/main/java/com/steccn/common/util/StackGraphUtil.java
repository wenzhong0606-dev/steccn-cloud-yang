package com.steccn.common.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.steccn.common.dto.*;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @desc 堆叠图的转换接口
 * @date 2022/11/14 14:47
 */
@Slf4j
public class StackGraphUtil {

    private static Random random = new Random();

    private static final String STR_STACK = "Total";
    private static final String STR_GRAPH = "line";
    public static final String STR_DEFAULT = "DEFAULT";

    /**
     * 单只柱子转换数据
     *
     * @param dataList 数据集合
     * @return
     */
    public static <T1, T2> QtLineChartBarDTO convertData(@NotNull List<StackGraphDTO<T1, T2>> dataList, boolean flag) {
        List<BiStackGraphDTO<T1, T2>> biDataList = Lists.newArrayList();
        //适配biConvertData
        for (StackGraphDTO<T1, T2> data : dataList) {
            BiStackGraphDTO<T1, T2> biStackGraphDTO = new BiStackGraphDTO<>();
            biStackGraphDTO.setXAxis(data.getXAxis());
            List<List<TupleDTO<T1, T2>>> list = Lists.newArrayList();
            list.add(data.getData());
            biStackGraphDTO.setData(list);
            biDataList.add(biStackGraphDTO);
        }
        QtLineChartBiBarDTO qtLineChartBiBarDTO = biConvertData(biDataList, flag);
        QtLineChartBarDTO resDto = new QtLineChartBarDTO();
        resDto.setLegend(qtLineChartBiBarDTO.getName());
        resDto.setXData(qtLineChartBiBarDTO.getXData());
        if (qtLineChartBiBarDTO.getSeriesDataVO().size() > 0) {
            resDto.setSeriesDataVO((List<QtSeriesDataDTO>) qtLineChartBiBarDTO.getSeriesDataVO().get(0));
        }
        return resDto;
    }

    /**
     * 单只柱子转换数据
     *
     * @param dataList 数据集合
     * @return
     */
    public static <T1, T2> QtLineChartBarDTO convertData(@NotNull List<StackGraphDTO<T1, T2>> dataList) {
        return convertData(dataList, Boolean.TRUE);
    }

    /**
     * 转换 默认为纵向图例
     *
     * @param dataList 数据集
     * @param <T1>
     * @param <T2>
     * @return
     */
    public static <T1, T2> QtLineChartBiBarDTO biConvertData(@NotNull List<BiStackGraphDTO<T1, T2>> dataList) {
        return biConvertData(dataList, Boolean.TRUE);
    }

    /**
     * 多只柱子转换
     *
     * @param dataList
     * @param legend   横向图例还是纵向图例 是标识纵向图例，否标识横向图例
     * @return
     */
    public static <T1, T2> QtLineChartBiBarDTO biConvertData(@NotNull List<BiStackGraphDTO<T1, T2>> dataList, boolean legend) {
        QtLineChartBiBarDTO qtLineChartBarDTO = new QtLineChartBiBarDTO();
        qtLineChartBarDTO.setXData(dataList.stream().map(v -> v.getXAxis()).collect(Collectors.toList()));
        //存放x轴的map
        LinkedHashMap<Integer, Map<Integer, List<T2>>> dataMap = new LinkedHashMap<>();

        //存放名称
        Map<String, T1> nameMap = Maps.newHashMap();

        for (int i = 0; i < dataList.size(); i++) {
            //获取x轴上i点的柱子集合
            List<List<TupleDTO<T1, T2>>> data = dataList.get(i).getData();
            for (int k = 0; k < data.size(); k++) {
                //如果k号柱子还没有map，则生成k号柱子
                if (!dataMap.containsKey(k)) {
                    dataMap.put(k, new LinkedHashMap<>());
                }
                //获取x轴上k号柱子
                List<TupleDTO<T1, T2>> tupleDTOS = data.get(k);
                for (int j = 0; j < tupleDTOS.size(); j++) {
                    //获取k号柱子的第j号堆叠图数据
                    TupleDTO<T1, T2> sbDto = tupleDTOS.get(j);
                    Map<Integer, List<T2>> map1 = dataMap.get(k);
                    if (!map1.containsKey(j)) {
                        map1.put(j, Lists.newArrayList());
                    }
                    nameMap.put(k + "_" + j, sbDto.getField1());
                    map1.get(j).add(sbDto.getField2());
                }
            }
        }
        //存放数据的vo
        List<List<QtSeriesDataDTO>> resDataVoList = Lists.newArrayList();

        //图例名称与标识器
        AtomicReference<Boolean> flag = new AtomicReference<>(Boolean.TRUE);
        List<T1> nameList = Lists.newArrayList();
        //存放返回的数据
        for (Map.Entry<Integer, Map<Integer, List<T2>>> entry : dataMap.entrySet()) {
            List<QtSeriesDataDTO> seriesDataVOList = Lists.newArrayList();

            for (Map.Entry<Integer, List<T2>> entryIn : entry.getValue().entrySet()) {
                QtSeriesDataDTO<T1, T2> qtSeriesDataDTO = new QtSeriesDataDTO();
                qtSeriesDataDTO.setName(nameMap.get(entry.getKey() + "_" + entryIn.getKey()));
                qtSeriesDataDTO.setData(entryIn.getValue());
                qtSeriesDataDTO.setStack(STR_STACK);
                qtSeriesDataDTO.setType(STR_GRAPH);
                seriesDataVOList.add(qtSeriesDataDTO);
                if (flag.get() && legend) {
                    //纵向图例
                    nameList.add(nameMap.get(entry.getKey() + "_" + entryIn.getKey()));
                }
            }
            if (!legend) {
                //横向图例
                Optional<Map.Entry<Integer, List<T2>>> first = entry.getValue().entrySet().stream().findFirst();
                nameList.add(nameMap.get(entry.getKey() + "_" + first.get().getKey()));
            }
            flag.set(Boolean.FALSE);
            resDataVoList.add(seriesDataVOList);
        }
        qtLineChartBarDTO.setSeriesDataVO(resDataVoList);
        qtLineChartBarDTO.setName(nameList);
        return qtLineChartBarDTO;
    }

//    private static BiStackGraphDTO<String, BigDecimal> getStackGraphDTO(String x) {
//        BiStackGraphDTO<String, BigDecimal> stackGraphDTO = new BiStackGraphDTO();
//        stackGraphDTO.setXAxis(x);
//        List<List<TupleDTO<String, BigDecimal>>> data1 = Lists.newArrayList();
//        List<TupleDTO<String, BigDecimal>> dataList1 = Lists.newArrayList();
//        dataList1.add(new TupleDTO<>("甲班", BigDecimal.valueOf(random.nextInt(100))));
//        dataList1.add(new TupleDTO<>("甲班", BigDecimal.valueOf(random.nextInt(100))));
//        dataList1.add(new TupleDTO<>("丙班", BigDecimal.valueOf(random.nextInt(100))));
//        data1.add(dataList1);
//
//        List<TupleDTO<String, BigDecimal>> dataList2 = Lists.newArrayList();
//        dataList2.add(new TupleDTO<>("甲班", BigDecimal.valueOf(random.nextInt(100))));
//        dataList2.add(new TupleDTO<>("乙班", BigDecimal.valueOf(random.nextInt(100))));
//        dataList2.add(new TupleDTO<>("丙班", BigDecimal.valueOf(random.nextInt(100))));
//        data1.add(dataList2);
//
//        stackGraphDTO.setData(data1);
//        return stackGraphDTO;
//    }
//
//    public static void main(String[] args) {
//        List<BiStackGraphDTO<String, BigDecimal>> dataList = Lists.newArrayList();
//        dataList.add(getStackGraphDTO("2022-09-15"));
//        dataList.add(getStackGraphDTO("2022-09-16"));
//        dataList.add(getStackGraphDTO("2022-09-17"));
//        dataList.add(getStackGraphDTO("2022-09-18"));
//        dataList.add(getStackGraphDTO("2022-09-19"));
//        dataList.forEach(ele -> {
//            log.info("{}", ele);
//        });
////        QtLineChartBarDTO qtLineChartBarDTO = biConvertData(dataList);
//        QtLineChartBiBarDTO qtLineChartBiBarDTO = biConvertData(dataList);
//        log.info("{}", qtLineChartBiBarDTO);
//    }

}
