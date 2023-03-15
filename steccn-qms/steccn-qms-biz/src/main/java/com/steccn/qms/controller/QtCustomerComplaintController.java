package com.steccn.qms.controller;


import com.steccn.common.annotation.SysLog;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.vo.R;
import com.steccn.qms.dto.QtCheckTaskDestoryInfoDTO;
import com.steccn.qms.dto.QtCustomerComplainDTO;
import com.steccn.qms.dto.QtCustomerComplainDetailDTO;
import com.steccn.qms.service.QtCustomerComplainService;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/qms/customer/complaint")
@ApiModel("客诉控制器")
@Slf4j
public class QtCustomerComplaintController {

    private final QtCustomerComplainService qtCustomerComplainService;


    @PostMapping("/add")
    @ApiOperation(value = "新增客诉记录")
    @SysLog("新增客诉记录")
    public R<Boolean> add(@RequestBody QtCustomerComplainDTO dto) {
        return R.success(qtCustomerComplainService.add(dto));
    }

}
