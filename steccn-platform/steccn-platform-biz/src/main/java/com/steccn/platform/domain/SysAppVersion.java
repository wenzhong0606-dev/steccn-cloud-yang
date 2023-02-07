package com.steccn.platform.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * app的版本控制表
 * @TableName sys_app_version
 */
@TableName(value ="sys_app_version")
@Data
public class SysAppVersion implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 版本
     */
    private String version;

    /**
     * 备注
     */
    private String note;

    /**
     * 文件id
     */
    private Long fileId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 是否删除标识：0否 1是
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}