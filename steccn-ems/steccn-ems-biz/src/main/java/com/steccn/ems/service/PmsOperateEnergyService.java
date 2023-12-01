package com.steccn.ems.service;

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
}
