package com.steccn.qms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 客诉，不合格品关联整改表
 * @TableName qms_complain_defect_link
 */
@TableName(value ="qms_complain_defect_link")
@Data
public class QmsComplainDefectLink implements Serializable {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 业务类型：0客诉 1不合格品 2整改
     */
    private Integer busiType;

    /**
     * 业务主键
     */
    private Long busiId;

    /**
     * 关联类型：0客诉 1不合格品 2整改
     */
    private Integer linkType;

    /**
     * 关联主键
     */
    private Long linkId;

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