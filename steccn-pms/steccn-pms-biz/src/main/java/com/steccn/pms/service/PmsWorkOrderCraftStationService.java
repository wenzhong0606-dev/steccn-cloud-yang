package com.steccn.pms.service;

import com.steccn.pms.domain.PmsWorkOrderCraftStation;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.vo.PmsWorkOrderCraftStationVO;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
* @author yequan
* @description 针对表【pms_work_order_craft_station(生产订单-工艺信息-工位信息)】的数据库操作Service
* @createDate 2022-09-28 15:18:51
*/
public interface PmsWorkOrderCraftStationService extends IService<PmsWorkOrderCraftStation> {


    /**
     * 根据工艺路线id批量查询工位信息
     * @param craftSet
     * @return
     */
    Map<Long, List<PmsWorkOrderCraftStationVO>> stations(Set<Long> craftSet);

}
