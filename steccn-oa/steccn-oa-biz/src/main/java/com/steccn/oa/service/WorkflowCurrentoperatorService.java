package com.steccn.oa.service;

import com.steccn.oa.domain.WorkflowCurrentoperator;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.oa.dto.OaUserIdAndName;
import com.steccn.oa.dto.OaWorkFlowOperatorDTO;

import java.util.List;

/**
* @author Admin
* @description 针对表【workflow_currentoperator】的数据库操作Service
* @createDate 2023-03-27 18:00:49
*/
public interface WorkflowCurrentoperatorService extends IService<WorkflowCurrentoperator> {
    /**
     * 根据流程id获取最新操作人id
     * @param requestId
     * @return
     */
    List<OaWorkFlowOperatorDTO> getUserIdByRequestId(Integer requestId);
}
