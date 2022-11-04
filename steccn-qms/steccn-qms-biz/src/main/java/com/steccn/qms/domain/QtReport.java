package com.steccn.qms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.steccn.qms.dto.QtBaseIdNameDTO;
import lombok.Data;

/**
 * 报表
 * @TableName qt_report
 */
@TableName(value ="qt_report",autoResultMap = true)
@Data
public class QtReport implements Serializable {
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 报表卡片类型
     */
    @TableField(value = "report_card_type")
    private String reportCardType;

    /**
     * 表报卡片名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 日期范围开始时间
     */
    @TableField(value = "time_scope_start")
    private LocalDate timeScopeStart;

    /**
     * 日期范围结束时间
     */
    @TableField(value = "time_scope_end")
    private LocalDate timeScopeEnd;

    /**
     * 粒度:小时、天、周、月、季度、年
     */
    @TableField(value = "granularity")
    private String granularity;

    /**
     * 物料名称
     */
    @TableField(value = "material_id")
    private Long materialId;

    /**
     * 检验类型:来料检1/首检2/在线检3/巡检4/出货检5/化验抽检6/配料抽检7
     */
    @TableField(value = "check_type")
    private Integer checkType;

    /**
     * 检验分组
     */
    @TableField(value = "check_group_id")
    private Integer checkGroupId;

    /**
     * 检验项：为选中检验分组当前快照下所有数值、百分比、自动计算类型的检验项
     */
    @TableField(value = "check_item_id")
    private Integer checkItemId;

    /**
     * 标准值
     */
    @TableField(value = "standard_value")
    private BigDecimal standardValue;

    /**
     * 上限
     */
    @TableField(value = "up_limit")
    private BigDecimal upLimit;

    /**
     * 下限
     */
    @TableField(value = "dowm_limit")
    private BigDecimal dowmLimit;

    /**
     * 生产车间
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private QtBaseIdNameDTO workshop;

    /**
     * 产线产生
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private QtBaseIdNameDTO productLine;

    /**
     * 客户
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private QtBaseIdNameDTO customer;

    /**
     * 物料使用车间
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private QtBaseIdNameDTO materialUseWorkshop;

    /**
     * 供应商
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private QtBaseIdNameDTO supplier;

    /**
     * 备注
     */
    @TableField(value = "note")
    private String note;

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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}