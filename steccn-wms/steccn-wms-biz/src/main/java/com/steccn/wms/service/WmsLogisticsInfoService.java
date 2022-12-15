package com.steccn.wms.service;

import com.steccn.wms.domain.WmsLogisticsInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.wms.vo.WmsLogisticsInfoVO;

import java.util.List;
import java.util.Map;

/**
* @author W10
* @description 针对表【wms_logistics_info(收发货信息)】的数据库操作Service
* @createDate 2022-11-30 09:45:17
*/
public interface WmsLogisticsInfoService extends IService<WmsLogisticsInfo> {

    /**
     * 获取物流方收发货机构信息
     * @param orderIds
     * @return
     */
    Map<Long, List<WmsLogisticsInfoVO>> mapByLogisticsId(List<Long> orderIds);
}
