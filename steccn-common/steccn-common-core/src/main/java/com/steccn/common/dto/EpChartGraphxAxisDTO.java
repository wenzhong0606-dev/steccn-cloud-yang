package com.steccn.common.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel("x轴的数据结构")
public class EpChartGraphxAxisDTO<E1, E2, E3> implements Serializable {

    @ApiModelProperty("x轴的一个点")
    private String xAxis;

    /**
     * 多根柱子，每根柱子分成多个堆叠
     */
    @ApiModelProperty("元素集合")
    private List<TupleDTO<E1, List<TupleDTO<E2, E3>>>> data;
}
