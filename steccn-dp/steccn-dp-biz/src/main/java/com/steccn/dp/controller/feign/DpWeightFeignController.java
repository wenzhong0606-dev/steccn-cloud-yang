package com.steccn.dp.controller.feign;

import com.steccn.common.dto.SteccnTableDataDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.dp.domain.ExitStationCount;
import com.steccn.dp.dto.DpPmsOperateDTO;
import com.steccn.common.dto.CommonReportDTO;
import com.steccn.dp.service.*;
import com.steccn.dp.vo.DpNatureGasVO;
import com.steccn.dp.vo.DpOnOffCountVO;
import com.steccn.dp.vo.DpValueDateVO;
import com.steccn.dp.vo.DpWeightVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2023/11/23
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/feign/dp")
public class DpWeightFeignController {
    private final AdsOpcUaAll3Service adsOpcUaAll3Service;
    private final AdsOnOffCountService adsOnOffCountService;
    private final DpColdEndCheckpointValueService dpColdEndCheckpointValueService;
    private final NaturalGasFlowCountService naturalGasFlowCountService;
    private final ExitStationCountService exitStationCountService;
    private final RawNumService rawNumService;

    @PostMapping("/weight/count")
    @ApiOperation(value = "电子秤读数")
    public R<List<DpWeightVO>> count(@RequestBody TupleDTO<LocalDateTime,LocalDateTime> dto) {
        return R.success(adsOpcUaAll3Service.getWeighCount(dto));
    }

    @PostMapping("/weight/countMap")
    @ApiOperation(value = "电子秤读数Map")
    public R<Map<String, BigDecimal>> countMap(@RequestBody TupleDTO<LocalDateTime,LocalDateTime> dto) {
        return R.success(adsOpcUaAll3Service.getWeightCountMap(dto));
    }

    @PostMapping("/weightDetail")
    @ApiOperation(value = "获取原料配料详情")
    public R<SteccnTableDataDTO> weightDetail(@RequestBody TupleDTO<LocalDateTime,LocalDateTime> date){
        return R.success(adsOpcUaAll3Service.weightDetail(date));
    }

    @PostMapping("/weight/countAsDay")
    @ApiOperation(value = "电子秤读数按天")
    public R<List<DpValueDateVO>> countAsDay(@RequestBody DpPmsOperateDTO dto) {
        return R.success(adsOpcUaAll3Service.weightCountAsDay(dto));
    }

    @PostMapping("/weight/consume")
    @ApiOperation(value = "耗料数")
    public R<List<CommonReportDTO>> consumeMaterial(@RequestBody DpPmsOperateDTO dto) {
        return R.success(adsOpcUaAll3Service.countDTO(dto));
    }

    @PostMapping("/onOff/list")
    @ApiOperation(value = "玻璃片数光电信号")
    public R<List<DpOnOffCountVO>> onOffList(@RequestBody TupleDTO<LocalDate,LocalDate> dto) {
        return R.success(adsOnOffCountService.onOffList(dto));
    }

    @PostMapping("/coldEnd/deleteData")
    @ApiOperation(value = "只保留7天的数据")
    public R<Boolean> deleteData(@RequestBody TupleDTO<LocalDateTime,String> dto) {
        return R.success(dpColdEndCheckpointValueService.deleteData(dto.getField1()));
    }

    @PostMapping("/gas/data")
    @ApiOperation(value = "获取天然气数据")
    public R<List<DpNatureGasVO>> natureGasData(@RequestBody TupleDTO<LocalDateTime,LocalDateTime> dto) {
        return R.success(naturalGasFlowCountService.natureGas(dto));
    }

    @GetMapping("/weight/push")
    @ApiOperation(value = "推送电子秤读数")
    public R<Boolean> push() {
        return R.success(adsOpcUaAll3Service.pushWeightSum());
    }

    @GetMapping("/weather/push")
    @ApiOperation(value = "推送气象站数据")
    public R<Boolean> weatherPush() {
        return R.success(adsOpcUaAll3Service.pushTempData());
    }


    @PostMapping("/output")
    @ApiOperation(value = "产量")
    public R<Map<Long,BigDecimal>> output(@RequestBody Set<Long> orderIds){
        return R.success(exitStationCountService.output(orderIds));
    }

    @PostMapping("/weightSum")
    @ApiOperation(value = "获取原料配料聚合数据")
    public R<SteccnTableDataDTO> weightSum(@RequestBody TupleDTO<LocalDate,LocalDate> dto){
        return R.success(adsOpcUaAll3Service.weightSum(dto.getField1()));
    }

    @PostMapping("/weightMaterial")
    @ApiOperation(value = "获取白云石、纯碱、石灰石每天的入料量数据")
    public R<List<Map<LocalDate,BigDecimal>>> weightMaterial(@RequestBody DpPmsOperateDTO dto){
        return R.success(adsOpcUaAll3Service.queryListDay(dto));
    }

    @GetMapping("/weight/material/push")
    @ApiOperation(value = "推送电子秤读数")
    R<Boolean> materialPush(){
        return R.success(rawNumService.insertToday());
    }
}
