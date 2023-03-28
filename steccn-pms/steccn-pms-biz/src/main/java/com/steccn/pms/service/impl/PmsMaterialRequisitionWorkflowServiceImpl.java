package com.steccn.pms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.pms.domain.PmsMaterialRequisitionWorkflow;
import com.steccn.pms.service.PmsMaterialRequisitionWorkflowService;
import com.steccn.pms.mapper.PmsMaterialRequisitionWorkflowMapper;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
* @author Admin
* @description 针对表【pms_material_requisition_workflow(生产领料单关联oa流程)】的数据库操作Service实现
* @createDate 2023-03-17 15:40:26
*/
@Service
public class PmsMaterialRequisitionWorkflowServiceImpl extends ServiceImpl<PmsMaterialRequisitionWorkflowMapper, PmsMaterialRequisitionWorkflow>
    implements PmsMaterialRequisitionWorkflowService{

    @Override
    public Long getIdByRequestId(Long requestId) {
        PmsMaterialRequisitionWorkflow one = getOne(new LambdaQueryWrapper<PmsMaterialRequisitionWorkflow>().eq(PmsMaterialRequisitionWorkflow::getRequestId, requestId));
        if(Objects.isNull(one)){
            return null;
        }else {
            return one.getRequisitionId();
        }
    }
}




