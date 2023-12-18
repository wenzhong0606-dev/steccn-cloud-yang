package com.steccn.pms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.pms.domain.PmsReportWork;
import com.steccn.pms.dto.*;
import com.steccn.pms.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.parameters.P;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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
    List<SubstrateDataListVO> substrateReport(@Param("productLineIdList") List<Integer> productLineIdList, @Param("classesIdList") List<Long> classesIdList, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * 基片日报表行数据
     *
     * @param productLineIdList 产线集合
     * @param start             开始
     * @param end              结束
     * @return list
     */
    List<SubstrateDataListVO> qmsSubstrateReport(@Param("productLineIdList") List<Integer> productLineIdList, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
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
    List<FurtherProduceReportDataVO> furtherProduceReport(@Param("productLineIdList") List<Integer> productLineIdList, @Param("classesIdList") List<Long> classesIdList, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);


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


    /**
     * 查询产线下在这个日期之间的所有报工数据
     * @param lineIds  产线id
     * @param start 开始日期
     * @param end   结束日期
     * @return 报工记录详情数据
     */
    List<PmsReportWorkVO> listReportWorkInfo(@Param("lineIds") List<Integer> lineIds,@Param("start")  LocalDateTime start,@Param("end") LocalDateTime end);

    /**
     * 获取原片耗量
     *
     * @param lineList 产线集合
     * @param start    开始时间
     * @param end      结束时间
     * @return
     */
    List<PmsUseCountDTO> getUseCount(@Param("lineList") List<Integer> lineList,
                                     @Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);

    /**
     * *
     * @param lineIdSet
     * @param start
     * @param end
     * @return
     */
    List<PmsUseCountDTO> getUseCountClassDate(@Param("lineList") List<Integer> lineIdSet,
                                              @Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end);

    /**
     * 获取原片耗量-每个订单，班次日期
     *
     * @param lineList 产线集合
     * @param start    开始时间
     * @param end      结束时间
     * @return
     */
    List<PmsUseCountDTO> useCountPerOrder(@Param("lineList") List<Integer> lineList,
                                          @Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);

    /**
     * 获取原片耗量-每个订单，班次日期
     *
     * @param lineList 产线集合
     * @param start    开始时间
     * @param end      结束时间
     * @return
     */
    List<PmsUseCountDTO> useCountWeek(@Param("lineList") List<Integer> lineList,
                                      @Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end);

    /**
     * 获取原片耗量-每个订单，班次日期
     *
     * @param lineList 产线集合
     * @param start    开始时间
     * @param end      结束时间
     * @return
     */
    List<PmsUseCountDTO> useCountMonth(@Param("lineList") List<Integer> lineList,
                                       @Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);

    /**
     * 获取今日产量
     * @param lineList 产线集合
     * @param today 班次日期
     * @return
     */
    List<PmsUseCountDTO> getTodayPm(@Param("lineList") List<Integer> lineList, @Param("today") LocalDate today);

    /**
     * 获取产线产量
     *
     * @param lineList 产线集合
     * @param start    开始时间
     * @param end      结束时间
     * @return
     */
    List<PmsUseCountDTO> getDateRangePm(@Param("lineList") List<Integer> lineList, @Param("start") LocalDate start, @Param("end") LocalDate end);

    /**
     * 获取今日入库量
     *
     * @param lineIdList 产线集合
     * @param start      班次日期
     * @param end      班次日期
     * @return
     */
    List<PmsUseCountDTO> getDateRangeInStorage(@Param("lineList") List<Integer> lineIdList,
                                               @Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end);

    List<PmsUseCountDTO> getDateRangeUseCount(@Param("lineList") List<Integer> lineIdList,
                                              @Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end);

    /**
     * 获取生产订单的入库量
     * @param orderIdSet 订单id集合
     * @return
     */
    List<PmsUseCountDTO> getInStorageByOrder(@Param("orderIdSet") Set<Long> orderIdSet);

    /**
     * 获取生产订单的产量
     * @param orderIdSet 订单id集合
     * @return
     */
    List<PmsUseCountDTO> getPmByOrder(@Param("orderIdSet") Set<Long> orderIdSet);

    /**
     * 获取产线班次的产量
     * @param lineIdSet 产线id
     * @param start 班次日期
     * @param end 班次日期
     * @return
     */
    List<PmsUseCountDTO> getPmByLineClass(@Param("lineList") Set<Integer> lineIdSet, @Param("start") LocalDate start, @Param("end") LocalDate end);

    /**
     * 获取产线班次的入库量
     * @param lineIdSet 产线id
     * @param start 开始时间
     * @param end 结束时间
     * @return
     */
    List<PmsUseCountDTO> getInStorageByLineClass(@Param("lineList") Set<Integer> lineIdSet,
                                                 @Param("start") LocalDateTime start,
                                                 @Param("end") LocalDateTime end);

    /**
     * 获取产线班次的入库量
     * @param lineIdSet 产线id
     * @param start 开始时间
     * @param end 结束时间
     * @return
     */
    List<PmsUseCountDTO> getUserCountByLineClass(@Param("lineList") Set<Integer> lineIdSet,
                                                 @Param("start") LocalDateTime start,
                                                 @Param("end") LocalDateTime end);
    /**
     * 工序报工展示
     *
     * @param dto  条件
     * @return vo
     */
    List<ProcessReportWorkPageVO> processReportDashBoard(@Param("dto") ProcessReportWorkPageDTO dto);


    /**
     * 获取班次日期产量
     * *
     * @param lineIdList 产线id
     * @param start 开始时间
     * @param end   结束时间
     * @return
     */
    List<PmsUseCountDTO> getDatePmUnit(@Param("lineList") List<Integer> lineIdList,
                                       @Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);

    /**
     * 获取班次日期入库数量
     * *
     * @param lineIdList 产线id
     * @param start 开始时间
     * @param end   结束时间
     * @return
     */
    List<PmsUseCountDTO> getDateInStorageUnit(@Param("lineList") List<Integer> lineIdList,
                                              @Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end);


    /**
     * 根据产线id，班次日期获取停机时间
     * @param dto
     * @return
     */
    List<ProcessReportWorkHourVO> getProcessWorkHour(@Param("dto") ProcessReportWorkHourQmsDTO dto);


    /**
     * 获取产线+班次日期+班次的产量
     *
     * @param lineIdList 产线id
     * @param start      开始时间
     * @param end        结束时间
     * @return
     */
    List<PmsUseCountDTO> dateClassLinePmUnit(@Param("lineList") List<Integer> lineIdList,
                                             @Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);

    /**
     * 获取产线+班次日期的产量
     *
     * @param lineIdList 产线id
     * @param start      开始时间
     * @param end        结束时间
     * @return
     */
    List<PmsUseCountDTO> dateLinePmUnit(@Param("lineList") List<Integer> lineIdList,
                                        @Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end);

    /**
     * 获取产线+班次日期+班次的入库量
     *
     * @param lineIdList 产线id
     * @param start      开始时间
     * @param end        结束时间
     * @return
     */
    List<PmsUseCountDTO> dateClassLineInStorageUnit(@Param("lineList") List<Integer> lineIdList,
                                                    @Param("start") LocalDateTime start,
                                                    @Param("end") LocalDateTime end);

    /**
     * 获取产线+班次日期的入库量
     *
     * @param lineIdList 产线id
     * @param start      开始时间
     * @param end        结束时间
     * @return
     */
    List<PmsUseCountDTO> dateLineInStorageUnit(@Param("lineList") List<Integer> lineIdList,
                                                    @Param("start") LocalDateTime start,
                                                    @Param("end") LocalDateTime end);


    /**
     * 获取入库数合计
     * @param start
     * @param end
     * @return
     */
    List<PmsStorageCountDTO> getStorageCount(@Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);

    /**
     * 外购不良日报表
     * @param productLineList 产线集合
     * @param start 开始时间
     * @param end 结束时间
     * @return
     */
    List<OutSourcingListDTO> outsourcingClass(@Param("lineList") List<Integer> productLineList,
                                              @Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end);
}




