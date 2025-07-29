package com.steccn.platform.dto;

import com.steccn.common.dto.BasePageDTO;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2025/07/29
 */
@Data
public class PlatformRoleDTO extends BasePageDTO {
    /**
     * 主键id
     */
    private Integer id;

    /**
     * 角色编码
     */
    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    /**
     * 角色排序（升序）
     */
    private Integer sort;

    /**
     * 状态（0正常 1停用）
     */
    private Integer status;

    /**
     * 用户id
     */
    private Long userId;
}
