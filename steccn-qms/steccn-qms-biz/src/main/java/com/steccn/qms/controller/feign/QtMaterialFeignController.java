package com.steccn.qms.controller.feign;

import com.steccn.common.vo.R;
import com.steccn.platform.dto.EqpSpareDto;
import com.steccn.qms.dto.OutputQualityDetailDTO;
import com.steccn.qms.service.QtMaterialService;
import com.steccn.qms.vo.OutputQualityDetailVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 物料同步
 *
 * @author AndrewLuo
 * @date 2023/09/07
 */
@RestController
@Api("物料同步")
@RequestMapping("/feign/qms/material")
@RequiredArgsConstructor
public class QtMaterialFeignController {

    private final QtMaterialService qtMaterialService;
    @ApiOperation("同步物料")
    @PostMapping("/synchronize")
    public R<Boolean> synchronizeMaterial(@RequestBody EqpSpareDto dto) {
        return R.success(qtMaterialService.synchronizeMaterial(dto));
    }
}
