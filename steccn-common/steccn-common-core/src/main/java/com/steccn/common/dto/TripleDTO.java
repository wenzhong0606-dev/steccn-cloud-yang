package com.steccn.common.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yequan
 * @desc 三元组
 */
@Data
@ApiModel("三元组")
@NoArgsConstructor
@AllArgsConstructor
public class TripleDTO<E1, E2, E3> extends TupleDTO<E1, E2> {

    @ApiModelProperty("属性3")
    private E3 field3;

}
