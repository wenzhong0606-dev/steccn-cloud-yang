package com.steccn.mes.dto;

import lombok.Data;

/**
 * 设备运行报表
 *
 * @author AndrewLuo
 * @date 2024/05/06
 */
@Data
public class EqpOperationReportDTO {
    /**
     * 设备编码
     */
    private String eqpCode;

    /**
     * 设备名称
     */
    private String eqpName;

    /**
     * 部门编码
     */
    private String departmentCode;

    /**
     * 分类编码
     */
    private String clsCode;

    /**
     * 状态
     */
    private Integer state;
}
