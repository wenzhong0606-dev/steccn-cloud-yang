package com.steccn.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.domain.PmsMaterialRequisitionWorkflow;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2023/03/20
 */
public interface PmsMaterialRequisitionWorkflowService extends IService<PmsMaterialRequisitionWorkflow> {

    /**
     * 根据requestId拿到领料单id
     * @param requestId
     * @return
     */
    Long getIdByRequestId(Long requestId);
}
