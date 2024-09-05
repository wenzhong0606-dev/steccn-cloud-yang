package com.steccn.pms.service;

import com.steccn.pms.domain.PmsMonthlyProductionPlanCraftStation;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.PmsMonthlyProductionPlanCraftStationDTO;

import java.util.List;

/**
* @author 34872
* @description 针对表【pms_monthly_production_plan_craft_station(生产订单-工艺信息-工位信息)】的数据库操作Service
* @createDate 2024-08-28 14:14:47
*/
public interface PmsMonthlyProductionPlanCraftStationService extends IService<PmsMonthlyProductionPlanCraftStation> {

    Boolean saveCrafts(List<PmsMonthlyProductionPlanCraftStationDTO> stationList, Long id);
}
