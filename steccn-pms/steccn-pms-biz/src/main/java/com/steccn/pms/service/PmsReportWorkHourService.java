package com.steccn.pms.service;

import com.steccn.pms.domain.PmsReportWorkHour;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.ProcessReportWorkHourDTO;

import java.util.List;

/**
 * @author yequan
 * @description 针对表【pms_report_work_hour(报工工时信息)】的数据库操作Service
 * @createDate 2022-09-28 15:18:51
 */
public interface PmsReportWorkHourService extends IService<PmsReportWorkHour> {

    /**
     * 保存工时信息
     *
     * @param hourDTOList 工时信息
     */
    void saveHourList(Long pmsReportWorkId, List<ProcessReportWorkHourDTO> hourDTOList);
}
