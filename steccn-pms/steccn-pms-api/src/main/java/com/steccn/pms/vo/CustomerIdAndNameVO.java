package com.steccn.pms.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 客户id和名字VO
 *
 * @author AndrewLuo
 * @date 2023/05/18
 */
@Data
public class CustomerIdAndNameVO implements Serializable {
    /**
     * 客户id
     */
    private Integer customerId;
    /**
     * 客户名字
     */
    private String customerName;
}
