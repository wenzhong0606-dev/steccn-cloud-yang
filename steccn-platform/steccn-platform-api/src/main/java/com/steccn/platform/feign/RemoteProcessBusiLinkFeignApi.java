package com.steccn.platform.feign;


import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.ProcessBusiLinkDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

@FeignClient(contextId = "remoteProcessBusiLinkFeignApi", value = FeignConstant.STECCN_PLATFORM_SERVICE_NAME)
public interface RemoteProcessBusiLinkFeignApi {

    @PostMapping("/feign/process/busi/Link")
    @ApiOperation("保存OA流程关联业务")
    R<Boolean> add(@RequestBody ProcessBusiLinkDTO processBusiLinkDTO);


    @GetMapping("/feign/process/busi/Link")
    @ApiOperation("获取OA流程关联业务数据")
    R<ProcessBusiLinkDTO> get(@RequestParam("instanceId") Integer instanceId);
    @GetMapping("/feign/process/busi/Link/busi/id")
    @ApiOperation("获取OA流程关联业务数据")
    R<ProcessBusiLinkDTO> getByBusiId(@RequestParam("busiId") Long busiId);

}
