package com.steccn.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.wms.domain.WmsLogisticsDetail;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.wms.dto.WmsLogisticsDetailDTO;
import com.steccn.wms.dto.WmsLogisticsOrderDTO;
import com.steccn.wms.vo.WmsLogisticsDetailVO;

import java.util.List;

/**
* @author W10
* @description 针对表【wms_logistics_detail】的数据库操作Service
* @createDate 2022-11-30 09:45:17
*/
public interface WmsLogisticsDetailService extends IService<WmsLogisticsDetail> {

    /**
     * 根据物流单id获取明细信息
     * @param id
     * @return
     */
    List<WmsLogisticsDetailVO> listByLogisticsId(Long id);


    /**
     * 根据物流单获取详情列表
     * @param dto
     * @return
     */
    Page<WmsLogisticsDetailVO> receiptInfo(WmsLogisticsDetailDTO dto);

    /**
     * 明细编辑
     * @param dto
     * @return
     */
    Boolean edit(WmsLogisticsOrderDTO dto);
}
