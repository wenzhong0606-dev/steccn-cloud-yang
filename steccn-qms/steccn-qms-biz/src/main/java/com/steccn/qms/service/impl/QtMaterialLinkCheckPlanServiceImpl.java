package com.steccn.qms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.qms.domain.QtMaterialLinkCheckPlan;
import com.steccn.qms.dto.QtMaterialLinkCheckPlanDTO;
import com.steccn.qms.service.QtMaterialLinkCheckPlanService;
import com.steccn.qms.mapper.QtMaterialLinkCheckPlanMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* @author yequan
* @description 针对表【qt_material_link_check_plan(物料关联检验计划表)】的数据库操作Service实现
* @createDate 2022-09-02 08:54:09
*/
@Service
public class QtMaterialLinkCheckPlanServiceImpl extends ServiceImpl<QtMaterialLinkCheckPlanMapper, QtMaterialLinkCheckPlan>
    implements QtMaterialLinkCheckPlanService{

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
                qtMaterialLinkCheckPlan.setCheckType(Long.parseLong(typeTmp.getCheckType()));
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
            throw new BusinessException("type值为空");
        }
    }
}




