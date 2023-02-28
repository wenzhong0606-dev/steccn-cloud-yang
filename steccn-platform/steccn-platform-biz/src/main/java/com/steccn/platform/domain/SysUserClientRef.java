package com.steccn.platform.domain;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 
 * @TableName sys_user_client_ref
 */
@TableName(value ="sys_user_client_ref")
@Data
public class SysUserClientRef implements Serializable {
    /**
     * 主键id
     */
    @TableId
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户名称
     */
    private String username;

    /**
     * 用户常用的客户端code
     */
    private String clientNo;

    /**
     * 最近登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

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