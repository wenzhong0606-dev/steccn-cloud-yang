package com.steccn.qms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 整改记录详情
 * @TableName qt_improve_record_detail
 */
@TableName(value ="qt_improve_record_detail")
@Data
public class QtImproveRecordDetail implements Serializable {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 整改主键
     */
    private Integer recordId;

    /**
     * 第几步 D1 D2 ...D8
     */
    private String approverStep;

    /**
     * 处理人主键
     */
    private Integer approverId;

    /**
     * 处理人名称
     */
    private String approverName;

    /**
     * 结论
     */
    private String approverConclusion;

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