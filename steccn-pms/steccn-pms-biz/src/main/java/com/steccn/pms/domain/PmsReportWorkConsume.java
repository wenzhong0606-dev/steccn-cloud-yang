package com.steccn.pms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 报工能耗表
 * @author yequan
 * @TableName pms_report_work_consume
 */
@TableName(value ="pms_report_work_consume")
@Data
public class PmsReportWorkConsume implements Serializable {
    /**
     * 主键id
     */
    @TableId
    private Long id;

    /**
     * 0耗电量 1耗水量 2耗油量 3耗气量
     */
    private Integer type;

    /**
     * 数量
     */
    private BigDecimal quantity;

    /**
     * 单位
     */
    private Long unitId;

    /**
     * 工序报工id
     */
    private Long reportWorkId;

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