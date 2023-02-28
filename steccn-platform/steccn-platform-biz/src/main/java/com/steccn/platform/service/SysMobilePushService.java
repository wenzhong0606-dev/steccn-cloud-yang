package com.steccn.platform.service;

import com.steccn.platform.dto.SysMobilePushDTO;

/**
 * @author yequan
 * @description 移动端push功能接口
 * @createDate 2023-02-28 15:21:15
 */
public interface SysMobilePushService {

    /**
     * 移动端推送服务
     *
     * @param dto dto1
     * @return true或者false
     */
    boolean push(SysMobilePushDTO dto);

}
