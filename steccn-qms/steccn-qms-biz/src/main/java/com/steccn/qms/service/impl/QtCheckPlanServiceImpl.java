package com.steccn.qms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.qms.domain.QtCheckPlan;
import com.steccn.qms.domain.QtItemCategory;
import com.steccn.qms.dto.QtCheckPlanDTO;
import com.steccn.qms.service.QtCheckPlanService;
import com.steccn.qms.mapper.QtCheckPlanMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
* @author yequan
* @description 针对表【qt_check_plan(检验方案表)】的数据库操作Service实现
* @createDate 2022-09-02 08:54:09
*/
@Service
public class QtCheckPlanServiceImpl extends ServiceImpl<QtCheckPlanMapper, QtCheckPlan>
    implements QtCheckPlanService{

    @Override
    public Boolean add(QtCheckPlanDTO dto) {
        //1、校验名字不能为空
        Map<String,Object> name = new HashMap<>(1);
        name.put("name",dto.getName());
        Collection<QtCheckPlan> checkNames = listByMap(name);
        if(!checkNames.isEmpty()){
            throw new BusinessException("与已有的检验方案名称重复");
        }
        //2、插入计划表
        QtCheckPlan model = new QtCheckPlan();
        model.setStatus(0);
        model.setName(dto.getName());
        if (Objects.isNull(dto.getId())) {
            model.setCreateBy(SecurityUtils.getUser().getUserId());
            model.setCreateName(SecurityUtils.getUser().getName());
        }
        model.setUpdateBy(SecurityUtils.getUser().getUserId());
        model.setUpdateName(SecurityUtils.getUser().getName());
        boolean save = save(model);
        return save;
    }
}




