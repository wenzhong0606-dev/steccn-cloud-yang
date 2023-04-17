package com.steccn.platform.strategy;

import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.platform.constant.OaDataConstant;
import com.steccn.platform.dto.OaProcessNotifyDTO;
import com.steccn.qms.feign.RemoteImproveProgressFeignApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author yequan
 * @desc 整改开始审批
 * @date 2023/4/12
 */
@Service
@RequiredArgsConstructor
public class ImproveProgressStartStrategy extends AbstractOaProcessStrategy {

    private final RemoteImproveProgressFeignApi remoteImproveProgressFeignApi;

    @Override
    public void execute(OaProcessNotifyDTO dto) {
        if (support(dto)) {
            FeignOptional.of(() -> remoteImproveProgressFeignApi.approve(dto), FeignConstant.STECCN_QMS_SERVICE_NAME).get();
        }
    }

    @Override
    public String getWorkFlow() {
        return OaDataConstant.Oa_WorkFlow_Type.IMPROVE_PROGRESS_START.getWorkflowId().toString();
    }
}
