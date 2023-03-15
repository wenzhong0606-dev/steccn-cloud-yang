package com.steccn.qms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 客诉不合格品进度表
 * @TableName qms_complain_defect_progress
 */
@TableName(value ="qms_complain_defect_progress")
@Data
public class QmsComplainDefectProgress implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 操作步骤
     */
    private String opStep;

    /**
     * 操作备注
     */
    private String opNote;

    /**
     * 操作人
     */
    private String opCreateName;

    /**
     * 操作时间
     */
    private LocalDateTime opTime;

    /**
     * 操作顺序
     */
    private Integer sort;

    /**
     * 业务类型 0客诉 1不合格品
     */
    private Integer busiType;

    /**
     * 业务主键
     */
    private Long busiId;

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