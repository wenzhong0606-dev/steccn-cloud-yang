package com.steccn.platform.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.SysUserClientRefDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(contextId = "remoteSysUserClientFeignApi", value = FeignConstant.STECCN_PLATFORM_SERVICE_NAME)
public interface RemoteSysUserClientFeignApi {

    /**
     * 移动端登录时，绑定消息通知
     *
     * @param dto dto
     * @return 绑定
     */
    @ApiOperation(value = "移动端登录时，绑定消息通知")
    @PostMapping("/feign/platform/sys/user/client/bind")
    R<Boolean> bind(@RequestBody SysUserClientRefDTO dto);

}
