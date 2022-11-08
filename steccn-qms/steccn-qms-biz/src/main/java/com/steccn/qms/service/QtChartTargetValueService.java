package com.steccn.qms.service;

import com.steccn.qms.domain.QtChartTargetValue;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.qms.dto.QtChatTargetValueDTO;

/**
* @author AndrewLuo
* @description 针对表【qt_chat_target_value】的数据库操作Service
* @createDate 2022-11-08 18:34:41
*/
public interface QtChartTargetValueService extends IService<QtChartTargetValue> {
    /**
     * 新建检验项
     *
     * @param dto
     * @return
     */
    Boolean add(QtChatTargetValueDTO dto);
}
