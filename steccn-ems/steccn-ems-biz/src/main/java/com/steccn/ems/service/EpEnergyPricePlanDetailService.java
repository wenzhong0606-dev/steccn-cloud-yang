package com.steccn.ems.service;

import com.steccn.ems.domain.EpEnergyPricePlanDetail;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
* @author yequan
* @description 针对表【ep_energy_price_plan_detail(能源价格方案明细)】的数据库操作Service
* @createDate 2022-11-29 19:27:28
*/
public interface EpEnergyPricePlanDetailService extends IService<EpEnergyPricePlanDetail> {

    /**
     * 根据能源价格方案id查询方案明细表,返回48个元素为1、2、3类型的集合
     * @param planIds
     * @return
     */
    Map<Long, List<Integer>> getEpEnergyPricePlanDetailByPlanId (Set<Long> planIds);
}
