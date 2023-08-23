package com.steccn.platform.service;

import com.steccn.platform.vo.SafetyTokenVO;
import com.steccn.platform.vo.SalesTokenVO;

/**
 * 获取销售系统token
 *
 * @author AndrewLuo
 * @date 2023/08/23
 */
public interface SalesTokenService {
    /**
     * 获取销售系统token
     * @return
     */
    SalesTokenVO getSalesToken();
}
