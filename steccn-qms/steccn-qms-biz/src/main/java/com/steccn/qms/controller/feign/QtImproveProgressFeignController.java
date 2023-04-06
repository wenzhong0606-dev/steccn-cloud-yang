package com.steccn.qms.controller.feign;


import com.steccn.common.vo.R;
import com.steccn.platform.dto.OaProcessNotifyDTO;
import com.steccn.qms.service.QtDefectProductService;
import com.steccn.qms.service.QtImproveRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api("整改")
@RequestMapping("/feign/qms/improve/record")
@RequiredArgsConstructor
public class QtImproveProgressFeignController {

    private final QtImproveRecordService qtImproveRecordService;

    @ApiOperation("整改启动审批回调")
    @PostMapping("/approve")
    public R<Boolean> approve(@RequestBody OaProcessNotifyDTO dto){
       return R.success(qtImproveRecordService.approve(dto));
    }


    @ApiOperation("整改结束审批回调")
    @PostMapping("/finishApprove")
    public R<Boolean> finishApprove(@RequestBody OaProcessNotifyDTO dto){
        return R.success(qtImproveRecordService.finishApprove(dto));
    }
}
