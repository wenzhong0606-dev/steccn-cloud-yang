package com.steccn.platform.service;

import com.steccn.platform.domain.HikResponse;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.platform.dto.HikDbDTO;

/**
* @author Admin
* @description 针对表【hik_response】的数据库操作Service
* @createDate 2024-05-16 09:39:14
*/
public interface HikResponseService extends IService<HikResponse> {
    /**
     * 保存推送信息
     * @param dto
     * @return
     */
    Boolean saveResponse(HikDbDTO dto);
}
