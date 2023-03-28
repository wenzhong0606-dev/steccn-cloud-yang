package com.steccn.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.query.MPJLambdaQueryWrapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.steccn.oa.domain.Hrmresource;
import com.steccn.oa.domain.WorkflowCurrentoperator;
import com.steccn.oa.dto.OaOperatorParamDTO;
import com.steccn.oa.dto.OaUserIdAndName;
import com.steccn.oa.dto.OaWorkFlowOperatorDTO;
import com.steccn.oa.service.WorkflowCurrentoperatorService;
import com.steccn.oa.mapper.WorkflowCurrentoperatorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Admin
* @description 针对表【workflow_currentoperator】的数据库操作Service实现
* @createDate 2023-03-27 18:00:49
*/
@Service
@RequiredArgsConstructor
public class WorkflowCurrentoperatorServiceImpl extends ServiceImpl<WorkflowCurrentoperatorMapper, WorkflowCurrentoperator>
    implements WorkflowCurrentoperatorService{
    private final WorkflowCurrentoperatorMapper workflowCurrentoperMapper;

    @Override
    public OaWorkFlowOperatorDTO getUserIdByRequestId(OaOperatorParamDTO dto) {
        //联合查询操作人
        MPJLambdaWrapper condition = new MPJLambdaWrapper<OaWorkFlowOperatorDTO>()
                .selectAs(WorkflowCurrentoperator::getUserid,OaWorkFlowOperatorDTO::getId)
                .selectAs(WorkflowCurrentoperator::getOperatedate,OaWorkFlowOperatorDTO::getOperateDate)
                .selectAs(WorkflowCurrentoperator::getOperatetime,OaWorkFlowOperatorDTO::getOperateTime)
                .selectAs(Hrmresource::getLastname,OaWorkFlowOperatorDTO::getName)
                .selectAs(Hrmresource::getWorkcode,OaWorkFlowOperatorDTO::getWorkcode)
                .leftJoin(Hrmresource.class,Hrmresource::getId,WorkflowCurrentoperator::getUserid)
                .eq(WorkflowCurrentoperator::getRequestid, dto.getRequestId())
                .eq(WorkflowCurrentoperator::getNodeid,dto.getNodeId());
        return workflowCurrentoperMapper.selectJoinOne(OaWorkFlowOperatorDTO.class,condition);
    }
}




