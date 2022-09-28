package com.steccn.pms.mapper;

import com.steccn.pms.domain.PmsWorkOrderCraftStation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author yequan
* @description 针对表【pms_work_order_craft_station(生产订单-工艺信息-工位信息)】的数据库操作Mapper
* @createDate 2022-09-28 15:18:51
* @Entity com.steccn.pms.domain.PmsWorkOrderCraftStation
*/
@Mapper
public interface PmsWorkOrderCraftStationMapper extends BaseMapper<PmsWorkOrderCraftStation> {

}




