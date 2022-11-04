package com.steccn.qms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.qms.domain.*;
import com.steccn.qms.dto.QtReportDTO;
import com.steccn.qms.service.*;
import com.steccn.qms.mapper.QtReportMapper;
import com.steccn.qms.vo.QtCheckItemListVO;
import com.steccn.qms.vo.QtReportVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;
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
    public Integer add(QtReportDTO dto) {
        QtReport qtReport = new QtReport();
        BeanUtils.copyProperties(dto,qtReport);
        QtReport report = getOne(new LambdaQueryWrapper<QtReport>().eq(QtReport::getName, dto.getName()));
        Assert.isNull(report,"该报表卡片已存在,请勿重复创建!");
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
        List<QtCheckPlanGroup> qtCheckPlanGroups = qtCheckPlanGroupService.list(new LambdaQueryWrapper<QtCheckPlanGroup>().in(QtCheckPlanGroup::getPlanId, collectCheckPlanId));
        qtCheckPlanGroups.forEach(q->{
            groupIdList.add(q.getId());
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
        List<QtCheckItem> qtCheckItems = qtCheckItemService.list(new LambdaQueryWrapper<QtCheckItem>().in(QtCheckItem::getId, checkItemIds));
        //遍历集合往检验项集合中添加数据
        qtCheckItems.forEach(q->{
            QtCheckItemListVO qtCheckItemListVO = new QtCheckItemListVO();
            BeanUtils.copyProperties(q,qtCheckItemListVO);
            qtCheckItemListVOS.add(qtCheckItemListVO);
        });
        return qtCheckItemListVOS;
    }

    /**
     * 根据id删除spc卡片
     * @param id
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean reportDelete(Integer id) {
        if (Objects.isNull(id)){
            throw new BusinessException("spc卡片id不能为空");
        }
        removeById(id);
        return Boolean.TRUE;
    }

    /**
     * 根据id查询spc控制图
     * @param id
     * @return
     */
    @Override
    public QtReportVO getQtReport(Integer id) {
        if (Objects.isNull(id)){
            throw new BusinessException("查询id不能为空");
        }
        QtReport qtReport = getById(id);
        QtReportVO qtReportVO = new QtReportVO();
        if (Objects.isNull(qtReport)){
            throw new BusinessException("数据库中没有此SPC控制图");
        }
        BeanUtils.copyProperties(qtReport,qtReportVO);
        qtReportVO.setCustomer(qtReport.getCustomer());
        qtReportVO.setMaterialUseWorkshop(qtReport.getMaterialUseWorkshop());
        qtReportVO.setProductLine(qtReport.getProductLine());
        qtReportVO.setWorkshop(qtReport.getWorkshop());
        qtReportVO.setSupplier(qtReport.getSupplier());
        return qtReportVO;
    }

    /**
     * 编辑SPC控制图卡片
     * @param qtReportDTO
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean edit(QtReportDTO qtReportDTO) {
        LambdaQueryWrapper<QtReport> eq = new LambdaQueryWrapper<QtReport>().eq(QtReport::getId, qtReportDTO.getId());
        QtReport report = new QtReport();
        if (Objects.nonNull(eq)){
            BeanUtils.copyProperties(qtReportDTO,report);
        }
        return updateById(report);
    }

}




