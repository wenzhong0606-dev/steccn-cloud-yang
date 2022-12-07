package com.steccn.ems.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.ems.domain.EpUnitConsumeAlarm;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.ems.dto.EpUnitConsumeAlarmDTO;
import com.steccn.ems.vo.EpUnitConsumeAlarmVO;

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
}
