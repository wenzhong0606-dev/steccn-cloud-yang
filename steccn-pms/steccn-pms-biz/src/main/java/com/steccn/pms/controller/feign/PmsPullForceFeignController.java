package com.steccn.pms.controller.feign;

import com.steccn.common.dto.ValidList;
import com.steccn.common.valid.UpdateGroup;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.PmsPullForceEditDTO;
import com.steccn.pms.service.PmsPullForceService;
import com.steccn.pms.service.PmsReportWorkService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;


@RestController
@Api("拉引量feign")
@RequestMapping("/feign/pms/pull/force")
@RequiredArgsConstructor
public class PmsPullForceFeignController {

    private final PmsPullForceService pmsPullForceService;


    @ApiOperation("拉引量值预填")
    @PostMapping
    public R<Boolean> edit(@Validated(UpdateGroup.class)@Valid @RequestBody ValidList<PmsPullForceEditDTO> dtoList) {
        return R.success(pmsPullForceService.edit(dtoList.getList()));
    }
}
