package com.steccn.platform.strategy;

import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.platform.constant.OaDataConstant;
import com.steccn.platform.dto.OaProcessNotifyDTO;
import com.steccn.pms.feign.RemotePmsFeignApi;
import com.steccn.qms.feign.RemoteCustomerComplainFeignApi;
import com.steccn.qms.feign.RemoteDefectProductFeignApi;
import com.steccn.qms.feign.RemoteImproveProgressFeignApi;
import com.steccn.websocket.feign.RemoteWsFeignApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author yequan
 * @desc 不合格品
 * @date 2023/4/12
 */
@Service
@RequiredArgsConstructor
public class DefectProductStrategy extends AbstractOaProcessStrategy {

    private final RemoteDefectProductFeignApi remoteDefectProductFeignApi;

    @Override
    public void execute(OaProcessNotifyDTO dto) {
        if (support(dto)) {
            FeignOptional.of(() -> remoteDefectProductFeignApi.approve(dto), FeignConstant.STECCN_QMS_SERVICE_NAME).get();
        }
    }

    @Override
    public String getWorkFlow() {
        return OaDataConstant.Oa_WorkFlow_Type.DEFECT_PRODUCT.getWorkflowId().toString();
    }
}
