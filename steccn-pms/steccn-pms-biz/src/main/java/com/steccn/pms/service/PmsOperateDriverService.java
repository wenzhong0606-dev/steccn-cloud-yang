package com.steccn.pms.service;

import com.steccn.pms.dto.PmsOperateDriverDTO;
import com.steccn.pms.vo.PmsOperateDriverCompleteStorageVO;

/**
 * @author yequan
 * @description 运营驾驶舱
 * @createDate 2024-01-08 15:46:08
 */
public interface PmsOperateDriverService {

    /**
     * 总入库数，入库合格率
     *
     * @param dto dto
     * @return
     */
    PmsOperateDriverCompleteStorageVO inStorage(PmsOperateDriverDTO dto);

    /**
     * 总完工数，完工合格率
     * @param dto
     * @return
     */
    PmsOperateDriverCompleteStorageVO complete(PmsOperateDriverDTO dto);
}
