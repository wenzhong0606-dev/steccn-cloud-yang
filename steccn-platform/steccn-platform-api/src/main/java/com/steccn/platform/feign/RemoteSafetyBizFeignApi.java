package com.steccn.platform.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.SysMsgSendDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2024/01/23
 */
@FeignClient(contextId = "remoteSafetyBizFeignApi", value = FeignConstant.STECCN_PLATFORM_SERVICE_NAME)
public interface RemoteSafetyBizFeignApi {

    /**
     * 发送所有安环待办消息
     * @return
     */
    @GetMapping("/feign/platform/safety/sendAllMsg")
    @ApiOperation("发送消息")
    R<Boolean> sendMsg();
}
