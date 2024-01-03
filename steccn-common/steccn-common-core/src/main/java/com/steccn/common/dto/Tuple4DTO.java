package com.steccn.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yequan
 * @desc 四元组
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tuple4DTO<E1, E2, E3, E4> extends TripleDTO<E1, E2, E3> {

    /**
     * 属性四
     */
    private E4 field4;

    /**
     * 全参构造函数
     *
     * @param e1 field1
     * @param e2 field2
     * @param e3 field3
     * @param e4 field4
     */
    public Tuple4DTO(E1 e1, E2 e2, E3 e3, E4 e4) {
        super(e1, e2, e3);
        field4 = e4;
    }
}
