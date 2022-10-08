package com.steccn.pms.service;

import com.steccn.pms.domain.PmsProduceOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.PmsProduceOrderDTO;

/**
* @author yequan
* @description 针对表【pms_produce_order(生产订单)】的数据库操作Service
* @createDate 2022-09-28 15:18:51
*/
public interface PmsProduceOrderService extends IService<PmsProduceOrder> {

    Boolean add(PmsProduceOrderDTO dto);
}
