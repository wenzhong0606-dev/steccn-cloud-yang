package com.steccn.qms.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.controller.QtImproveRecordApproveDTO;
import com.steccn.qms.domain.QtImproveStepMan;
import com.steccn.qms.dto.QtImproveRecordDisposeDTO;
import com.steccn.qms.mapper.QtImproveStepManMapper;
import com.steccn.qms.service.QtImproveStepManService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QtImproveStepManServiceImpl extends ServiceImpl<QtImproveStepManMapper, QtImproveStepMan> implements QtImproveStepManService {
    @Override
    public Boolean saveMan(QtImproveRecordApproveDTO dto) {
        List<QtImproveStepMan> manList = Lists.newArrayList();
        if (CollUtil.isNotEmpty(dto.getCarbonCopyManList())) {
            //保存抄送人信息
            remove(new LambdaQueryWrapper<QtImproveStepMan>().eq(QtImproveStepMan::getRecordId, dto.getId()).eq(QtImproveStepMan::getUserType, QtManagerConstant.NUM_1));
            dto.getCarbonCopyManList().forEach(man -> {
                QtImproveStepMan qtImproveStepMan = new QtImproveStepMan();
                qtImproveStepMan.setRecordId(dto.getId());
                qtImproveStepMan.setUserType(QtManagerConstant.NUM_1);
                qtImproveStepMan.setUserId(man.getUserId());
                qtImproveStepMan.setUserName(man.getUserName());
                manList.add(qtImproveStepMan);
            });
        }
        return saveBatch(manList);
    }
}
