package com.steccn.wms.service;

import com.steccn.wms.domain.WmsLogisticsTransportation;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.wms.dto.WmsLogisticsOrderDTO;
import com.steccn.wms.vo.WmsLogisticsTransportationVO;

import java.util.List;

/**
* @author W10
* @description 针对表【wms_logistics_transportation(运输方式)】的数据库操作Service
* @createDate 2022-11-30 09:45:17
*/
public interface WmsLogisticsTransportationService extends IService<WmsLogisticsTransportation> {


    /**
     * 获取物流单的运输方式
     * @param id
     * @return
     */
    List<WmsLogisticsTransportationVO> listByLogisticsId(Long id);

    /**
     * 修改运输方式
     * @param dto
     * @return
     */
    Boolean edit(WmsLogisticsOrderDTO dto);
}
