package com.steccn.ems.controller;

import com.steccn.common.vo.R;
import com.steccn.ems.service.EpPartitionService;
import com.steccn.ems.vo.EpEnergyTypeVO;
import com.steccn.ems.vo.EpPartitionVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Api("能耗分区")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ems/energy/partition")
public class EpPartitionController {
    private final EpPartitionService epPartitionService;

    @GetMapping("/{energyType}")
    @ApiOperation(value = "根据能源类型id查询能耗分区树列表")
    public R<Map<String,List<EpPartitionVO>>> getEpEnergyTypeVOList(@RequestParam("energyType") Integer energyType){
        return R.success(epPartitionService.getEpEnergyTypeVOList(energyType));
    }

}
