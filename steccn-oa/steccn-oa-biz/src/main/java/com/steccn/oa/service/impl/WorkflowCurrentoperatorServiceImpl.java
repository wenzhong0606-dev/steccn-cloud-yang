package com.steccn.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.oa.domain.WorkflowCurrentoperator;
import com.steccn.oa.service.WorkflowCurrentoperatorService;
import com.steccn.oa.mapper.WorkflowCurrentoperatorMapper;
import org.springframework.stereotype.Service;

/**
* @author Admin
* @description 针对表【workflow_currentoperator】的数据库操作Service实现
* @createDate 2023-03-27 18:00:49
*/
@Service
public class WorkflowCurrentoperatorServiceImpl extends ServiceImpl<WorkflowCurrentoperatorMapper, WorkflowCurrentoperator>
    implements WorkflowCurrentoperatorService{

    @Override
    public Integer getUserIdByRequestId(Integer requestId) {
        WorkflowCurrentoperator one = getOne(new LambdaQueryWrapper<WorkflowCurrentoperator>()
                .eq(WorkflowCurrentoperator::getRequestid, requestId)
                .orderByDesc(WorkflowCurrentoperator::getOperatedate)
                .orderByDesc(WorkflowCurrentoperator::getOperatetime),false);
        return one.getUserid();
    }
}




