package com.steccn.pms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.pms.domain.PmsMonthlyProductionPlanCraftStation;
import com.steccn.pms.dto.PmsMonthlyProductionPlanCraftStationDTO;
import com.steccn.pms.service.PmsMonthlyProductionPlanCraftStationService;
import com.steccn.pms.mapper.PmsMonthlyProductionPlanCraftStationMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author 34872
* @description 针对表【pms_monthly_production_plan_craft_station(生产订单-工艺信息-工位信息)】的数据库操作Service实现
* @createDate 2024-08-28 14:14:47
*/
@Service
public class PmsMonthlyProductionPlanCraftStationServiceImpl extends ServiceImpl<PmsMonthlyProductionPlanCraftStationMapper, PmsMonthlyProductionPlanCraftStation>
    implements PmsMonthlyProductionPlanCraftStationService{

    @Override
    public Boolean saveCrafts(List<PmsMonthlyProductionPlanCraftStationDTO> stationList, Long id) {


        return null;
    }
}




