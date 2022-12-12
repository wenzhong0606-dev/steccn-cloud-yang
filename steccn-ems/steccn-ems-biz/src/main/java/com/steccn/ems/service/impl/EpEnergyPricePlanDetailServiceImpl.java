package com.steccn.ems.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.steccn.ems.domain.EpEnergyPricePlanDetail;
import com.steccn.ems.service.EpEnergyPricePlanDetailService;
import com.steccn.ems.mapper.EpEnergyPricePlanDetailMapper;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author yequan
* @description 针对表【ep_energy_price_plan_detail(能源价格方案明细)】的数据库操作Service实现
* @createDate 2022-11-29 19:27:28
*/
@Service
public class EpEnergyPricePlanDetailServiceImpl extends ServiceImpl<EpEnergyPricePlanDetailMapper, EpEnergyPricePlanDetail>
    implements EpEnergyPricePlanDetailService{

    @Override
    public Map<Long, List<Integer>> getEpEnergyPricePlanDetailByPlanId(Set<Long> planIds) {
        List<EpEnergyPricePlanDetail> epEnergyPricePlanDetails = list(new LambdaQueryWrapper<EpEnergyPricePlanDetail>()
                .in(EpEnergyPricePlanDetail::getPlanId, planIds));
        //设置返回数据集合
        Map<Long,List<Integer>> getPricePlanUnit = Maps.newHashMap();
        if (CollUtil.isNotEmpty(epEnergyPricePlanDetails)){
            //按能源价格方案的id分组
            Map<Long, List<EpEnergyPricePlanDetail>> dataMap = epEnergyPricePlanDetails
                    .parallelStream().collect(Collectors.groupingBy(EpEnergyPricePlanDetail::getPlanId));

            for (Map.Entry<Long, List<EpEnergyPricePlanDetail>> entry : dataMap.entrySet()) {
                List<Integer> planTypeList = getPlanTypeList(entry.getValue());
                getPricePlanUnit.put(entry.getKey(),planTypeList);
            }

        }
        return getPricePlanUnit;
    }

    private List<Integer> getPlanTypeList (List<EpEnergyPricePlanDetail> energyPricePlanDetails){
        List<Integer> planTypes = new ArrayList<>();
      energyPricePlanDetails.forEach(v->{
          LocalTime start = v.getStart();
          LocalTime end = v.getEnd();
          while (start.isBefore(end)){
            planTypes.add(v.getType());
            start=start.plusMinutes(30);
          }
          if (end.equals(LocalTime.of(0,0))){
              //加上最后一次
              planTypes.add(v.getType());
          }
      });
      return planTypes;
    }
}




