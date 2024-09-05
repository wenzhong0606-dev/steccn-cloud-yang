package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.pms.domain.PmsMonthlyProductionPlanBom;
import com.steccn.pms.domain.PmsMonthlyProductionPlanCraft;
import com.steccn.pms.domain.PmsProduceOrderCraft;
import com.steccn.pms.dto.PmsMonthlyProductionPlanCraftDTO;
import com.steccn.pms.dto.PmsMonthlyProductionPlanCraftStationDTO;
import com.steccn.pms.dto.PmsMonthlyProductionPlanDTO;
import com.steccn.pms.service.PmsMonthlyProductionPlanCraftService;
import com.steccn.pms.mapper.PmsMonthlyProductionPlanCraftMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
* @author 34872
* @description 针对表【pms_monthly_production_plan_craft(生产订单-工艺信息)】的数据库操作Service实现
* @createDate 2024-08-28 15:50:02
*/
@Service
public class PmsMonthlyProductionPlanCraftServiceImpl extends ServiceImpl<PmsMonthlyProductionPlanCraftMapper, PmsMonthlyProductionPlanCraft>
    implements PmsMonthlyProductionPlanCraftService{


    @Override
    public Boolean saveCrafts(List<PmsMonthlyProductionPlanCraftDTO> stationList, Long id) {

        //1.定义数据集合
        List<PmsMonthlyProductionPlanCraft> craftList = Lists.newArrayList();
        //2.处理数据
        stationList.forEach(material -> {
            PmsMonthlyProductionPlanCraft pmsMonthlyProductionPlanCraft = new PmsMonthlyProductionPlanCraft();
            BeanUtil.copyProperties(material, pmsMonthlyProductionPlanCraft);
            pmsMonthlyProductionPlanCraft.setMonthlyProductionPlanId(id);
            craftList.add(pmsMonthlyProductionPlanCraft);
        });
        //3.批量保存
        return saveBatch(craftList);
    }

    @Override
    public Boolean deleteCrafts(Set<Long> ids, Long id) {
        if (CollUtil.isNotEmpty(ids) && Objects.nonNull(id)) {
            return remove(new LambdaQueryWrapper<PmsMonthlyProductionPlanCraft>().eq(PmsMonthlyProductionPlanCraft::getMonthlyProductionPlanId, id).notIn(PmsMonthlyProductionPlanCraft::getId, ids));
        }
        return Boolean.TRUE;
    }
}




