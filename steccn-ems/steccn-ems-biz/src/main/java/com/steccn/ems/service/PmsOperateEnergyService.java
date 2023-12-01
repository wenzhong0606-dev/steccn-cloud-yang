package com.steccn.ems.service;

import com.steccn.common.constant.ResultCode;
import com.steccn.dp.dto.DpPmsOperateDTO;
import com.steccn.ems.vo.PmsOperateEnergyVO;

/**
 * @author yequan
 * @desc 运营驾驶舱-能源服务接口
 * @date 2023/12/1
 */
public interface PmsOperateEnergyService {

    /**
     * 天然气
     *
     * @param dto dto
     * @return
     */
    PmsOperateEnergyVO gas(DpPmsOperateDTO dto);

    /**
     * 重油
     *
     * @param dto dto
     * @return
     */
    PmsOperateEnergyVO oil(DpPmsOperateDTO dto);

    /**
     * 水
     *
     * @param dto dto
     * @return
     */
    PmsOperateEnergyVO water(DpPmsOperateDTO dto);
}
