package com.steccn.platform.service;

import com.steccn.platform.domain.PlatformUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.platform.dto.PlatformUserDTO;
import com.steccn.platform.vo.PlatformUserVO;

/**
* @author Admin
* @description 针对表【platform_user(用户表)】的数据库操作Service
* @createDate 2025-07-29 14:19:10
*/
public interface PlatformUserService extends IService<PlatformUser> {
    /**
     * 新增用户
     * @param dto
     * @return
     */
    Boolean add(PlatformUserDTO dto);

    /**
     * 修改用户
     * @param dto
     * @return
     */
    Boolean update(PlatformUserDTO dto);

    /**
     * 删除用户
     * @param id
     * @return
     */
    Boolean delete(Long id);

    /**
     * 根据用户名获取信息
     * @param userName
     * @return
     */
    PlatformUserVO getByUserName(String userName);
}
