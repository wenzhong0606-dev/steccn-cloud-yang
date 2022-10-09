package com.steccn.pms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.pms.domain.PmsProduceOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.PmsProduceOrderDTO;
import com.steccn.pms.dto.PmsProduceOrderPageDTO;
import com.steccn.pms.vo.PmsProduceOrderVO;

/**
* @author yequan
* @description 针对表【pms_produce_order(生产订单)】的数据库操作Service
* @createDate 2022-09-28 15:18:51
*/
public interface PmsProduceOrderService extends IService<PmsProduceOrder> {

    Boolean add(PmsProduceOrderDTO dto);

    Page<PmsProduceOrderVO> pageList(PmsProduceOrderPageDTO dto);

    Boolean edit(PmsProduceOrderDTO dto);
}
