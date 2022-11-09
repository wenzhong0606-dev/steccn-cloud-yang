package com.steccn.pms.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.pms.domain.PmsReportWork;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.pms.dto.PmsDayReportDTO;
import com.steccn.pms.dto.ProcessReportWorkPageDTO;
import com.steccn.pms.vo.ProcessReportWorkPageVO;
import com.steccn.pms.vo.SubstrateDataListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * @author yequan
 * @description 针对表【pms_report_work(工单报工)】的数据库操作Mapper
 * @createDate 2022-09-28 15:18:51
 * @Entity com.steccn.pms.domain.PmsReportWork
 */
@Mapper
public interface PmsReportWorkMapper extends BaseMapper<PmsReportWork> {

    /**
     * 工序报工分页列表
     *
     * @param page 分页参数
     * @param dto  条件
     * @return vo
     */
    Page<ProcessReportWorkPageVO> processReportPage(Page page, @Param("dto") ProcessReportWorkPageDTO dto);

    /**
     * 基片日报表行数据
     *
     * @param dto dto
     * @return list
     */
    List<SubstrateDataListVO> substrateReport(PmsDayReportDTO dto);

    /**
     * 根据指定日期和产线获取产量
     *
     * @param productLineId 产线id
     * @param date          指定日期
     * @return
     */
    List<PmsReportWork> dateProductNum(@Param("productLineId") Integer productLineId, @Param("date") LocalDate date);
}




