package com.steccn.ems.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.constant.ResultCode;
import com.steccn.ems.domain.EpUnitConsumeAlarm;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.ems.dto.EpUnitConsumeAlarmDTO;
import com.steccn.ems.vo.EpUnitConsumeAlarmVO;

import java.time.LocalDate;

/**
* @author yequan
* @description 针对表【ep_unit_consume_alarm(单耗告警表)】的数据库操作Service
* @createDate 2022-11-29 19:27:28
*/
public interface EpUnitConsumeAlarmService extends IService<EpUnitConsumeAlarm> {

    /**
     * 单耗告警分页列表
     *
     * @param dto
     * @return
     */
    Page<EpUnitConsumeAlarmVO> page(EpUnitConsumeAlarmDTO dto);

    /**
     * 单耗告警 - 触发
     *
     * @param classDate 班次日期
     * @return
     */
    Boolean trigger(LocalDate classDate);

    /**
     * 能耗告警 - 触发
     *
     * @param classDate   班次日期
     * @param granularity 粒度
     * @return
     */
    Boolean consumeTrigger(LocalDate classDate, Integer granularity);
}
