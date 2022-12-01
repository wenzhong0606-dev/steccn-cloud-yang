package com.steccn.ems.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.ems.domain.EpMeterRead;
import com.steccn.ems.dto.EpMeterReadAddDTO;
import com.steccn.ems.vo.EqMeterReadVO;

/**
* @author yequan
* @description 针对表【ep_meter_read(抄表)】的数据库操作Service
* @createDate 2022-11-30 15:05:32
*/
public interface EpMeterReadService extends IService<EpMeterRead> {

    /**
     * 抄表管理列表
     *
     * @param readMonth 抄表月份
     * @param meterType 仪表类型
     * @return
     */
    EqMeterReadVO list(String readMonth, Integer meterType);

    /**
     * 录入抄表数据
     *
     * @param dto 入参
     * @return
     */
    Boolean add(EpMeterReadAddDTO dto);
}
