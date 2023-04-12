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
 * @desc 整改开始审批
 * @date 2023/4/12
 */
@Service
@RequiredArgsConstructor
public class ImproveProgressEndStrategy extends AbstractOaProcessStrategy {

    private final RemoteImproveProgressFeignApi remoteImproveProgressFeignApi;

    @Override
    public void execute(OaProcessNotifyDTO dto) {
        if (support(dto)) {
            FeignOptional.of(() -> remoteImproveProgressFeignApi.finishApprove(dto), FeignConstant.STECCN_QMS_SERVICE_NAME).get();
        }
    }

    @Override
    public String getWorkFlow() {
        return OaDataConstant.Oa_WorkFlow_Type.IMPROVE_PROGRESS_END.getWorkflowId().toString();
    }
}
