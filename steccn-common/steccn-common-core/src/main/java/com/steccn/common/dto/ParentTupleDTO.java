package com.steccn.common.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @param <E1> 泛型1
 * @param <E2> 泛型2
 * @author yequan
 * @desc 父子关系的二元组
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("父子关系的二元组")
@ToString(callSuper = true)
public class ParentTupleDTO<E1, E2> extends TupleDTO<E1, E2> implements Serializable {

    @ApiModelProperty("孩子节点集合")
    private List<ParentTupleDTO<E1, E2>> children;

    /**
     * 两个参数的构造器
     *
     * @param e1 e1元素
     * @param e2 e2元素
     */
    public ParentTupleDTO(E1 e1, E2 e2) {
        this.setField1(e1);
        this.setField2(e2);
    }
}
