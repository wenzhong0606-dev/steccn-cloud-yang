package com.steccn.pms.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.constant.FeignConstant;
import com.steccn.oa.feign.OaInfoService;
import com.steccn.pms.domain.PmsMaterialRequisitionWorkflow;
import com.steccn.pms.service.PmsMaterialRequisitionWorkflowService;
import com.steccn.pms.mapper.PmsMaterialRequisitionWorkflowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
* @author Admin
* @description 针对表【pms_material_requisition_workflow(生产领料单关联oa流程)】的数据库操作Service实现
* @createDate 2023-03-17 15:40:26
*/
@Service
@RequiredArgsConstructor
public class PmsMaterialRequisitionWorkflowServiceImpl extends ServiceImpl<PmsMaterialRequisitionWorkflowMapper, PmsMaterialRequisitionWorkflow>
    implements PmsMaterialRequisitionWorkflowService{
    private final OaInfoService oaInfoService;

    @Override
    public Long getIdByRequestId(Long requestId) {
        PmsMaterialRequisitionWorkflow one = getOne(new LambdaQueryWrapper<PmsMaterialRequisitionWorkflow>().eq(PmsMaterialRequisitionWorkflow::getRequestId, requestId),false);
        if(Objects.isNull(one)){
            return null;
        }else {
            return one.getRequisitionId();
        }
    }

    @Override
    public String getTokenById(Long id) {
        PmsMaterialRequisitionWorkflow one = getOne(new LambdaQueryWrapper<PmsMaterialRequisitionWorkflow>().eq(PmsMaterialRequisitionWorkflow::getRequisitionId, id),false);
        if(Objects.isNull(one)){
            throw new BusinessException("该条数据未绑定流程");
        }
        //获取oa中的loginId
        String loginId = FeignOptional.of(() -> oaInfoService.getLoginIdById(Integer.parseInt(one.getCreateBy().toString())), FeignConstant.STECCN_OA_SERVICE_NAME).get();
        //获取token
        Object o = FeignOptional.of(() -> oaInfoService.getSsoToken(loginId), FeignConstant.STECCN_OA_SERVICE_NAME).get();
        JSONObject jsonObject = (JSONObject) JSON.toJSON(o);
        return jsonObject.getString("token");
    }
}




