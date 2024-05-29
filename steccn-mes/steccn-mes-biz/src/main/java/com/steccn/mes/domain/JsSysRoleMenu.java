package com.steccn.mes.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 角色与菜单关联表
 * @TableName js_sys_role_menu
 */
@TableName(value ="js_sys_role_menu")
@Data
public class JsSysRoleMenu implements Serializable {
    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 菜单编码
     */
    private String menuCode;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}