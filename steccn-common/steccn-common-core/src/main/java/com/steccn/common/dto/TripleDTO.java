package com.steccn.common.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author yequan
 * @desc 三元组
 */
@Data
@ApiModel("三元组")
@NoArgsConstructor
@AllArgsConstructor
public class TripleDTO<E1, E2, E3> extends TupleDTO<E1, E2> implements Serializable {

    @ApiModelProperty("属性3")
    private E3 field3;

    public TripleDTO(E1 e1, E2 e2, E3 e3) {
        super(e1, e2);
        field3 = e3;
    }
}
