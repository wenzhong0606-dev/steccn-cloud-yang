package com.steccn.dp.service;

import com.steccn.common.constant.ResultCode;
import com.steccn.dp.dto.DpPmsOperateDTO;
import com.steccn.dp.vo.DpPmsOperateCompleteStorageVO;

/**
 * @author yequan
 * @desc 生产管理-运营驾驶舱接口层
 * @date 2023/11/29
 */
public interface DpPmsOperateService {

    /**
     * 总入库数，入库合格率
     *
     * @param dto dto
     * @return
     */
    DpPmsOperateCompleteStorageVO inStorage(DpPmsOperateDTO dto);

    /**
     * 总完工数，完工合格率
     * @param dto
     * @return
     */
    DpPmsOperateCompleteStorageVO complete(DpPmsOperateDTO dto);
}
