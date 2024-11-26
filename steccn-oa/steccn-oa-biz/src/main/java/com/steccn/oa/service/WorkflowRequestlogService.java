package com.steccn.oa.service;

import com.steccn.common.dto.TupleDTO;
import com.steccn.oa.domain.WorkflowRequestlog;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.oa.dto.OaUserIdAndName;

/**
* @author Admin
* @description 针对表【workflow_requestlog】的数据库操作Service
* @createDate 2023-09-01 10:15:22
*/
public interface WorkflowRequestlogService extends IService<WorkflowRequestlog> {
    /**
     * 获取流程节点操作人
     * @param dto E1 requestId E2 nodeId
     * @return
     */
    OaUserIdAndName getOperator(TupleDTO<Integer,Integer> dto);

    /**
     * 根据requestId查询节点是否完成
     * @return
     */
    Boolean workflowFinished(Integer requestId);
}
