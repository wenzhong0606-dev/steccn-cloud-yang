package com.steccn.pms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.common.constant.ResultCode;
import com.steccn.common.vo.BaseSelectVO;
import com.steccn.pms.domain.PmsProduceOrder;
import com.steccn.pms.dto.*;
import com.steccn.pms.vo.*;
import com.steccn.qms.dto.QtDefectReportDTO;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【pms_produce_order(生产订单)】的数据库操作Service
 * @createDate 2022-09-28 15:18:51
 */
public interface PmsProduceOrderService extends IService<PmsProduceOrder> {

    /**
     * 新增接口
     *
     * @param dto
     * @return
     */
    Boolean add(PmsProduceOrderDTO dto);

    /**
     * 获取分页列表
     *
     * @param dto
     * @return
     */
    Page<PmsProduceOrderVO> pageList(PmsProduceOrderPageDTO dto);

    /**
     * 获取生产订单详情
     * @param id
     * @return
     */
    PmsProduceOrderVO info(Long id);

    /**
     * 编辑
     *
     * @param dto
     * @return
     */
    Boolean edit(PmsProduceOrderDTO dto);

    /**
     * 编辑计划时间
     * @param dto
     * @return
     */
    Boolean editPlanTime(PmsProduceOrderDTO dto);

    /**
     * 根据订单获取批次分页列表
     *
     * @param dto
     * @return
     */
    Page<PmsWorkOrderBatchVO> batchPageList(PmsProduceOrderBatchDTO dto);

    /**
     * 生产订单甘特图
     * @param dto
     * @return
     */
    PmsProduceOrderGanttChartVO produceOrderGanttChart(PmsProduceOrderGanttChartDTO dto);

    /**
     * 报工订单批次详细信息
     *
     * @param productLineId 产线id
     * @param stationId     工位id
     * @return
     */
    ReportWorkOrderVO reportWorkOrder(Integer productLineId, Integer stationId);

    /**
     * 报工订单批次详细信息
     *
     * @param orderId   订单id
     * @param stationId 工位id
     * @return
     */
    ReportWorkOrderVO reportWorkOrderId(Long orderId, Integer stationId);

    /**
     * 订单接收分页列表
     *
     * @param dto
     * @return
     */
    Page<PmsProduceOrderVO> acceptPage(ProduceOrderAcceptPageDTO dto);

    /**
     * 订单下达
     *
     * @param orderId
     * @return
     */
    Boolean placeAnOrder(Long orderId);

    /**
     * 工序报工-订单列表
     *
     * @param dto dto
     * @return 分页数据
     */
    Page<PmsProduceOrderVO> reportWorkOrderPage(ReportWorkOrderPageDTO dto);

    /**
     * 订单关闭
     *
     * @param orderId
     * @return
     */
    Boolean close(Long orderId);

    /**
     * 取消订单下达
     *
     * @param orderId
     * @return
     */
    Boolean cancelTheOrder(Long orderId);

    /**
     * 订单确认
     *
     * @param orderId
     * @return
     */
    Boolean affirm(Long orderId);

    /**
     * 订单执行
     *
     * @param orderId
     * @return
     */
    Boolean execute(Long orderId);

    /**
     * 根据产线id查询订单当前正在执行订单
     *
     * @param produceLineId
     * @return
     */
    PmsProduceOrderVO listByProduceLineId(Integer produceLineId);

    /**
     * 订单暂停
     * @param orderId
     * @return
     */
    Boolean stop(Long orderId);

    /**
     * 订单完成
     * @param orderId
     * @return
     */
    Boolean complete(Long orderId);

    /**
     * 订单的单位
     * @param orderSet 订单集合
     * @return key 订单id value 订单的单位
     */
    Map<Long, BaseSelectVO<Long>> listUnit(Set<Long> orderSet);

    /**
     * 根据产线id获取正在执行中的订单
     * @param ids
     * @return
     */
    Map<Integer,PmsProduceOrderDTO> listByLineIds(Set<Integer> ids);

    /**
     * 订单跟踪报表列表
     *
     * @param dto
     * @return
     */
    Page<ProduceOrderTrackReportVO> trackReportList(ProduceOrderTrackReportDTO dto);

    /**
     * 根据生产订单号查询正在执行中的产线,批次,车间信息
     * @param code
     * @return
     */
    PmsProduceOrderVO code(String code);

    /**
     * 根据生产订单号查询产线,批次,车间信息
     * @param code
     * @return
     */
    PmsProduceOrderVO allCode(String code);

    /**
     * 根据产线获取下达以后完成之前的数据
     * @param ids
     * @return
     */
    List<PmsProduceOrderVO>  executionOfOrder(Set<Integer> ids);

    /**
     * 获取订单的信息
     * @param orderIds
     * @return
     */
    Map<Long, PmsProduceOrder> orderList(List<Long> orderIds);

    /**
     * 根据物料查询订单信息
     * @param materialIds
     * @return
     */
    Map<Long, PmsProduceOrderVO> listByMaterialIds(Set<Long> materialIds);

    /**
     * 根据班次时间段获取物料id
     *
     * @param startTime
     * @param endTime
     * @return
     */
    WorkOrderVO getMaterialCodeByClassTime(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询当前班次日期的所有报工记录
     * @param currentDate 当前班次日期
     * @return list
     */
    List<DwCurrentReportWorkDTO> currentDateReportWork(LocalDate currentDate);

    /**
     * 获取当前日期下所有的订单
     * @param currentDate 当前班次日期
     * @return 订单集合
     */
    List<DwProduceOrderDTO> currentProduceOrder(LocalDate currentDate);

    /**
     * 获取16条产线下的生产订单的详情数据
     * @return
     */
    List<ProduceOrderDataInfoVO> getExecuteOrderInfo();

    /**
     * 全景追溯数据
     * @param panoramicTracingDTO
     * @return
     */
    Page<PanoramicTracingVO> tracing(PanoramicTracingDTO panoramicTracingDTO);

    /**
     * 获取客户
     * @param dto
     * @return
     */
    CustomerIdAndNameVO getCustomer(QmsCustomerDTO dto);


    /**
     * 根据生产订单id 获取生产订单bom中的主物料
     * @param orderIds
     * @return key 生产订单id  value 物料id
     */
    Map<Long,Long> maiMaterialAByOrderId(Set<Long> orderIds);

    List<PmsProduceOrderVO> listOrderByProduceLineId(Integer productLineId);

    /**
     * 返洗不良日报表
     * @param dto 请求类
     * @return
     */
    RepaireBatchVO repaireBatchList(RepaireBatchListDTO dto);

    /**
     * 返洗不良日报表
     * @param dto 请求类
     * @return
     */
    void repaireBatchListDownload(RepaireBatchListDTO dto, HttpServletResponse response) throws IOException;

    /**
     * 外购原片不良日报表
     * @param dto dto
     * @return
     */
    OutsourcingDayReportVO outsourcing(DayReportCommonDTO dto);

    /**
     * 其余能源类型的产量
     *
     * @param start        开始日期
     * @param end          结束日期
     * @param officeIdList officeId集合
     * @return
     */
    Map<String, BigDecimal> orderDetailOtherEnergy(LocalDate start, LocalDate end, List<Integer> officeIdList);

    /**
     * 其余能源类型的订单
     *
     * @param start        开始日期
     * @param end          结束日期
     * @param officeIdList officeId集合
     * @return
     */
    Map<String, BigDecimal> orderDetailOtherEnergyPf(LocalDate start, LocalDate end, List<Integer> officeIdList);
}
