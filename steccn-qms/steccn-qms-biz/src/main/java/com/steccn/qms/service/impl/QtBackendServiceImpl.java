package com.steccn.qms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.steccn.qms.domain.QtItemCategory;
import com.steccn.qms.service.QtBackendService;
import com.steccn.qms.service.QtItemCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import vo.QtItemCategoryVO;

import java.util.List;

/**
 * @desc 后台配置服务实现
 */
@Service
@RequiredArgsConstructor
public class QtBackendServiceImpl implements QtBackendService {

    private final QtItemCategoryService qtItemCategoryService;

    @Override
    public List<QtItemCategoryVO> getCategoryList(Integer type) {
        //1.定义返回集合
        List<QtItemCategoryVO> resList = Lists.newArrayList();
        //2.从数据库获取数据
        List<QtItemCategory> dbList = qtItemCategoryService.list(new LambdaQueryWrapper<QtItemCategory>().eq(QtItemCategory::getType, type));
        //3.不为空，返回数据集合
        if (CollUtil.isNotEmpty(dbList)) {
            resList = BeanUtil.copyToList(dbList, QtItemCategoryVO.class);
        }
        return resList;
    }
}
