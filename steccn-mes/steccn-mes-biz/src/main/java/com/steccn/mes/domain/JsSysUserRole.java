package com.steccn.mes.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 用户与角色关联表
 * @TableName js_sys_user_role
 */
@TableName(value ="js_sys_user_role")
@Data
public class JsSysUserRole implements Serializable {
    /**
     * 用户编码
     */
    private String userCode;

    /**
     * 角色编码
     */
    private String roleCode;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}