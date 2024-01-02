package com.steccn.dp.controller;

import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.dto.SteccnTableDataDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.dp.dto.DpPmsOperateDTO;
import com.steccn.dp.dto.WeightCountDTO;
import com.steccn.dp.service.AdsOpcUaAll3Service;
import com.steccn.dp.vo.DpWeightVO;
import groovy.lang.Tuple;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2023/11/23
 */
@Api("备件信息")
@RestController
@RequiredArgsConstructor
@RequestMapping("/dp/weight")
public class DpWeightController {
    private final AdsOpcUaAll3Service adsOpcUaAll3Service;

    @PostMapping("/count")
    @ApiOperation(value = "电子秤读数")
    public R<List<DpWeightVO>> count(@RequestBody TupleDTO<LocalDateTime,LocalDateTime> dto) {
        return R.success(adsOpcUaAll3Service.getWeighCount(dto));
    }

    @PostMapping("/count/date")
    @ApiOperation(value = "电子秤读数按日期")
    public R<List<DpWeightVO>> countDate(@RequestBody TupleDTO<LocalDate,LocalDate> dto) {
        return R.success(adsOpcUaAll3Service.getWeighCountDate(dto));
    }

    @PostMapping("/dashboard")
    @ApiOperation(value = "电子秤读数图表")
    public R<QtLineChartBarDTO> dashBoard(@RequestBody DpPmsOperateDTO dto) {
        return R.success(adsOpcUaAll3Service.weightCountDashBoard(dto));
    }

    @GetMapping("/weightDetail")
    @ApiOperation(value = "获取原料配料详情")
    public R<SteccnTableDataDTO> weightDetail(@RequestParam("date") LocalDate date){
        return R.success(adsOpcUaAll3Service.weightDetail(date));
    }

    @PostMapping("/test")
    @ApiOperation(value = "电子秤读数图表")
    public R<List<WeightCountDTO>> test(@RequestBody DpPmsOperateDTO dto) {
        return R.success(adsOpcUaAll3Service.countDTO(dto));
    }
}
