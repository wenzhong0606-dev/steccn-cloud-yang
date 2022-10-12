package com.steccn.pms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.pms.domain.PmsCompleteDelivery;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.PmsCompleteDeliveryDTO;
import com.steccn.pms.vo.PmsCompleteDeliveryVO;

/**
 * @author yequan
 * @description 针对表【pms_complete_delivery(生产订单-出库)】的数据库操作Service
 * @createDate 2022-09-28 15:18:51
 */
public interface PmsCompleteDeliveryService extends IService<PmsCompleteDelivery> {

    /**
     * 查询投料信息
     *
     * @param dto
     * @return
     */
    Page<PmsCompleteDeliveryVO> pageList(PmsCompleteDeliveryDTO dto);

    /**
     * 获取订单投料分页列表
     *
     * @param dto
     * @return
     */
    Page<PmsCompleteDeliveryVO> completeDeliveryPageList(PmsCompleteDeliveryDTO dto);


    /**
     * 工艺信息-投料信息
     *
     * @param dto
     * @return
     */
    Page<PmsCompleteDeliveryVO> feedPageList(PmsCompleteDeliveryDTO dto);
}
