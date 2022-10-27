package com.steccn.pms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.BaseSelectVO;
import com.steccn.pms.domain.PmsProduceOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.*;
import com.steccn.pms.vo.*;

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
     * 编辑
     *
     * @param dto
     * @return
     */
    Boolean edit(PmsProduceOrderDTO dto);

    /**
     * 根据订单获取批次分页列表
     *
     * @param dto
     * @return
     */
    Page<PmsWorkOrderBatchVO> batchPageList(PmsProduceOrderBatchDTO dto);

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
}
