package com.steccn.platform.controller.feign;


import com.steccn.common.annotation.SysLog;
import com.steccn.common.vo.R;
import com.steccn.platform.domain.ProcessBusiLink;
import com.steccn.platform.dto.ProcessBusiLinkDTO;
import com.steccn.platform.service.ProcessBusiLinkService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api("oa 流程业务关联")
@RestController
@RequiredArgsConstructor
@RequestMapping("/feign/process/busi/Link")
public class ProcessBusiLinkFeignController {


    private final ProcessBusiLinkService processBusiLinkService;


    @SysLog("保存OA流程关联业务")
    @PostMapping
    @ApiOperation("保存OA流程关联业务")
    public R<Boolean> add(@Valid @RequestBody ProcessBusiLinkDTO processBusiLinkDTO) {
        return R.success(processBusiLinkService.add(processBusiLinkDTO));
    }

    @GetMapping()
    @ApiOperation("获取OA流程关联业务数据")
    public R<ProcessBusiLinkDTO> get(@RequestParam("instanceId") Integer instanceId){
        return R.success(processBusiLinkService.get(instanceId));
    }

    @GetMapping("/busi/id")
    @ApiOperation("获取OA流程关联业务数据")
    public R<ProcessBusiLinkDTO> getByBusiId(@RequestParam("busiId") Long busiId){
        return R.success(processBusiLinkService.getByBusiId(busiId));
    }

}
