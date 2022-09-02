package com.steccn.qms.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 检验方案分组检验项目表
 * @TableName qt_check_plan_group_item
 */
@TableName(value ="qt_check_plan_group_item")
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
    private Object rangeArray;

    /**
     * 平均数数组【存放检验项集合】
     */
    private Object averageArray;

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
    private Object linkedDefectiveItem;

    /**
     * 关联客户
     */
    private Object linkedCustomer;

    /**
     * 创建人id
     */
    private Long createBy;

    /**
     * 创建人名称
     */
    private String createName;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新人id
     */
    private Long updateBy;

    /**
     * 更新人名称
     */
    private String updateName;

    /**
     * 更新人时间
     */
    private Date updateTime;

    /**
     * 是否删除标识：0否 1是
     */
    private Integer delFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}