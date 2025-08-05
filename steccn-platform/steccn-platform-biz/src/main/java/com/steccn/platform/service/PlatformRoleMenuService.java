package com.steccn.platform.service;

import com.steccn.platform.domain.PlatformRoleMenu;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.platform.domain.PlatformUserRoleDTO;
import com.steccn.platform.dto.PlatformRoleMenuDTO;

/**
* @author Admin
* @description 针对表【platform_role_menu(角色与菜单关联表)】的数据库操作Service
* @createDate 2025-08-05 14:42:05
*/
public interface PlatformRoleMenuService extends IService<PlatformRoleMenu> {

    /**
     * 绑定角色菜单
     * @param dto
     * @return
     */
    Boolean bind(PlatformRoleMenuDTO dto);
}
