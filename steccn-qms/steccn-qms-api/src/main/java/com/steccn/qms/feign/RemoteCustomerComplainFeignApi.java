package com.steccn.qms.feign;


import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.OaProcessNotifyDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(contextId = "remoteCustomerComplainFeignApi", value = FeignConstant.STECCN_QMS_SERVICE_NAME)
public interface RemoteCustomerComplainFeignApi {


    @ApiOperation("客诉审批回调")
    @PostMapping("/feign/qms/customer/complaint/approve")
    R<Boolean> approve(@RequestBody OaProcessNotifyDTO dto);
}
