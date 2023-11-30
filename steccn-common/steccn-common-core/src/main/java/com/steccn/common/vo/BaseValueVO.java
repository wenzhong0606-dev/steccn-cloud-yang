package com.steccn.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author yequan
 * @desc 基础值data元素
 * @date 2023/11/29
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseValueVO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数值元素
     */
    private T data;
}
