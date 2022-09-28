package com.steccn.qms.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 检验方案分组检验项目表
 * @TableName qt_check_plan_group_item
 */
@TableName(value ="qt_check_plan_group_item" , autoResultMap = true)
@Data
public class QtCheckPlanGroupItem implements Serializable {
    /**
     * 主键
     */
    @TableId
    private Integer id;

    /**
     * 分组id
     */
    private Integer groupId;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 检验项id
     */
    private Integer checkItemId;

    /**
     * 极差数组【存放检验项集合】
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Integer> rangeArray;

    /**
     * 平均数数组【存放检验项集合】
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Integer> averageArray;

    /**
     * 标准值
     */
    private BigDecimal standardValue;

    /**
     * 上偏差
     */
    private BigDecimal upperValue;

    /**
     * 下偏差
     */
    private BigDecimal lowerValue;

    /**
     * 关联缺陷项
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Integer> linkedDefectiveItem;

    /**
     * 关联客户
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> linkedCustomer;

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