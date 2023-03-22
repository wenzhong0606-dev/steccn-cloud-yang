package com.steccn.qms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.QtImproveRecordDetail;
import com.steccn.qms.service.QtImproveRecordDetailService;
import com.steccn.qms.mapper.QtImproveRecordDetailMapper;
import org.springframework.stereotype.Service;

/**
 * @author yequan
 * @description 针对表【qms_improve_record_detail(整改记录详情)】的数据库操作Service实现
 * @createDate 2023-03-15 10:16:54
 */
@Service
public class QtImproveRecordDetailServiceImpl extends ServiceImpl<QtImproveRecordDetailMapper, QtImproveRecordDetail>
        implements QtImproveRecordDetailService {

    @Override
    public QtImproveRecordDetail getDetail(Long recordId, Integer status) {
        LambdaQueryWrapper<QtImproveRecordDetail> wrapper = new LambdaQueryWrapper<QtImproveRecordDetail>().eq(QtImproveRecordDetail::getRecordId, recordId);
        if (QtManagerConstant.NUM_2.equals(status)) {
            wrapper.eq(QtImproveRecordDetail::getApproverStep, QtManagerConstant.improveProgress.D1_CREATE.getName());
        } else if (QtManagerConstant.NUM_3.equals(status)) {
            wrapper.eq(QtImproveRecordDetail::getApproverStep, QtManagerConstant.improveProgress.D2_CLEAR_PROBLEM.getName());
        } else if (QtManagerConstant.NUM_4.equals(status)) {
            wrapper.eq(QtImproveRecordDetail::getApproverStep, QtManagerConstant.improveProgress.D3_COUNTERMEASURE.getName());
        } else if (QtManagerConstant.NUM_5.equals(status)) {
            wrapper.eq(QtImproveRecordDetail::getApproverStep, QtManagerConstant.improveProgress.D4_REASON_ANALYSIS.getName());
        } else if (QtManagerConstant.NUM_6.equals(status)) {
            wrapper.eq(QtImproveRecordDetail::getApproverStep, QtManagerConstant.improveProgress.D5_PERMANENT_CORRECTIVE_ACTION_PLAN.getName());
        } else if (QtManagerConstant.NUM_7.equals(status)) {
            wrapper.eq(QtImproveRecordDetail::getApproverStep, QtManagerConstant.improveProgress.D6_CORRECTIVE_ACTION_VERIFICATION.getName());
        }
        return getOne(wrapper);
    }
}




