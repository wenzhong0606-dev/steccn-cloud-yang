package com.steccn.common.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author yequan
 */
@Data
@ApiModel("二元组")
@NoArgsConstructor
@AllArgsConstructor
public class TupleDTO<E1, E2> implements Serializable {

    @ApiModelProperty("属性1")
    private E1 field1;

    @ApiModelProperty("属性2")
    private E2 field2;

}
