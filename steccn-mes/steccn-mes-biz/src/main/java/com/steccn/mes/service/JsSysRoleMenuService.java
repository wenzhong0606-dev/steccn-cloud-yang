package com.steccn.mes.service;

import com.steccn.mes.domain.JsSysRoleMenu;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Admin
* @description 针对表【js_sys_role_menu(角色与菜单关联表)】的数据库操作Service
* @createDate 2024-05-29 10:18:41
*/
public interface JsSysRoleMenuService extends IService<JsSysRoleMenu> {

    /**
     * 根据userName获取菜单权限
     * @param userName
     * @return
     */
    List<Long> menuIds(String userName);

}
