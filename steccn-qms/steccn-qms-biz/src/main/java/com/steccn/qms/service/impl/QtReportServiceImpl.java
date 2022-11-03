package com.steccn.qms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.qms.domain.QtCheckPlanGroup;
import com.steccn.qms.domain.QtMaterialLinkCheckPlan;
import com.steccn.qms.domain.QtReport;
import com.steccn.qms.dto.QtAddReportDTO;
import com.steccn.qms.service.QtCheckPlanGroupService;
import com.steccn.qms.service.QtMaterialLinkCheckPlanService;
import com.steccn.qms.service.QtReportService;
import com.steccn.qms.mapper.QtReportMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;

/**
* @author 86178
* @description 针对表【qt_report】的数据库操作Service实现
* @createDate 2022-11-01 16:42:01
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class QtReportServiceImpl extends ServiceImpl<QtReportMapper, QtReport>
    implements QtReportService{
    private final QtMaterialLinkCheckPlanService qtMaterialLinkCheckPlanService;
    private final QtCheckPlanGroupService qtCheckPlanGroupService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer add(QtAddReportDTO dto) {
        QtReport qtReport = new QtReport();
        BeanUtils.copyProperties(dto,qtReport);
        QtReport report = getOne(new LambdaQueryWrapper<QtReport>().eq(QtReport::getName, dto.getName()));
        Assert.isNull(report,"该报表卡片名称已存在");
        this.save(qtReport);
        return qtReport.getId();
    }

    @Override
    public List<Integer> getGroupIdList(Long materialId, Integer checkType) {
        List<QtMaterialLinkCheckPlan> materialLinkCheckPlans = qtMaterialLinkCheckPlanService.list(new LambdaQueryWrapper<QtMaterialLinkCheckPlan>()
                .eq(QtMaterialLinkCheckPlan::getMaterialId, materialId)
                .eq(QtMaterialLinkCheckPlan::getCheckTypeCode, checkType));
        List<Integer> collectCheckPlanId = materialLinkCheckPlans.stream().map(QtMaterialLinkCheckPlan::getCheckPlanId).collect(Collectors.toList());
        if (CollUtil.isEmpty(collectCheckPlanId)){
            throw new BusinessException("请先在物料配置中为物料配置#选中的检验类型#的检验方案");
        }
        List<Integer> groupIdList= Lists.newArrayList();
        collectCheckPlanId.forEach(collectId->{
            Integer groupId = qtCheckPlanGroupService.getOne(new LambdaQueryWrapper<QtCheckPlanGroup>().eq(QtCheckPlanGroup::getPlanId, collectId)).getId();
            groupIdList.add(groupId);
        });
        return groupIdList;
    }

}




