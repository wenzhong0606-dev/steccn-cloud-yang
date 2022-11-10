package com.steccn.qms.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class QtCheckGroupVO implements Serializable {
    /**
     * 分组id
     */
    private Integer id;

    /**
     * 分组名称
     */
    private String name;
}
