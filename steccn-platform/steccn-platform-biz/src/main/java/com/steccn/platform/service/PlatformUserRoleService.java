package com.steccn.platform.service;

import com.steccn.platform.domain.PlatformUserRole;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.platform.domain.PlatformUserRoleDTO;

/**
* @author Admin
* @description 针对表【platform_user_role(用户与角色关联表)】的数据库操作Service
* @createDate 2025-07-29 16:23:29
*/
public interface PlatformUserRoleService extends IService<PlatformUserRole> {
    /**
     * 绑定用户角色
     * @param dto
     * @return
     */
    Boolean bind(PlatformUserRoleDTO dto);
}
