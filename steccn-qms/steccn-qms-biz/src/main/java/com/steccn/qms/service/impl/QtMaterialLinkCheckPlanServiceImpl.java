package com.steccn.qms.service.impl;



import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.qms.domain.QtMaterialLinkCheckPlan;
import com.steccn.qms.dto.QtMaterialLinkCheckPlanDTO;
import com.steccn.qms.mapper.QtMaterialLinkCheckPlanMapper;
import com.steccn.qms.service.QtMaterialLinkCheckPlanService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;


/**
* @author yequan
* @description 针对表【qt_material_link_check_plan(物料关联检验计划表)】的数据库操作Service实现
* @createDate 2022-09-02 08:54:09
*/
@Service
@RequiredArgsConstructor
public class QtMaterialLinkCheckPlanServiceImpl extends ServiceImpl<QtMaterialLinkCheckPlanMapper, QtMaterialLinkCheckPlan>
    implements QtMaterialLinkCheckPlanService {

//    private final QtCheckPlanService qtCheckPlanService;

    @Override
    public Boolean add(QtMaterialLinkCheckPlanDTO dto) {
        //1、先删除之前的配置数据
        Map<String, Object> materialConlumn = new HashMap<>();
        materialConlumn.put("material_id",dto.getMaterialId());
        removeByMap(materialConlumn);
        //2、进行新增
        List<QtMaterialLinkCheckPlan> qtMaterialLinkCheckPlanList = Lists.newArrayList();
        if(dto.getType()!=null && dto.getType().size() > 0){
            dto.getType().forEach(typeTmp->{
                QtMaterialLinkCheckPlan qtMaterialLinkCheckPlan = new QtMaterialLinkCheckPlan();
                qtMaterialLinkCheckPlan.setMaterialId(Long.parseLong(dto.getMaterialId()));
                qtMaterialLinkCheckPlan.setCheckPlanId(typeTmp.getCheckPlanId());
//                qtMaterialLinkCheckPlan.setCheckType(Long.parseLong(typeTmp.getCheckType()));
                qtMaterialLinkCheckPlan.setUpdateBy(SecurityUtils.getUser().getUserId());
                qtMaterialLinkCheckPlan.setUpdateName(SecurityUtils.getUser().getName());
                qtMaterialLinkCheckPlan.setCreateBy(SecurityUtils.getUser().getUserId());
                qtMaterialLinkCheckPlan.setCreateName(SecurityUtils.getUser().getName());
                qtMaterialLinkCheckPlan.setCheckTypeCode(typeTmp.getCheckTypeCode());
                qtMaterialLinkCheckPlanList.add(qtMaterialLinkCheckPlan);
            });
        }
        if(qtMaterialLinkCheckPlanList.size() > 0){
            return saveBatch(qtMaterialLinkCheckPlanList);
        }else {
            throw new BusinessException("关联类型type值为空");
        }
    }

    @Override
    public List<Integer> getCheckType(Long id) {
        List<Integer> checkTypeCodeList = new ArrayList<>();
        if (Objects.nonNull(id)) {
            checkTypeCodeList = list(new LambdaQueryWrapper<QtMaterialLinkCheckPlan>().
                    eq(QtMaterialLinkCheckPlan::getMaterialId, id)).stream()
                    .map(QtMaterialLinkCheckPlan::getCheckTypeCode).collect(Collectors.toList());
        }
       if (CollUtil.isEmpty(checkTypeCodeList)){
           throw new BusinessException("该材料没有检验方案类型,请先配置");
       }
        return checkTypeCodeList;
    }

}




