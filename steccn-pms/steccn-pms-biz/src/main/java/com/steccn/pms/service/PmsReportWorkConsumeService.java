package com.steccn.pms.service;

import com.steccn.pms.domain.PmsReportWorkConsume;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.ProcessReportWorkConsumeDTO;

import java.util.List;

/**
* @author yequan
* @description 针对表【pms_report_work_consume(报工能耗表)】的数据库操作Service
* @createDate 2022-11-02 15:32:08
*/
public interface PmsReportWorkConsumeService extends IService<PmsReportWorkConsume> {

    /**
     * 保存能耗信息
     * @param id 报工主键id
     * @param consumeDTOList 能耗信息
      */
    void saveConsume(Long id, List<ProcessReportWorkConsumeDTO> consumeDTOList);
}
