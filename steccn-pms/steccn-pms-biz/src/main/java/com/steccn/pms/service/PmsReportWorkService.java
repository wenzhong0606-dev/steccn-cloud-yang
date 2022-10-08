package com.steccn.pms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.pms.domain.PmsReportWork;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.ProcessReportWorkPageDTO;
import com.steccn.pms.vo.ProcessReportWorkPageVO;

/**
* @author yequan
* @description 针对表【pms_report_work(工单报工)】的数据库操作Service
* @createDate 2022-09-28 15:18:51
*/
public interface PmsReportWorkService extends IService<PmsReportWork> {

    /**
     * 工序分页列表
     * @param dto 条件
     * @return 分页数据
     */
    Page<ProcessReportWorkPageVO> pageList(ProcessReportWorkPageDTO dto);
}
