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

    /**
     * 电
     *
     * @param dto dto
     * @return
     */
    PmsOperateEnergyVO electric(DpPmsOperateDTO dto);

    /**
     * 单耗 （天然气用量*7800+重油用量*1000*9300)/(拉引量*1000)
     * @param dto
     * @return
     */
    PmsOperateEnergyVO unitConsume(DpPmsOperateDTO dto);
}
