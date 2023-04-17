package com.steccn.qms.controller.feign;


import com.steccn.common.vo.R;
import com.steccn.platform.dto.OaProcessNotifyDTO;
import com.steccn.qms.service.QtCustomerComplainService;
import com.steccn.qms.service.QtDefectProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api("客诉")
@RequestMapping("/feign/qms/defect/product")
@RequiredArgsConstructor
public class QtDefectProductFeignController {

    private final QtDefectProductService qtDefectProductService;

    @ApiOperation("不合格品审批回调")
    @PostMapping("/approve")
    public R<Boolean> approve(@RequestBody OaProcessNotifyDTO dto){
       return R.success(qtDefectProductService.approve(dto));
    }
}
