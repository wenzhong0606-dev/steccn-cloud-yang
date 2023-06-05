package com.steccn.ems.service;

import com.steccn.ems.domain.EpHeatPower;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
* @author yequan
* @description 针对表【ep_heat_power(余热发电表)】的数据库操作Service
* @createDate 2023-06-05 14:33:28
*/
public interface EpHeatPowerService extends IService<EpHeatPower> {

    /**
     * 获取开始时间到结束时间的 余热发点能耗
     * @param start 开始时间
     * @param end 结束时间
     * @return 余热发电能耗
     */
    List<BigDecimal> heatPower(LocalDate start, LocalDate end);
}
