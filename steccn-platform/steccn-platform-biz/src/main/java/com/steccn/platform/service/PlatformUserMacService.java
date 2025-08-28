package com.steccn.platform.service;

import com.steccn.platform.domain.PlatformUserMac;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Admin
* @description 针对表【platform_user_mac(用户mac地址关联表)】的数据库操作Service
* @createDate 2025-08-28 15:27:49
*/
public interface PlatformUserMacService extends IService<PlatformUserMac> {
    /**
     * 匹配用户的mac地址
     * @param userId
     * @return
     */
    Boolean compareMac(Long userId,String macAddress);

    /**
     * 清除mac用户mac地址
     * @param userId
     * @return
     */
    Boolean clearMac(Long userId);
}
