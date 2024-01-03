package com.steccn.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yequan
 * @desc 五元组
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tuple5DTO<E1, E2, E3, E4, E5> extends Tuple4DTO<E1, E2, E3, E4> {

    /**
     * 属性四
     */
    private E5 field5;

    /**
     * 全参构造函数
     *
     * @param e1 field1
     * @param e2 field2
     * @param e3 field3
     * @param e4 field4
     * @param e5 field5
     */
    public Tuple5DTO(E1 e1, E2 e2, E3 e3, E4 e4, E5 e5) {
        super(e1, e2, e3, e4);
        field5 = e5;
    }
}
