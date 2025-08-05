package com.steccn.platform.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2025/08/05
 */
@Data
public class PlatformRoleMenuDTO {
    @NotNull(message = "用户id不能为空")
    private Integer roleId;
    private Set<Integer> menuIds;
}
