package com.steccn.ems.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author chenhuan
 * @date 2022/12/1 9:19
 */
@Data
public class MeterReadMapperVO {
    /**
     * 节点id
     */
    private Long partitionId;

    /**
     * 父节点id
     */
    private Long parentId;

    /**
     * 机构名称
     */
    private String officeName;

    /**
     * 0实体远传仪表、1实体非远传仪表、2虚拟仪表
     */
    private Integer entityTable;

    /**
     * 仪表名称
     */
    private String meterName;

    /**
     * 仪表id
     */
    private Integer meterId;

    /**
     * 班次id
     */
    private Integer classId;

    /**
     * 抄表日期
     */
    private LocalDate readDate;

    /**
     * 管理员调整值
     */
    private BigDecimal adminValue;

    /**
     * 纠正值
     */
    private BigDecimal correctValue;

    /**
     * 抄表值
     */
    private BigDecimal readValue;

    private Integer meterReadId;

    private Integer status;

    private LocalDateTime endTime;

}
