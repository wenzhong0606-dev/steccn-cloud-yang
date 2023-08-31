package com.steccn.oa.service;

import com.steccn.common.dto.TupleDTO;
import com.steccn.oa.domain.WorkflowNodeFlowtime;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.oa.dto.OaUserIdAndName;

/**
* @author Admin
* @description 针对表【workflow_node_flowtime】的数据库操作Service
* @createDate 2023-08-31 10:51:50
*/
public interface WorkflowNodeFlowtimeService extends IService<WorkflowNodeFlowtime> {
    /**
     * 获取流程节点操作人
     * @param dto E1 requestId E2 nodeId
     * @return
     */
    OaUserIdAndName getOperator(TupleDTO<Integer,Integer> dto);
}
