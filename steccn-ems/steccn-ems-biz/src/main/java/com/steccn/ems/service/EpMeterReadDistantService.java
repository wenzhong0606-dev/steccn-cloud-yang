package com.steccn.ems.service;

import com.steccn.common.util.BigDecimalUtil;
import com.steccn.ems.domain.EpMeterReadDistant;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
* @author yequan
* @description 针对表【ep_meter_read_distant(远传仪表读数数据)】的数据库操作Service
* @createDate 2023-05-15 14:16:43
*/
public interface EpMeterReadDistantService extends IService<EpMeterReadDistant> {

    /**
     * 获取时间范围内的全部厚度
     * @param start 开始时间
     * @param end 结束时间
     * @return
     */
    List<BigDecimal> selectAllThick(LocalDate start, LocalDate end);
}
