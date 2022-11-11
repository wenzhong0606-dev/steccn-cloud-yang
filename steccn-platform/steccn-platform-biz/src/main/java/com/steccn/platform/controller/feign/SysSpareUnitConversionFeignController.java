package com.steccn.platform.controller.feign;

import com.google.common.collect.Maps;
import com.steccn.common.vo.R;
import com.steccn.platform.service.SysSpareUnitConversionService;
import com.steccn.platform.vo.SysSpareUnitConversionVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yequan
 * @desc 单位转换dto
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/feign/platform/spare/convert")
public class SysSpareUnitConversionFeignController {

    private final SysSpareUnitConversionService sysSpareUnitConversionService;

    @PostMapping("/unit")
    @ApiOperation("单位转换")
    public R<Map<Long, List<SysSpareUnitConversionVO>>> convert(@RequestBody Set<Long> spareSet) {
        return R.success(sysSpareUnitConversionService.convert(spareSet));
    }
}
