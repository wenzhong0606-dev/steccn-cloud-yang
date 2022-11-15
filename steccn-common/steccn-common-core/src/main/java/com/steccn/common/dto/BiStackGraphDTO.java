package com.steccn.common.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 * @desc 堆叠图
 */
@Data
@ApiModel("堆叠图vo")
public class BiStackGraphDTO<E1, E2> implements Serializable {

    @ApiModelProperty("x轴")
    private String xAxis;

    /**
     * 如果x轴对应的只有一个堆叠图，则data只有一个元素，且E1为堆叠类型，E2为堆叠的数据
     */
    @ApiModelProperty("元素集合")
    private List<List<TupleDTO<E1, E2>>> data;

}
