package com.steccn.mes.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.mes.domain.MesEqpServicePlan;
import com.steccn.mes.domain.MesEquipment;
import com.steccn.mes.service.MesEqpServicePlanService;
import com.steccn.mes.service.MesEquipmentService;
import com.steccn.mes.mapper.MesEquipmentMapper;
import com.steccn.mes.vo.MesEqpPartVO;
import com.steccn.mes.vo.MesEqpVO;
import com.steccn.oa.constant.OaConstant;
import com.steccn.platform.feign.RemoteSysConfigurationFeignApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
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
    private final RemoteSysConfigurationFeignApi remoteSysConfigurationFeignApi;

    @Override
    public List<MesEqpVO> getEqpInfo() {
        //获取所有检修作业计划
        List<MesEqpServicePlan> servicePlan = mesEqpServicePlanService.list(new LambdaQueryWrapper<MesEqpServicePlan>()
                .select(MesEqpServicePlan::getEqpId, MesEqpServicePlan::getPart, MesEqpServicePlan::getMaintainMethod));
        List<MesEqpVO> results = Lists.newArrayList();
        List<MesEquipment> mesEquipments = list(new LambdaQueryWrapper<MesEquipment>()
                .select(MesEquipment::getId, MesEquipment::getEquipmentCode, MesEquipment::getEquipmentName, MesEquipment::getRemarks, MesEquipment::getModel)
                .eq(MesEquipment::getStatus, MpConstant.NO.toString())
                .eq(MesEquipment::getIsEqp, MpConstant.NO.toString()));
        mesEquipments.forEach(tmp ->{
            MesEqpVO vo = new MesEqpVO();
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

    @Override
    public MesEqpVO getEqpByCode(String eqpCode) {
        MesEquipment one = getOne(new LambdaQueryWrapper<MesEquipment>()
                .eq(MesEquipment::getEquipmentCode, eqpCode)
                .eq(MesEquipment::getStatus, MpConstant.NO.toString())
                .last("limit 1"));
        MesEqpVO vo = new MesEqpVO();
        BeanUtil.copyProperties(one,vo);
        return vo;
    }

    @Override
    public Boolean changeEqpState(TupleDTO<String, String> dto) {
        MesEquipment equipment = new MesEquipment();
        //找到设备code信号
        Set<String> signalCode = FeignOptional.of(() -> remoteSysConfigurationFeignApi.contentByType("EQP_SIGNAL"), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //修改设备启停
        equipment.setState(dto.getField2());
        //需要修改的id
        Set<Long> idSet = Sets.newHashSet();
        //获取设备自身信息和上级信息
        MesEquipment eqp = getOne(new LambdaQueryWrapper<MesEquipment>()
                .eq(MesEquipment::getEquipmentCode, dto.getField1())
                .eq(MesEquipment::getStatus, OaConstant.NO.toString())
                .eq(MesEquipment::getIsEqp, OaConstant.NO.toString())
                .last("limit 1"));
        if(Objects.isNull(eqp)){
            log.error(dto.getField1()+"设备不存在");
        }
        MesEquipment parent = getOne(new LambdaQueryWrapper<MesEquipment>()
                .eq(MesEquipment::getEquipmentCode, eqp.getEquipmentCode())
                .eq(MesEquipment::getStatus, OaConstant.NO.toString())
                .eq(MesEquipment::getIsEqp, OaConstant.NO.toString())
                .last("limit 1"));
        //设备停止时，1)下级设备全部停止 2)同级是否是否都停止，若都停止则上级也停止
        if(equipment.getState().equals(OaConstant.NUM_1.toString())){
            //1)下级设备全部停止
            List<MesEquipment> childrenList = list(new LambdaQueryWrapper<MesEquipment>()
                    .eq(MesEquipment::getParentCode, dto.getField1())
                    .eq(MesEquipment::getStatus, OaConstant.NO.toString())
                    .eq(MesEquipment::getIsEqp, OaConstant.NO.toString()));
            if(CollUtil.isNotEmpty(childrenList)){
                //仅对设备信号外的设备有效
                childrenList.forEach(tmp->{
                    if(!signalCode.contains(tmp.getEquipmentCode())){
                        idSet.add(tmp.getId());
                    }
                });
            }
            //2)同级是否是否都停止，若都停止则上级也停止
            List<MesEquipment> platList = list(new LambdaQueryWrapper<MesEquipment>()
                    .eq(MesEquipment::getParentCode, eqp.getParentCode())
                    .eq(MesEquipment::getStatus, OaConstant.NO.toString())
                    .eq(MesEquipment::getIsEqp, OaConstant.NO.toString()));
            if(CollUtil.isNotEmpty(platList)){
                //判断同级设备是否都停止
                int size = platList.size();
                platList = platList.stream().filter(v->v.getState().equals(OaConstant.NUM_1.toString())).collect(Collectors.toList());
                if(size == platList.size()&&!signalCode.contains(parent.getEquipmentCode())){
                    idSet.add(parent.getId());
                }
            }

        }
        return update(equipment,new LambdaQueryWrapper<MesEquipment>()
                .in(MesEquipment::getId,idSet)
                .eq(MesEquipment::getStatus, OaConstant.NO.toString()));
    }
}




