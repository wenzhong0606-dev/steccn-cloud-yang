package com.steccn.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.common.dto.TupleDTO;
import com.steccn.oa.domain.Hrmresource;
import com.steccn.oa.domain.WorkflowNodeFlowtime;
import com.steccn.oa.dto.OaUserIdAndName;
import com.steccn.oa.service.HrmresourceService;
import com.steccn.oa.service.WorkflowNodeFlowtimeService;
import com.steccn.oa.mapper.WorkflowNodeFlowtimeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
* @author Admin
* @description 针对表【workflow_node_flowtime】的数据库操作Service实现
* @createDate 2023-08-31 10:51:50
*/
@Service
@RequiredArgsConstructor
public class WorkflowNodeFlowtimeServiceImpl extends ServiceImpl<WorkflowNodeFlowtimeMapper, WorkflowNodeFlowtime>
    implements WorkflowNodeFlowtimeService{
    private final HrmresourceService hrmresourceService;

    @Override
    public OaUserIdAndName getOperator(TupleDTO<Integer, Integer> dto) {
        OaUserIdAndName result = new OaUserIdAndName();
        WorkflowNodeFlowtime workflowNodeFlowtime = getOne(new LambdaQueryWrapper<WorkflowNodeFlowtime>()
                .select(WorkflowNodeFlowtime::getNodeoperator)
                .eq(WorkflowNodeFlowtime::getRequestid, dto.getField1())
                .eq(WorkflowNodeFlowtime::getNodeid, dto.getField2()).last("LIMIT 1"));
        if(Objects.nonNull(workflowNodeFlowtime)){
            Hrmresource one = hrmresourceService.getOne(new LambdaQueryWrapper<Hrmresource>()
                    .select(Hrmresource::getLastname)
                    .eq(Hrmresource::getId, workflowNodeFlowtime.getNodeoperator()).last("LIMIT 1"));
            if(Objects.nonNull(one)){
                result.setId(workflowNodeFlowtime.getNodeoperator());
                result.setName(one.getLastname());
            }
        }
        return result;
    }
}




