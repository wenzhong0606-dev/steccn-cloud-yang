package com.steccn.ems.controller;

import com.steccn.common.annotation.SysLog;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.vo.R;
import com.steccn.ems.dto.EpAddEnergyTypeDTO;
import com.steccn.ems.service.EpEnergyTypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Api("能源类型")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ems/energy/type")
public class EpEnergyTypeController {
    private EpEnergyTypeService epEnergyTypeService;

    @PostMapping
    @ApiOperation(value = "新增能源类型")
    @SysLog("新增能源类型")
    public R<Integer> add(@Validated(InsertGroup.class) @RequestBody EpAddEnergyTypeDTO dto) {
        return R.success(epEnergyTypeService.add(dto));
    }

//    @GetMapping
//    @ApiOperation(value = "根据id查询能源类型")
//    public R<>
}
