package com.steccn.platform.service;

import com.steccn.platform.vo.SafetyTokenVO;

/**
 * 安环系统service
 *
 * @author AndrewLuo
 * @date 2023/07/31
 */
public interface SafetyTokenService {
    /**
     * 获取安环token
     * @return
     */
    SafetyTokenVO getSafetyToken();
}
