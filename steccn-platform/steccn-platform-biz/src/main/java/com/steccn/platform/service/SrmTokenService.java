package com.steccn.platform.service;

import com.steccn.platform.vo.SafetyTokenVO;
import com.steccn.platform.vo.SrmTokenVO;

/**
 * srm系统获取token
 *
 * @author AndrewLuo
 * @date 2023/08/24
 */
public interface SrmTokenService {
    /**
     * 获取Srm系统token
     * @return
     */
    SrmTokenVO getSrmToken();

    /**
     * 获取SrmToken
     * @return
     */
    String getSrmOauthToken();
}
