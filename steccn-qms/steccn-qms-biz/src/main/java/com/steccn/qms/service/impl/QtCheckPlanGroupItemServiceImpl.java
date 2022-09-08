package com.steccn.qms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Sets;
import com.steccn.qms.domain.QtCheckPlanGroupItem;
import com.steccn.qms.service.QtCheckPlanGroupItemService;
import com.steccn.qms.mapper.QtCheckPlanGroupItemMapper;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【qt_check_plan_group_item(检验方案分组检验项目表)】的数据库操作Service实现
 * @createDate 2022-09-02 08:54:09
 */
@Service
public class QtCheckPlanGroupItemServiceImpl extends ServiceImpl<QtCheckPlanGroupItemMapper, QtCheckPlanGroupItem>
        implements QtCheckPlanGroupItemService {

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
}




