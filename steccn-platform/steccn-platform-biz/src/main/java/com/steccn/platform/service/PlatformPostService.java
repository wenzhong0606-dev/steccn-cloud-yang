package com.steccn.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.platform.domain.PlatformPost;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.platform.dto.PlatformPostDTO;
import com.steccn.platform.dto.PlatformRoleDTO;
import com.steccn.platform.vo.PlatformPostVO;
import com.steccn.platform.vo.PlatformRoleVO;

/**
* @author Admin
* @description 针对表【platform_post(岗位表)】的数据库操作Service
* @createDate 2025-07-31 16:40:21
*/
public interface PlatformPostService extends IService<PlatformPost> {
    /**
     * 新增角色
     * @param dto
     * @return
     */
    Boolean add(PlatformPostDTO dto);

    /**
     * 修改角色
     * @param dto
     * @return
     */
    Boolean update(PlatformPostDTO dto);

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
    Page<PlatformPostVO> pageList(PlatformPostDTO dto);

}
