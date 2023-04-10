package com.steccn.qms.controller.feign;


import cn.hutool.core.lang.Assert;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.OaProcessNotifyDTO;
import com.steccn.qms.domain.QtCustomerComplain;
import com.steccn.qms.service.QtCustomerComplainService;
import com.steccn.qms.vo.QtCheckTaskDetailItemMaterialVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Api("客诉")
@RequestMapping("/feign/qms/customer/complaint")
@RequiredArgsConstructor
public class QtCustomerComplainFeignController {

    private final QtCustomerComplainService qtCustomerComplainService;

    @ApiOperation("客诉审批回调")
    @PostMapping("/approve")
    public R<Boolean> approve(@RequestBody OaProcessNotifyDTO dto){
       return R.success(qtCustomerComplainService.approve(dto));
    }
}
