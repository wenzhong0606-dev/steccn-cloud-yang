package com.steccn.pms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.pms.domain.PmsProduceOrder;
import com.steccn.pms.dto.PanoramicTracingDTO;
import com.steccn.pms.dto.PmsProduceOrderPageDTO;
import com.steccn.pms.dto.ProduceOrderAcceptPageDTO;
import com.steccn.pms.dto.ReportWorkOrderPageDTO;
import com.steccn.pms.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yequan
 * @description 针对表【pms_produce_order(生产订单)】的数据库操作Mapper
 * @createDate 2022-09-28 15:18:51
 * @Entity com.steccn.pms.domain.PmsProduceOrder
 */
@Mapper
public interface PmsProduceOrderMapper extends BaseMapper<PmsProduceOrder> {

    /**
     * 根据条件筛选订单信息
     * @param page
     * @param dto
     * @return
     */
    Page<PmsProduceOrderVO> pageList(Page page, @Param("dto") PmsProduceOrderPageDTO dto);

    /**
     * 获取订单接收数据
     *
     * @param page
     * @param dto
     * @return
     */
    Page<PmsProduceOrderVO> acceptPage(Page page, @Param("dto") ProduceOrderAcceptPageDTO dto);

    /**
     * 订单报工-根据条件筛选订单信息
     * @param page 分页信息
     * @param dto dto
     * @return 报工信息
     */
    Page<PmsProduceOrderVO> reportWorkOrderPage(Page page, @Param("dto") ReportWorkOrderPageDTO dto);

    /**
     * 订单跟踪报表
     *
     * @param page
     * @param productionLineIdList
     * @return
     */
    Page<ProduceOrderTrackReportVO> trackReportPage(Page page, @Param("productionLineIdList") List<Integer> productionLineIdList);

    /**
     * 根据产线id查询入库数
     *
     * @param productionLineIdList
     * @return
     */
    List<CompleteStorageCountVO> selectCompleteStorageCount(@Param("productionLineIdList") List<Integer> productionLineIdList
            , @Param("startTime")LocalDateTime startTime
            , @Param("endTime")LocalDateTime endTime
            , @Param("group") Integer group);

    /**
     * 根据班次时间查询数据
     *
     * @param startTime
     * @param endTime
     * @return
     */
    WorkOrderVO selectByClassTime(@Param("startTime")LocalDateTime startTime,@Param("endTime")LocalDateTime endTime);

    /**
     * 全景追诉
     * @param dto
     * @return
     */
    List<PanoramicTracingVO>  tracingCustomerBarcode(@Param("dto") PanoramicTracingDTO dto);


    /**
     * 全景追诉
     * @param dto
     * @return
     */
    List<PanoramicTracingVO>  tracing(@Param("dto") PanoramicTracingDTO dto);

    /**
     * 获取返洗不良的生产批次
     *
     * @param productLineList 产线集合
     * @param start           开始时间
     * @param end             结束时间
     * @return
     */
    List<RepaireBatchListVO> repaireBatchList(@Param("lineList") List<Integer> productLineList,
                                              @Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end,
                                              @Param("typeId") Long typeId);

    /**
     * 获取生产订单生产的物料所使用的主物料
     * @param start
     * @param end
     * @return
     */
    List<Long> listOrderMaterialIdByDate(@Param("start")LocalDate start,
                                         @Param("end") LocalDate end);
}




