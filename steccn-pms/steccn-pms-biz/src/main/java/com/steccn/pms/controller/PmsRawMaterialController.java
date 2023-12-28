package com.steccn.pms.controller;

import cn.hutool.core.lang.Assert;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.PmsRawMaterialCountDTO;
import com.steccn.pms.service.PmRawMaterialCountService;
import com.steccn.pms.vo.PmRawMaterialVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2023/12/21
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/pms/raw/material")
public class PmsRawMaterialController {

    private final PmRawMaterialCountService pmRawMaterialCountService;

    @PostMapping("/affirm")
    @ApiOperation("根据日期检索合计值")
    public R<Boolean> listByDate(@RequestBody @Valid PmsRawMaterialCountDTO dto) {
        return R.success(pmRawMaterialCountService.generateDeliveryAndSave(dto));
    }
}
