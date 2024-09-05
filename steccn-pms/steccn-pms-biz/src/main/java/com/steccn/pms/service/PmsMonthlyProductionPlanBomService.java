package com.steccn.pms.service;

import com.steccn.pms.domain.PmsMonthlyProductionPlanBom;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.PmsMonthlyProductionPlanBomDTO;
import com.steccn.pms.dto.PmsMonthlyProductionPlanDTO;

import java.util.List;
import java.util.Set;

/**
* @author 34872
* @description 针对表【pms_monthly_production_plan_bom(生产工单-物料清单信息)】的数据库操作Service
* @createDate 2024-08-28 14:14:47
*/
public interface PmsMonthlyProductionPlanBomService extends IService<PmsMonthlyProductionPlanBom> {

    Boolean saveBom(List<PmsMonthlyProductionPlanBomDTO> materialList, Long id);

    Boolean deleteBom(Set<Long> idSet, Long id);
}
