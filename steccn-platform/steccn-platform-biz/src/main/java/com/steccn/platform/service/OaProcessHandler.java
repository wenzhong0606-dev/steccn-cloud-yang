package com.steccn.platform.service;

import com.steccn.platform.dto.OaProcessNotifyDTO;

/**
 * @author yequan
 * @desc OA流程处理
 * @date 2023/4/12
 */
public interface OaProcessHandler {


    /**
     * oa流程的处理
     *
     * @param dto
     */
    void handler(OaProcessNotifyDTO dto);
}
