package com.steccn.pms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.pms.domain.PmsProduceOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.*;
import com.steccn.pms.vo.*;

/**
* @author yequan
* @description 针对表【pms_produce_order(生产订单)】的数据库操作Service
* @createDate 2022-09-28 15:18:51
*/
public interface PmsProduceOrderService extends IService<PmsProduceOrder> {

    /**
     * 新增接口
     * @param dto
     * @return
     */
    Boolean add(PmsProduceOrderDTO dto);

    /**
     * 获取分页列表
     * @param dto
     * @return
     */
    Page<PmsProduceOrderVO> pageList(PmsProduceOrderPageDTO dto);

    /**
     * 编辑
     * @param dto
     * @return
     */
    Boolean edit(PmsProduceOrderDTO dto);

    /**
     * 根据订单id获取入库信息
     * @param produceOrderId
     * @return
     */
    Page<PmsCompleteStorageVO> completeStoragePageList(PmsCompleteStorageDTO produceOrderId);

    /**
     * 根据订单id获取出库信息
     * @param dto
     * @return
     */
    Page<PmsCompleteDeliveryVO> completeDeliveryPageList(PmsCompleteDeliveryDTO dto);

    /**
     * 根据工序获取进站的信息
     * @param dto
     * @return
     */
    Page<PmsProcessInStationVO> inStationPageList(PmsProcessInStationDTO dto);

    /**
     * 根据订单获取批次分页列表
     * @param dto
     * @return
     */
    Page<PmsWorkOrderBatchVO> batchPageList(PmsProduceOrderBatchDTO dto);

    /**
     * 根据工序获取出站的信息
     * @param dto
     * @return
     */
    Page<PmsProcessOutStationVO> outStationPageList(PmsProcessOutStationDTO dto);
}
