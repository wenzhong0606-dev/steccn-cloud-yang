package com.steccn.pms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.pms.domain.PmsReportWork;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.*;
import com.steccn.common.vo.BaseSelectVO;
import com.steccn.pms.vo.PmsReportWorkVO;
import com.steccn.pms.vo.ProcessReportWorkPageVO;

import java.util.List;

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

    /**
     * 提交报工
     * @param dto dto
     * @return 是否成功
     */
    Boolean submitReportWork(ProcessReportWorkSubmitDTO dto);

    /**
     * 报工记录
     * @param dto 筛选条件
     * @return 分页报工记录
     */
    Page<ProcessReportWorkPageVO> recordPage(ProcessReportWorkRecordDTO dto);

    /**
     * 物料消耗
     * @param dto dto
     * @return 物料消耗列表，不分页
     */
    List<ProcessReportWorkMaterialListDTO> materialConsume(ProcessReportWorkMaterialConsumeDTO dto);

    /**
     * 生产订单-工艺信息-报工信息
     * @param dto
     * @return 分页报工信息
     */
    Page<PmsReportWorkVO> reportWorkPageList(PmsReportWorkPageDTO dto);
}
