package com.steccn.platform.service;

import com.steccn.common.constant.ResultCode;
import com.steccn.platform.domain.SysUserClientRef;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.platform.dto.SysUserClientRefDTO;

/**
 * @author yequan
 * @description 针对表【sys_user_client_ref】的数据库操作Service
 * @createDate 2023-02-28 13:26:08
 */
public interface SysUserClientRefService extends IService<SysUserClientRef> {

    /**
     * 登录时-绑定移动端设备，用于push消息
     *
     * @param dto
     * @return
     */
    Boolean bind(SysUserClientRefDTO dto);

    /**
     * 获取用户最近的一个clientId
     *
     * @param userId 用户id
     * @return clientId
     */
    String lastClientId(Long userId);
}
