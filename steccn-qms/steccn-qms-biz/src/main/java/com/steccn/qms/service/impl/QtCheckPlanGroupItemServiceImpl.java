package com.steccn.qms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.qms.domain.QtCheckPlanGroupItem;
import com.steccn.qms.domain.QtDefectiveItem;
import com.steccn.qms.service.QtCheckPlanGroupItemService;
import com.steccn.qms.mapper.QtCheckPlanGroupItemMapper;
import com.steccn.qms.service.QtDefectiveItemService;
import com.steccn.qms.vo.QtCheckTaskDefectVO;
import com.steccn.qms.vo.QtTaskDefectVO;
import lombok.NoArgsConstructor;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【qt_check_plan_group_item(检验方案分组检验项目表)】的数据库操作Service实现
 * @createDate 2022-09-02 08:54:09
 */
@Service
@NoArgsConstructor
public class QtCheckPlanGroupItemServiceImpl extends ServiceImpl<QtCheckPlanGroupItemMapper, QtCheckPlanGroupItem>
        implements QtCheckPlanGroupItemService {

    private QtDefectiveItemService qtDefectiveItemService;

    @Override
    public Set<Integer> inUseCheckItem() {
        List<QtCheckPlanGroupItem> dataList = list();
        if (CollUtil.isEmpty(dataList)) {
            return Sets.newHashSet();
        }
        Set<Integer> resSet = Sets.newHashSet();
        for (QtCheckPlanGroupItem data : dataList) {
            resSet.add(data.getCheckItemId());
            if (CollUtil.isNotEmpty(data.getAverageArray())) {
                resSet.addAll(data.getAverageArray());
            }
            if (CollUtil.isNotEmpty(data.getRangeArray())) {
                resSet.addAll(data.getRangeArray());
            }
        }
        return resSet;
    }

    @Override
    public Set<Integer> inUseDefectiveItem() {
        List<QtCheckPlanGroupItem> dataList = list();
        if (CollUtil.isEmpty(dataList)) {
            return Sets.newHashSet();
        }
        Set<Integer> resSet = Sets.newHashSet();
        for (QtCheckPlanGroupItem data : dataList) {
            if (CollUtil.isNotEmpty(data.getLinkedDefectiveItem())) {
                resSet.addAll(data.getLinkedDefectiveItem());
            }
        }
        return resSet;
    }

    @Override
    public List<QtCheckTaskDefectVO> findTaskDefect(Integer id) {
        //找到groupItem
        QtCheckPlanGroupItem qtCheckPlanGroupItem = getById(id);
        //若数据为空则抛出异常
        if(qtCheckPlanGroupItem.getLinkedDefectiveItem() == null || qtCheckPlanGroupItem.getLinkedDefectiveItem().size()==0){
            throw new BusinessException("该检验项对应的缺陷项为空");
        }
        LambdaQueryWrapper<QtDefectiveItem> condition = new LambdaQueryWrapper<QtDefectiveItem>()
                .in(QtDefectiveItem::getId,qtCheckPlanGroupItem.getLinkedDefectiveItem())
                .orderByAsc(QtDefectiveItem::getCategoryId)
                .orderByAsc(QtDefectiveItem::getCreateTime);
        List<QtDefectiveItem> defectiveItems =  qtDefectiveItemService.list(condition);
        //处理items
        Map<Integer,QtTaskDefectVO> defects = new HashMap<>();
        defectiveItems.forEach(tmp->{
            QtTaskDefectVO vo = new QtTaskDefectVO();
            vo.setId(tmp.getId());
            vo.setName(tmp.getName());
            defects.put(tmp.getCategoryId(),vo);
        });
        QtCheckTaskDefectVO vo = new QtCheckTaskDefectVO();
        vo.setDefects(defects);
        List<QtCheckTaskDefectVO> vos = Lists.newArrayList();
        vos.add(vo);
        return vos;
    }
}




