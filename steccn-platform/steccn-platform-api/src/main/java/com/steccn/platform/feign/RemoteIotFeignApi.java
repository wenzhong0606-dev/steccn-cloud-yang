package com.steccn.platform.feign;

import com.steccn.common.vo.R;
import com.steccn.platform.dto.SrmOaWorkFlowDTO;
import com.steccn.platform.vo.IotIdNameVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数采系统查询API
 *
 * @author AndrewLuo
 * @date 2025/11/11
 */
@FeignClient(name = "remoteIotFeignApi", url = "60.171.128.8:82", decode404 = true)
public interface RemoteIotFeignApi {

    /**
     * 查询设备分组信息
     * @param headers
     * @param dto
     * @return
     */
    @GetMapping(value = "/iot/api/v1/device_group/for_display")
    @ResponseBody
    R<List<IotIdNameVO>> queryEqpGroup(@RequestHeader MultiValueMap<String,String> headers);
}
