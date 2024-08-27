package com.steccn.dp.controller;

import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.dto.SteccnTableDataDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.dp.dto.DpPmsOperateDTO;
import com.steccn.common.dto.CommonReportDTO;
import com.steccn.dp.service.AdsOnOffCountService;
import com.steccn.dp.service.AdsOpcUaAll3Service;
import com.steccn.dp.vo.DpOnOffCountVO;
import com.steccn.dp.vo.DpValueDateVO;
import com.steccn.dp.vo.DpWeightVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
    private final AdsOnOffCountService adsOnOffCountService;

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

    @PostMapping("/weightDetail")
    @ApiOperation(value = "获取原料配料详情")
    @PreAuthorize("hasAuthority('pms:raw:detail')")
    public R<SteccnTableDataDTO> weightDetail(@RequestBody TupleDTO<LocalDateTime,LocalDateTime> date){
        return R.success(adsOpcUaAll3Service.weightDetail(date));
    }

    @GetMapping("/weightSum")
    @ApiOperation(value = "获取原料配料聚合数据")
    @PreAuthorize("hasAuthority('pms:raw:count')")
    public R<SteccnTableDataDTO> weightSum(@RequestParam("date") LocalDate date){
        return R.success(adsOpcUaAll3Service.weightSum(date));
    }

    @PostMapping("/test")
    @ApiOperation(value = "电子秤读数图表")
    public R<List<CommonReportDTO>> test(@RequestBody DpPmsOperateDTO dto) {
        return R.success(adsOpcUaAll3Service.countDTO(dto));
    }

    @PostMapping("/test2")
    @ApiOperation(value = "玻璃片数光电信号")
    public R<List<DpOnOffCountVO>> onOffList(@RequestBody TupleDTO<LocalDate,LocalDate> dto) {
        return R.success(adsOnOffCountService.onOffList(dto));
    }

    @PostMapping("/test3")
    @ApiOperation(value = "电子秤读数图表")
    public R<List<DpValueDateVO>> test3(@RequestBody DpPmsOperateDTO dto) {
        return R.success(adsOpcUaAll3Service.weightCountAsDay(dto));
    }

}
