package com.steccn.oa.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.query.MPJLambdaQueryWrapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.common.dto.TupleDTO;
import com.steccn.oa.constant.OaConstant;
import com.steccn.oa.domain.Hrmresource;
import com.steccn.oa.domain.WorkflowCurrentoperator;
import com.steccn.oa.domain.WorkflowNodebase;
import com.steccn.oa.domain.WorkflowNownode;
import com.steccn.oa.dto.OaOperatorParamDTO;
import com.steccn.oa.dto.OaUserIdAndName;
import com.steccn.oa.dto.OaWorkFlowOperatorDTO;
import com.steccn.oa.mapper.WorkflowNodebaseMapper;
import com.steccn.oa.mapper.WorkflowNownodeMapper;
import com.steccn.oa.service.WorkflowCurrentoperatorService;
import com.steccn.oa.mapper.WorkflowCurrentoperatorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final WorkflowNodebaseMapper workflowNodebaseMapper;
    private final WorkflowNownodeMapper workflowNownodeMapper;

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
                .eq(WorkflowCurrentoperator::getNodeid,dto.getNodeId())
                .orderByDesc(WorkflowCurrentoperator::getOperatedate)
                .orderByDesc(WorkflowCurrentoperator::getOperatetime)
                .last("LIMIT 1");
        return workflowCurrentoperMapper.selectJoinOne(OaWorkFlowOperatorDTO.class,condition);
    }

    @Override
    public Set<Integer> getUserByRequestIdAndNodeId(TupleDTO<String,String> dto) {
        Set<Integer> results = Sets.newHashSet();
        WorkflowNownode workflowNownode = workflowNownodeMapper.selectOne(
                new LambdaQueryWrapper<WorkflowNownode>().select(WorkflowNownode::getNownodeid)
                .eq(WorkflowNownode::getRequestid, dto.getField1()).last("LIMIT 1"));
        WorkflowNodebase workflowNodebase = workflowNodebaseMapper.selectOne(new LambdaQueryWrapper<WorkflowNodebase>()
                        .select(WorkflowNodebase::getIsend)
                        .eq(WorkflowNodebase::getId,workflowNownode.getNownodeid())
                        .last("LIMIT 1"));
        if(workflowNodebase.getIsend().equals(OaConstant.NUM_1.toString())){
            results.add(-1);
            return results;
        }
        List<WorkflowCurrentoperator> list = list(new LambdaQueryWrapper<WorkflowCurrentoperator>()
                .select(WorkflowCurrentoperator::getUserid)
                .eq(WorkflowCurrentoperator::getRequestid, dto.getField1())
                .eq(WorkflowCurrentoperator::getNodeid, workflowNownode.getNownodeid()));
        if(CollUtil.isNotEmpty(list)){
            results = list.stream().map(WorkflowCurrentoperator::getUserid).collect(Collectors.toSet());
        }
        return results;
    }
}




