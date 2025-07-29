package com.steccn.platform.domain;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2025/07/29
 */
@Data
public class PlatformUserRoleDTO {
    @NotNull(message = "用户id不能为空")
    private Long userId;
    private Set<Integer> roleIds;
}
