package com.steccn.ems.controller;

import com.steccn.common.annotation.SysLog;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.vo.R;
import com.steccn.ems.dto.EpAddEnergyTypeDTO;
import com.steccn.ems.service.EpEnergyTypeService;
import com.steccn.ems.vo.EpEnergyTypeVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api("能源类型")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ems/energy/type")
public class EpEnergyTypeController {
    private final EpEnergyTypeService epEnergyTypeService;

    @PostMapping
    @ApiOperation(value = "新增能源类型")
    @SysLog("新增能源类型")
    public R<EpEnergyTypeVO> addOrEdit(@Validated(InsertGroup.class) @RequestBody EpAddEnergyTypeDTO dto) {
        return R.success(epEnergyTypeService.addOrEdit(dto));
    }

    @GetMapping
    @ApiOperation(value = "根据id查询能源类型")
    public R<EpEnergyTypeVO> getEpAddEnergyTypeVO(@PathVariable("id") Integer id){
        return R.success(epEnergyTypeService.getEpAddEnergyTypeVO(id));
    }

    @GetMapping("/selectAll")
    @ApiOperation(value = "查询能源列表")
    public R<List<EpEnergyTypeVO>> getEpAddEnergyTypeVOList(){
        return R.success(epEnergyTypeService.getEpAddEnergyTypeVOList());
    }
}
