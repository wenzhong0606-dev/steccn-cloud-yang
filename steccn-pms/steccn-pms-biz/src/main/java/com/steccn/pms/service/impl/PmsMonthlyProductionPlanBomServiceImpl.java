package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.pms.domain.PmsMonthlyProductionPlan;
import com.steccn.pms.domain.PmsMonthlyProductionPlanBom;
import com.steccn.pms.domain.PmsMonthlyProductionPlanCraft;
import com.steccn.pms.domain.PmsWorkOrderBom;
import com.steccn.pms.dto.PmsMonthlyProductionPlanBomDTO;
import com.steccn.pms.dto.PmsMonthlyProductionPlanDTO;
import com.steccn.pms.service.PmsMonthlyProductionPlanBomService;
import com.steccn.pms.mapper.PmsMonthlyProductionPlanBomMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
* @author 34872
* @description 针对表【pms_monthly_production_plan_bom(生产工单-物料清单信息)】的数据库操作Service实现
* @createDate 2024-08-28 14:14:47
*/
@Service
public class PmsMonthlyProductionPlanBomServiceImpl extends ServiceImpl<PmsMonthlyProductionPlanBomMapper, PmsMonthlyProductionPlanBom>
    implements PmsMonthlyProductionPlanBomService{

    @Override
    public Boolean saveBom(List<PmsMonthlyProductionPlanBomDTO> materialList, Long id) {
        //1.定义数据集合
        List<PmsMonthlyProductionPlanBom> bomList = Lists.newArrayList();
        //2.处理数据
        materialList.forEach(material -> {
            PmsMonthlyProductionPlanBom pmsMonthlyProductionPlanBom = new PmsMonthlyProductionPlanBom();
            BeanUtil.copyProperties(material, pmsMonthlyProductionPlanBom);
            pmsMonthlyProductionPlanBom.setMonthlyProductionPlanId(id);
            bomList.add(pmsMonthlyProductionPlanBom);
        });
        //3.批量保存
       return saveBatch(bomList);

    }

    @Override
    public Boolean deleteBom(Set<Long> idSet, Long id) {
        if (CollUtil.isNotEmpty(idSet) && Objects.nonNull(id)) {
            return remove(new LambdaQueryWrapper<PmsMonthlyProductionPlanBom>().eq(PmsMonthlyProductionPlanBom::getMonthlyProductionPlanId, id).notIn(PmsMonthlyProductionPlanBom::getId, idSet));
        }
        return Boolean.TRUE;
    }
}




