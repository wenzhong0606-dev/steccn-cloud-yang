package com.steccn.mes.vo;

import lombok.Data;

/**
 * erp系统客户VO
 *
 * @author AndrewLuo
 * @date 2023/07/26
 */
@Data
public class ErpCustomerVO {
    /**
     * 客户id
     */
    private Integer fcustid;
    /**
     * 客户code
     */
    private String fnumber;
    /**
     * 客户名字
     */
    private String fname;
}
