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

}
