package com.steccn.mes.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 设备操作报表VO
 *
 * @author AndrewLuo
 * @date 2024/05/06
 */
@Data
public class EqpOperationReportVO {
    /**
     * 设备Id
     */
    private Long eqpId;
    /**
     * 设备编码
     */
    private String eqpCode;

    /**
     * 设备名称
     */
    private String eqpName;

    /**
     * 状态
     */
    private Integer state;

    /**
     * 分类名称
     */
    private String clsName;

    /**
     * 使用部门
     */
    private String officeName;

    /**
     * 本次运行时长
     */
    private BigDecimal startPeriod;

    /**
     * 累计运行时长
     */
    private BigDecimal startPeriodAll;

    /**
     * 停机时长
     */
    private BigDecimal stopPeriod;

    /**
     * 最长运行时长
     */
    private BigDecimal maxStartPeriod;

    /**
     * 最短运行时长
     */
    private BigDecimal minStartPeriod;
}
