package com.steccn.qms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.qms.domain.*;
import com.steccn.qms.dto.QtAddReportDTO;
import com.steccn.qms.service.*;
import com.steccn.qms.mapper.QtReportMapper;
import com.steccn.qms.vo.QtCheckItemListVO;
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
    private final QtCheckPlanGroupItemService qtCheckPlanGroupItemService;
    private final QtCheckItemService qtCheckItemService;

    /**
     * 新建报表卡片
     * @param dto
     * @return
     */
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

    /**
     * 根据物料id以及检验类型选择检验分组
     * @param materialId
     * @param checkType
     * @return
     */
    @Override
    public List<Integer> getGroupIdList(Long materialId, Integer checkType) {
        List<QtMaterialLinkCheckPlan> materialLinkCheckPlans = qtMaterialLinkCheckPlanService.list(new LambdaQueryWrapper<QtMaterialLinkCheckPlan>()
                .eq(QtMaterialLinkCheckPlan::getMaterialId, materialId)
                .eq(QtMaterialLinkCheckPlan::getCheckTypeCode, checkType));
        //获取检验方案id
        List<Integer> collectCheckPlanId = materialLinkCheckPlans.stream().map(QtMaterialLinkCheckPlan::getCheckPlanId).collect(Collectors.toList());
        if (CollUtil.isEmpty(collectCheckPlanId)){
            throw new BusinessException("请先在物料配置中为物料配置#选中的检验类型#的检验方案");
        }
        //创建检验方案分组id集合
        List<Integer> groupIdList= Lists.newArrayList();
        //遍历检验方案id,去检验方案分组表查询获取分组id并添加进分组id集合
        collectCheckPlanId.forEach(collectId->{
            Integer groupId = qtCheckPlanGroupService.getOne(new LambdaQueryWrapper<QtCheckPlanGroup>().eq(QtCheckPlanGroup::getPlanId, collectId)).getId();
            groupIdList.add(groupId);
        });
        return groupIdList;
    }

    /**
     * 根据检验分组id选择检验项
     * @param groupid
     * @return
     */
    @Override
    public List<QtCheckItemListVO> getQtCheckItemList(Integer groupid) {
        //获取检验项id集合
        List<Integer> checkItemIds = qtCheckPlanGroupItemService.list(new LambdaQueryWrapper<QtCheckPlanGroupItem>().
                        eq(QtCheckPlanGroupItem::getGroupId, groupid)).stream()
                .map(QtCheckPlanGroupItem::getCheckItemId).collect(Collectors.toList());
        if (CollUtil.isEmpty(checkItemIds)){
            throw new BusinessException("该检验分组下没有检验项");
        }
        //创建返回检验项的集合
        List<QtCheckItemListVO> qtCheckItemListVOS=Lists.newArrayList();
        checkItemIds.forEach(checkItemId->{
            QtCheckItemListVO qtCheckItemListVO = new QtCheckItemListVO();
            QtCheckItem qtCheckItem = qtCheckItemService.getOne(new LambdaQueryWrapper<QtCheckItem>().eq(QtCheckItem::getId, checkItemId));
            BeanUtils.copyProperties(qtCheckItem,qtCheckItemListVO);
            qtCheckItemListVOS.add(qtCheckItemListVO);
        });
        return qtCheckItemListVOS;
    }

}




