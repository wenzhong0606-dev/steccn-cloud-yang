package com.steccn.qms.service;

import com.steccn.common.vo.R;
import com.steccn.qms.dto.QtThirdPartyDefectiveDTO;

/**
 * @desc 第三方同步缺陷数据接口
 */
public interface QtThirdPartyService {

    /**
     * 保存推送过来的缺陷项
     *
     * @param dto dto
     * @return 保存过来的数据
     */
    R<Integer> saveDefective(QtThirdPartyDefectiveDTO dto);
}
