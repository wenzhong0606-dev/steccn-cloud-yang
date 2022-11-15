package com.steccn.common.util;

import com.google.common.collect.Lists;
import com.steccn.common.dto.*;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
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

    /**
     * 单只柱子转换数据
     *
     * @param dataList 数据集合
     * @return
     */
    public static <T1> QtLineChartBarDTO convertData(@NotNull List<StackGraphDTO<T1, BigDecimal>> dataList) {
        List<BiStackGraphDTO<T1, BigDecimal>> biDataList = Lists.newArrayList();
        //适配biConvertData
        for (StackGraphDTO<T1, BigDecimal> data : dataList) {
            BiStackGraphDTO<T1, BigDecimal> biStackGraphDTO = new BiStackGraphDTO<>();
            biStackGraphDTO.setXAxis(data.getXAxis());
            List<List<TupleDTO<T1, BigDecimal>>> list = Lists.newArrayList();
            list.add(data.getData());
            biStackGraphDTO.setData(list);
        }
        QtLineChartBiBarDTO qtLineChartBiBarDTO = biConvertData(biDataList);
        QtLineChartBarDTO resDto = new QtLineChartBarDTO();
        resDto.setName(qtLineChartBiBarDTO.getName());
        resDto.setXData(qtLineChartBiBarDTO.getXData());
        resDto.setSeriesDataVO((List<QtSeriesDataDTO>) qtLineChartBiBarDTO.getSeriesDataVO().get(0));
        return resDto;
    }

    /**
     * 多只柱子转换
     *
     * @param dataList
     * @return
     */
    public static <T1> QtLineChartBiBarDTO biConvertData(@NotNull List<BiStackGraphDTO<T1, BigDecimal>> dataList) {
        QtLineChartBiBarDTO qtLineChartBarDTO = new QtLineChartBiBarDTO();
        qtLineChartBarDTO.setXData(dataList.stream().map(v -> v.getXAxis()).collect(Collectors.toList()));
        //存放x轴的map
        LinkedHashMap<Integer, Map<T1, List<BigDecimal>>> dataMap = new LinkedHashMap<>();

        for (int i = 0; i < dataList.size(); i++) {
            List<List<TupleDTO<T1, BigDecimal>>> data = dataList.get(i).getData();
            //一个x轴对应多个柱子，现在处理第一个柱子
            for (int k = 0; k < data.size(); k++) {
                List<TupleDTO<T1, BigDecimal>> tupleDTOS = data.get(k);
                for (int j = 0; j < tupleDTOS.size(); j++) {
                    if (!dataMap.containsKey(k)) {
                        dataMap.put(k, new LinkedHashMap<>());
                    }
                    TupleDTO<T1, BigDecimal> sbDto = tupleDTOS.get(j);
                    Map<T1, List<BigDecimal>> map1 = dataMap.get(k);
                    if (!map1.containsKey(sbDto.getField1())) {
                        map1.put(sbDto.getField1(), Lists.newArrayList());
                    }
                    map1.get(sbDto.getField1()).add(sbDto.getField2());
                }
            }
        }
        //存放数据的vo
        List<List<QtSeriesDataDTO>> resDataVoList = Lists.newArrayList();

        //图例名称与标识器
        AtomicReference<Boolean> flag = new AtomicReference<>(Boolean.TRUE);
        List<T1> nameList = Lists.newArrayList();
        //存放返回的数据
        for (Map.Entry<Integer, Map<T1, List<BigDecimal>>> entry : dataMap.entrySet()) {
            List<QtSeriesDataDTO> seriesDataVOList = Lists.newArrayList();

            for (Map.Entry<T1, List<BigDecimal>> entryIn : entry.getValue().entrySet()) {
                QtSeriesDataDTO<T1, BigDecimal> qtSeriesDataDTO = new QtSeriesDataDTO();
                qtSeriesDataDTO.setName(entryIn.getKey());
                qtSeriesDataDTO.setData(entryIn.getValue());
                qtSeriesDataDTO.setStack("stack");
                qtSeriesDataDTO.setType("graph");
                seriesDataVOList.add(qtSeriesDataDTO);
                if (flag.get()) {
                    nameList.add(entryIn.getKey());
                }
            }
            flag.set(Boolean.FALSE);
            resDataVoList.add(seriesDataVOList);
        }
        qtLineChartBarDTO.setSeriesDataVO(resDataVoList);
        qtLineChartBarDTO.setName(nameList);
        return qtLineChartBarDTO;
    }

    private static BiStackGraphDTO<String, BigDecimal> getStackGraphDTO(String x) {
        BiStackGraphDTO<String, BigDecimal> stackGraphDTO = new BiStackGraphDTO();
        stackGraphDTO.setXAxis(x);
        List<List<TupleDTO<String, BigDecimal>>> data1 = Lists.newArrayList();
        List<TupleDTO<String, BigDecimal>> dataList1 = Lists.newArrayList();
        dataList1.add(new TupleDTO<>("甲班", BigDecimal.valueOf(random.nextInt(100))));
        dataList1.add(new TupleDTO<>("乙班", BigDecimal.valueOf(random.nextInt(100))));
        dataList1.add(new TupleDTO<>("丙班", BigDecimal.valueOf(random.nextInt(100))));
        data1.add(dataList1);

        List<TupleDTO<String, BigDecimal>> dataList2 = Lists.newArrayList();
        dataList2.add(new TupleDTO<>("甲班", BigDecimal.valueOf(random.nextInt(100))));
        dataList2.add(new TupleDTO<>("乙班", BigDecimal.valueOf(random.nextInt(100))));
        dataList2.add(new TupleDTO<>("丙班", BigDecimal.valueOf(random.nextInt(100))));
        data1.add(dataList2);

        stackGraphDTO.setData(data1);
        return stackGraphDTO;
    }

//    public static void main(String[] args) {
//        List<StackGraphDTO<String, BigDecimal>> dataList = Lists.newArrayList();
//        dataList.add(getStackGraphDTO("2022-09-15"));
//        dataList.add(getStackGraphDTO("2022-09-16"));
//        dataList.add(getStackGraphDTO("2022-09-17"));
//        dataList.add(getStackGraphDTO("2022-09-18"));
//        dataList.add(getStackGraphDTO("2022-09-19"));
//        dataList.forEach(ele -> {
//            log.info("{}", ele);
//        });
//        QtLineChartBarDTO qtLineChartBarDTO = convertData(dataList);
//        log.info("{}", qtLineChartBarDTO);
//    }

}
