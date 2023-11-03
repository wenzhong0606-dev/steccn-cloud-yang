package com.steccn.pms.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author yequan
 * @desc 外购原片不良日报表
 * @date 2023/11/3
 */
@Data
public class OutSourcingListDTO implements Serializable {

    private final static long serialVersionUID = 1L;

    /**
     * 报工主键
     */
    private Long reportWorkId;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 产线主键
     */
    private Integer productLineId;

    /**
     * 缺陷开始时间
     */
    private LocalDateTime startTime;

    /**
     * 缺陷结束时间
     */
    private LocalDateTime endTime;

    /**
     * 班次日期
     */
    private LocalDate classDate;

    /**
     * 班次主键
     */
    private Long classId;

    /**
     * 物料主键
     */
    private Long materialId;

    /**
     * 工位主键
     */
    private Integer stationId;

    /**
     * 报工数量
     */
    private BigDecimal quantity;

    /**
     * 供应商编码
     */
    private String supplierCode;

}
