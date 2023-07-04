package com.steccn.qms.vo;


import lombok.Data;

@Data
public class QtCustomerComplainModelVO {


    /**
     * 主键
     */
    private Long id;

    /**
     * 客诉记录id
     */
    private Long customerComplainId;

    /**
     * 组件序列号
     */
    private String moduleNumber;

}
