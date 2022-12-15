package com.steccn.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.wms.domain.WmsLogisticsOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.wms.dto.WmsLogisticsOrderDTO;
import com.steccn.wms.dto.WmsLogisticsOrderPageDTO;
import com.steccn.wms.vo.WmsLogisticsOrderVO;

/**
* @author W10
* @description 针对表【wms_logistics_order(物流单)】的数据库操作Service
* @createDate 2022-11-30 09:45:17
*/
public interface WmsLogisticsOrderService extends IService<WmsLogisticsOrder> {

    /**
     * 物流单分页列表
     * @param dto
     * @return
     */
    Page<WmsLogisticsOrderVO> pageList(WmsLogisticsOrderPageDTO dto);

    /**
     * 物流单新增
     * @param wmsLogisticsOrderDTO
     * @return
     */
    Boolean add(WmsLogisticsOrderDTO wmsLogisticsOrderDTO);

    /**
     * 物流单详情
     * @param id
     * @return
     */
    WmsLogisticsOrderVO get(Long id);
}
