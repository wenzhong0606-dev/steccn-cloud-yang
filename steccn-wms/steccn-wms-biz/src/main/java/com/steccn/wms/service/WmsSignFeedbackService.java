package com.steccn.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.constant.ResultCode;
import com.steccn.wms.domain.WmsSignFeedback;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.wms.dto.WmsSignFeedbackDTO;
import com.steccn.wms.vo.WmsSignFeedbackVO;

/**
* @author 34872
* @description 针对表【wms_sign_feedback】的数据库操作Service
* @createDate 2024-08-06 16:49:20
*/
public interface WmsSignFeedbackService extends IService<WmsSignFeedback> {

    Boolean add(WmsSignFeedbackDTO dto);

    Boolean updateSignFeedback(WmsSignFeedbackDTO dto);

    WmsSignFeedbackVO get(Long id);

    Page<WmsSignFeedbackVO> pageList(WmsSignFeedbackDTO dto);

    Boolean affirm(WmsSignFeedbackDTO dto);

    Boolean sendBack(Long id);

    Long countSendBack();

    Page<WmsSignFeedbackVO> sendBackList(WmsSignFeedbackDTO dto);
}
