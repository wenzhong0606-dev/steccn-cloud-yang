package com.steccn.pms.controller;

import com.steccn.common.annotation.SysLog;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.PmsLaborStandardDTO;
import com.steccn.pms.dto.PmsMeltingConfigDTO;
import com.steccn.pms.service.PmsMeltingConfigService;
import com.steccn.pms.vo.PmsMeltingConfigVO;
import com.steccn.pms.vo.PmsPullForceVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2025/03/20
 */
@Api("熔化率控制器")
@RestController
@RequiredArgsConstructor
@RequestMapping("/pms/melting")
public class PmsMeltingConfigController {
    private final PmsMeltingConfigService pmsMeltingConfigService;

    @PostMapping("/add")
    @ApiOperation(value = "设置熔化率")
    @SysLog("设置熔化率")
    public R<Boolean> add(@Validated @RequestBody PmsMeltingConfigDTO dto) {
        return R.success(pmsMeltingConfigService.add(dto));
    }

    @ApiOperation("熔化率设置")
    @GetMapping("/list")
    public R<List<PmsMeltingConfigVO>> list(@RequestParam String monthDate) {
        return R.success(pmsMeltingConfigService.list(monthDate));
    }

    @ApiOperation("熔化率设置")
    @GetMapping("/test")
    public R<Boolean> test() {
        return R.success(pmsMeltingConfigService.addMeltingRate());
    }
}
