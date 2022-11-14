package com.steccn.common.util;

import com.google.common.collect.Lists;
import com.steccn.common.dto.StackGraphDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * @author yequan
 * @desc 堆叠图的转换接口
 * @date 2022/11/14 14:47
 */
@Slf4j
public class StackGraphUtil {

    private static Random random = new Random();

    public static void main(String[] args) {
        List<StackGraphDTO> dataList = Lists.newArrayList();


        dataList.add(getStackGraphDTO("2022-09-15"));
        dataList.add(getStackGraphDTO("2022-09-16"));
        dataList.add(getStackGraphDTO("2022-09-17"));
        dataList.add(getStackGraphDTO("2022-09-18"));
        dataList.add(getStackGraphDTO("2022-09-19"));

        dataList.forEach(ele -> {
            log.info("{}", ele);
        });


    }

    private static StackGraphDTO<String, List<BigDecimal>> getStackGraphDTO(String x) {
        StackGraphDTO<String, List<BigDecimal>> stackGraphDTO = new StackGraphDTO();
        stackGraphDTO.setXAxis(x);
        List<TupleDTO<String, List<BigDecimal>>> data = Lists.newArrayList();


        data.add(new TupleDTO<>(x, Arrays.asList(BigDecimal.valueOf(random.nextInt(100)), BigDecimal.valueOf(random.nextInt(100)), BigDecimal.valueOf(random.nextInt(100)))));
        stackGraphDTO.setData(data);
        return stackGraphDTO;
    }

}
