package com.steccn.platform.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.SysMsgSendDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author yequan
 * @desc 消息推送
 */
@FeignClient(contextId = "remoteMsgFeignApi", value = FeignConstant.STECCN_PLATFORM_SERVICE_NAME)
public interface RemoteMsgFeignApi {

    @PostMapping("/feign/platform/sys/msg")
    @ApiOperation("发送消息")
    R<Boolean> sendMsg(@RequestBody SysMsgSendDTO sysMsgDTO);
}
