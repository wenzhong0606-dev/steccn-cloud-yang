package com.steccn.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.common.dto.TupleDTO;
import com.steccn.oa.constant.OaConstant;
import com.steccn.oa.domain.Hrmresource;
import com.steccn.oa.domain.WorkflowNodeFlowtime;
import com.steccn.oa.domain.WorkflowNodebase;
import com.steccn.oa.domain.WorkflowRequestlog;
import com.steccn.oa.dto.OaUserIdAndName;
import com.steccn.oa.service.HrmresourceService;
import com.steccn.oa.service.WorkflowNodebaseService;
import com.steccn.oa.service.WorkflowRequestlogService;
import com.steccn.oa.mapper.WorkflowRequestlogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
* @author Admin
* @description 针对表【workflow_requestlog】的数据库操作Service实现
* @createDate 2023-09-01 10:15:22
*/
@Service
@RequiredArgsConstructor
public class WorkflowRequestlogServiceImpl extends ServiceImpl<WorkflowRequestlogMapper, WorkflowRequestlog>
    implements WorkflowRequestlogService{
    private final HrmresourceService hrmresourceService;
    private final WorkflowNodebaseService workflowNodebaseService;

    @Override
    public OaUserIdAndName getOperator(TupleDTO<Integer, Integer> dto) {
        OaUserIdAndName result = new OaUserIdAndName();
        WorkflowRequestlog workflowRequestlog = getOne(new LambdaQueryWrapper<WorkflowRequestlog>()
                .select(WorkflowRequestlog::getOperator)
                .eq(WorkflowRequestlog::getNodeid, dto.getField2())
                .eq(WorkflowRequestlog::getRequestid, dto.getField1())
                .orderByDesc(WorkflowRequestlog::getOperatedate)
                .orderByDesc(WorkflowRequestlog::getOperatetime)
                .last("LIMIT 1"));
        if(Objects.nonNull(workflowRequestlog)){
            Hrmresource one = hrmresourceService.getOne(new LambdaQueryWrapper<Hrmresource>()
                    .select(Hrmresource::getLastname)
                    .eq(Hrmresource::getId, workflowRequestlog.getOperator()).last("LIMIT 1"));
            if(Objects.nonNull(one)){
                result.setId(workflowRequestlog.getOperator());
                result.setName(one.getLastname());
            }
        }
        return result;
    }

    @Override
    public Boolean workflowFinished(Integer requestId) {
        WorkflowRequestlog workflowRequestlog = getOne(new LambdaQueryWrapper<WorkflowRequestlog>()
                .select(WorkflowRequestlog::getNodeid)
                .eq(WorkflowRequestlog::getRequestid, requestId)
                .orderByDesc(WorkflowRequestlog::getOperatedate)
                .orderByDesc(WorkflowRequestlog::getOperatetime)
                .last("LIMIT 1"));
        if(Objects.isNull(workflowRequestlog)){
            return false;
        }
        WorkflowNodebase workflowNodebase = workflowNodebaseService.getOne(new LambdaQueryWrapper<WorkflowNodebase>()
                .select(WorkflowNodebase::getIsend)
                .eq(WorkflowNodebase::getId, workflowRequestlog.getNodeid()));
        if(workflowNodebase.getIsend().equals(OaConstant.NUM_1.toString())){
            return true;
        }else {
            return false;
        }
    }
}




