package com.steccn.qms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 客诉记录表
 * @TableName qms_customer_complain
 */
@TableName(value ="qt_customer_complain")
@Data
public class QtCustomerComplain implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 客诉单号
     */
    private String code;

    /**
     * 标题
     */
    private String title;

    /**
     * 描述
     */
    private String description;

    /**
     * 投诉客户
     */
    private Integer complainCustomer;

    /**
     * 投诉日期
     */
    private LocalDate complainDate;

    /**
     * 投诉接收人
     */
    private Integer complainReceiver;

    /**
     * 投诉类型
     */
    private Integer complainType;

    /**
     * 处理人
     */
    private Integer dealMan;

    /**
     * 处理方式
     */
    private Integer dealWay;

    /**
     * 0-待处理、1-待审批、2-已关闭
     */
    private Integer status;

    /**
     * 备注
     */
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

    /**
     * 是否删除标识：0否 1是
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}