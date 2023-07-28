package com.steccn.platform.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2023/07/28
 */
@Data
public class SysErpOfficeVO implements Serializable {
    private Integer id;

    /**
     * 机构code
     */
    private String officeCode;

    /**
     * 机构id
     */
    private Integer officeId;

    /**
     * 机构名字
     */
    private String officeName;

    /**
     * erp编码
     */
    private String erpCode;

    /**
     * erp名字
     */
    private String erpName;
}
