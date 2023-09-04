package com.steccn.mes.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.common.constant.MpConstant;
import com.steccn.mes.domain.MesEqpServicePlan;
import com.steccn.mes.domain.MesEquipment;
import com.steccn.mes.service.MesEqpServicePlanService;
import com.steccn.mes.service.MesEquipmentService;
import com.steccn.mes.mapper.MesEquipmentMapper;
import com.steccn.mes.vo.MesEqpPartVO;
import com.steccn.mes.vo.MesEqpVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author Admin
* @description 针对表【mes_equipment(设备表
)】的数据库操作Service实现
* @createDate 2023-09-01 16:29:57
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class MesEquipmentServiceImpl extends ServiceImpl<MesEquipmentMapper, MesEquipment>
    implements MesEquipmentService{
    private final MesEqpServicePlanService mesEqpServicePlanService;

    @Override
    public List<MesEqpVo> getEqpInfo() {
        //获取所有检修作业计划
        List<MesEqpServicePlan> servicePlan = mesEqpServicePlanService.list(new LambdaQueryWrapper<MesEqpServicePlan>()
                .select(MesEqpServicePlan::getEqpId, MesEqpServicePlan::getPart, MesEqpServicePlan::getMaintainMethod));
        List<MesEqpVo> results = Lists.newArrayList();
        List<MesEquipment> mesEquipments = list(new LambdaQueryWrapper<MesEquipment>()
                .select(MesEquipment::getId, MesEquipment::getEquipmentCode, MesEquipment::getEquipmentName, MesEquipment::getRemarks, MesEquipment::getModel)
                .eq(MesEquipment::getStatus, MpConstant.NO.toString())
                .eq(MesEquipment::getIsEqp, MpConstant.NO.toString()));
        mesEquipments.forEach(tmp ->{
            MesEqpVo vo = new MesEqpVo();
            BeanUtil.copyProperties(tmp,vo);
            List<MesEqpServicePlan> collect = servicePlan.stream().filter(v -> v.getEqpId().equals(tmp.getId().toString())).collect(Collectors.toList());
            if(CollUtil.isNotEmpty(collect)){
                List<MesEqpPartVO> part = Lists.newArrayList();
                collect.forEach(collectTmp->{
                    MesEqpPartVO partVO = new MesEqpPartVO();
                    BeanUtil.copyProperties(collectTmp,partVO);
                    part.add(partVO);
                });
                vo.setParts(part);
            }
            results.add(vo);
        });
        return results;
    }
}




