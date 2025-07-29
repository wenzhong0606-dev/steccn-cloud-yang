package com.steccn.platform.vo;

import lombok.Data;

/**
 * 角色VO
 *
 * @author AndrewLuo
 * @date 2025/07/29
 */
@Data
public class PlatformRoleVO {
    private Integer id;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色排序（升序）
     */
    private Integer sort;

    /**
     * 状态（0正常 1停用）
     */
    private String status;

    /**
     * 是否绑定0否 1是
     */
    private Integer isBind;
}
