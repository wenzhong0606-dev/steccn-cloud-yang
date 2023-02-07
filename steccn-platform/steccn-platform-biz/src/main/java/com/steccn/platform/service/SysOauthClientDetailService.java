package com.steccn.platform.service;

import com.steccn.platform.domain.SysOauthClientDetail;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Set;

/**
* @author yequan
* @description 针对表【sys_oauth_client_detail】的数据库操作Service
* @createDate 2022-12-16 15:33:47
*/
public interface SysOauthClientDetailService extends IService<SysOauthClientDetail> {

    /**
     * 获取所有客户端凭证加密后的字符串
     * @return
     */
    Set<String> clientEncodePassword();
}
