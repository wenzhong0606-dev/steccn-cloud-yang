package com.steccn.common.vo;

import lombok.Data;

/**
 * @author yequan
 * @desc 值 + name组合
 * @date 2023/11/30
 */
@Data
public class BaseValueNameVO<T> extends BaseValueVO<T> {

    private static final long serialVersionUID = 1L;

    public BaseValueNameVO(T data, String name) {
        super(data);
        this.name = name;
    }

    /**
     * 名称
     */
    private String name;
}
