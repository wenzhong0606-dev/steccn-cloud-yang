package com.steccn.pms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.pms.domain.PmsReportWork;
import com.steccn.pms.dto.DwCurrentReportWorkDTO;
import com.steccn.pms.dto.ProcessReportWorkPageDTO;
import com.steccn.pms.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
     * @param productLineIdList 产线集合
     * @param classesIdList     班次id
     * @param date              指定日期
     * @return list
     */
    List<SubstrateDataListVO> substrateReport(@Param("productLineIdList") List<Integer> productLineIdList, @Param("classesIdList") List<Long> classesIdList, @Param("date") LocalDate date);

    /**
     * 根据指定日期和产线获取产量
     *
     * @param productLineId 产线id
     * @param date          指定日期
     * @return
     */
    List<PmsReportWork> dateProductNum(@Param("productLineId") Integer productLineId, @Param("date") LocalDate date);

    /**
     * 根据日分组获取产量
     *
     * @param startDate
     * @param endDate
     * @return
     */
    List<PmsReportWorkGroupVO> selectAllGroupByDate(@Param("startTime") LocalDateTime startDate, @Param("endTime") LocalDateTime endDate, @Param("productLineIdList") List<Integer> productLineIdList);

    /**
     * 基于产线，班次，物料，工序分组报工数据【加工，合格，不良】
     *
     * @param productLineIdList 产线id
     * @param classesIdList     班次id
     * @param date              指定日期
     * @return list
     */
    List<FurtherProduceReportDataVO> furtherProduceReport(@Param("productLineIdList") List<Integer> productLineIdList, @Param("classesIdList") List<Long> classesIdList, @Param("date") LocalDate date);


    /**
     * 深加工日报表-原料耗料
     *
     * @param productLineIdList 产线id
     * @param classesIdList     班次id
     * @param date              指定日期
     * @return list
     */
    List<FurtherIngredientMapperVO> furtherIngredient(@Param("productLineIdList") List<Integer> productLineIdList, @Param("classesIdList") List<Long> classesIdList, @Param("date") LocalDate date);

    /**
     * 查询班次日期内所有的报工记录
     *
     * @param start 班次开始时间
     * @param end   班次结束时间
     * @return 所有的报工记录
     */
    List<DwCurrentReportWorkDTO> currentDateReportWork(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}




