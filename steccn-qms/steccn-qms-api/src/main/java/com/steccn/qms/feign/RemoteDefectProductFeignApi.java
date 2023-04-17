package com.steccn.qms.feign;


import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.OaProcessNotifyDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(contextId = "remoteDefectProductFeignApi", value = FeignConstant.STECCN_QMS_SERVICE_NAME)
public interface RemoteDefectProductFeignApi {


    @ApiOperation("不合格品审批回调")
    @PostMapping("/feign/qms/defect/product/approve")
    R<Boolean> approve(@RequestBody OaProcessNotifyDTO dto);
}
