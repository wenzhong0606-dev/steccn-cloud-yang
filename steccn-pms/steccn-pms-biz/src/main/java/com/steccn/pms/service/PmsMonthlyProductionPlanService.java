package com.steccn.pms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.constant.ResultCode;
import com.steccn.pms.domain.PmsMonthlyProductionPlan;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.PmsMonthlyProductionPlanDTO;
import com.steccn.pms.vo.PmsMonthlyProductionPlanVO;
import org.springframework.stereotype.Service;

/**
* @author 34872
* @description 针对表【pms_monthly_production_plan】的数据库操作Service
* @createDate 2024-08-26 10:51:10
*/

public interface PmsMonthlyProductionPlanService extends IService<PmsMonthlyProductionPlan> {

    Boolean add(PmsMonthlyProductionPlanDTO dto);

    Boolean edit(PmsMonthlyProductionPlanDTO dto);

    Boolean order(PmsMonthlyProductionPlanDTO dto);

    Boolean stop(Long id);

    Boolean complete(Long id);

    Page<PmsMonthlyProductionPlanVO> pageList(PmsMonthlyProductionPlanDTO dto);

    PmsMonthlyProductionPlanVO info(Long id);

    Boolean createOrder(PmsMonthlyProductionPlanDTO dto);
}
