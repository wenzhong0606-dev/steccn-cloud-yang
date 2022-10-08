package com.steccn.pms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 工单报工
 * @author yequan
 * @TableName pms_report_work
 */
@TableName(value ="pms_report_work")
@Data
public class PmsReportWork implements Serializable {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 报工信息编码
     */
    private String code;

    /**
     * 工序id
     */
    private Long processId;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 加工数量
     */
    private BigDecimal processQuantity;

    /**
     * 不良数量
     */
    private BigDecimal badnessQuantity;

    /**
     * 数量单位
     */
    private Long unitId;

    /**
     * 批次
     */
    private String batchNo;

    /**
     * 班次id
     */
    private Integer classesId;

    /**
     * 产线id
     */
    private Integer produceLineId;

    /**
     * 工位id
     */
    private Integer stationId;

    /**
     * 班组id
     */
    private Integer classGroupId;

    /**
     * 生产订单编码
     */
    private String produceOrderNo;

    /**
     * 生产订单id
     */
    private Long produceOrderId;

    /**
     * 创建人id
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 创建人名称
     */
    @TableField(fill = FieldFill.INSERT)
    private String createName;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人id
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /**
     * 更新人名称
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateName;

    /**
     * 更新人时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 是否删除标识：0否 1是
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}