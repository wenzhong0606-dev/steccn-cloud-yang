package com.steccn.qms.feign;


import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.OaProcessNotifyDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(contextId = "remoteImproveProgressFeignApi", value = FeignConstant.STECCN_QMS_SERVICE_NAME)
public interface RemoteImproveProgressFeignApi {


    @ApiOperation("整改启动回调")
    @PostMapping("/feign/qms/improve/record/approve")
    R<Boolean> approve(@RequestBody OaProcessNotifyDTO dto);


    @ApiOperation("整改结束审批回调")
    @PostMapping("/feign/qms/improve/record/finishApprove")
    R<Boolean> finishApprove(@RequestBody OaProcessNotifyDTO dto);
}
