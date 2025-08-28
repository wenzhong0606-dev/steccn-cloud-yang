package com.steccn.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.platform.domain.PlatformRole;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.platform.dto.PlatformRoleDTO;
import com.steccn.platform.vo.PlatformRoleVO;

import java.util.List;

/**
* @author Admin
* @description 针对表【platform_role(角色表)】的数据库操作Service
* @createDate 2025-07-29 15:39:59
*/
public interface PlatformRoleService extends IService<PlatformRole> {
    /**
     * 新增角色
     * @param dto
     * @return
     */
    Boolean add(PlatformRoleDTO dto);

    /**
     * 修改角色
     * @param dto
     * @return
     */
    Boolean update(PlatformRoleDTO dto);

    /**
     * 删除用户
     * @param id
     * @return
     */
    Boolean delete(Long id);

    /**
     *
     * @param dto
     * @return
     */
    Page<PlatformRoleVO> pageList(PlatformRoleDTO dto);

    /**
     * 是否绑定
     * @param dto
     * @return
     */
    List<PlatformRoleVO> bindPageList(PlatformRoleDTO dto);
}
