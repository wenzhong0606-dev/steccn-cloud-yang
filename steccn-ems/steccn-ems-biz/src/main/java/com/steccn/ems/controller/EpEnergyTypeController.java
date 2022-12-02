package com.steccn.ems.controller;

import com.steccn.common.annotation.SysLog;
import com.steccn.common.vo.R;
import com.steccn.ems.dto.EpEnergyTypeDTO;
import com.steccn.ems.service.EpEnergyTypeService;
import com.steccn.ems.vo.EpEnergyTypeVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Api("能源类型")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ems/energy/type")
public class EpEnergyTypeController {
    private final EpEnergyTypeService epEnergyTypeService;

    @PostMapping
    @ApiOperation(value = "新增或编辑能源类型")
    @SysLog("新增或编辑能源类型")
    public R<Boolean> addOrEdit(@Valid @RequestBody EpEnergyTypeDTO dto) {
        return R.success(epEnergyTypeService.addOrEdit(dto));
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "根据id查询能源类型")
    public R<EpEnergyTypeVO> getEpAddEnergyTypeVO(@PathVariable("id") Integer id){
        return R.success(epEnergyTypeService.getEpAddEnergyTypeVO(id));
    }

    @GetMapping("/selectAll")
    @ApiOperation(value = "查询能源列表")
    public R<List<EpEnergyTypeVO>> getEpEnergyTypeVOList(){
        return R.success(epEnergyTypeService.getEpEnergyTypeVOList());
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除能源类型")
    @SysLog("删除能源类型")
    public R<Boolean> delete(@PathVariable("id") Integer id){
        return R.success(epEnergyTypeService.delete(id));
    }

    @GetMapping("/linkTable/{code}")
    @ApiOperation(value = "根据关联图表的code查询能源类型id以及能源单位")
    public R<Map<Integer,EpEnergyTypeVO>> getBylinkTable(@PathVariable("code") Integer code){
        return R.success(epEnergyTypeService.getBylinkTable(code));
    }
}
