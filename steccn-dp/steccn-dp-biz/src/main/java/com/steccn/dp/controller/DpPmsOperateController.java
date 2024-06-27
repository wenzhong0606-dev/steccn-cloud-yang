package com.steccn.dp.controller;

import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.dp.dto.DpPmsOperateDTO;
import com.steccn.dp.service.*;
import com.steccn.dp.vo.DpPmsOperateCompleteStorageVO;
import com.steccn.dp.vo.DpSalesContractVO;
import com.steccn.dp.vo.DpTheoryPullForceVO;
import com.steccn.ems.vo.EpDeepConsumeContrastVO;
import com.steccn.qms.constant.QtManagerConstant;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;

/**
 * @author yequan
 * @desc 生产驾驶舱运营看板
 * @date 2023/11/29
 */
@Api("备件信息")
@RestController
@RequiredArgsConstructor
@RequestMapping("/dp/pms/operate")
public class DpPmsOperateController {

    private final DpPmsOperateService dpPmsOperateService;
    private final AdsOpcUaAll3Service adsOpcUaAll3Service;
    private final PullProduParCountService pullProduParCountService;
    private final PullProduParDetail1Service pullProduParDetail1Service;
    private final PullProduParDetail2Service pullProduParDetail2Service;

    @PostMapping("/in/storage")
    @ApiOperation(value = "总入库数，入库合格率")
    public R<DpPmsOperateCompleteStorageVO> inStorage(@Valid @RequestBody DpPmsOperateDTO dto) {
        return R.success(dpPmsOperateService.inStorage(dto));
    }

    @PostMapping("/complete")
    @ApiOperation(value = "总完工数，完工合格率")
    public R<DpPmsOperateCompleteStorageVO> complete(@Valid @RequestBody DpPmsOperateDTO dto) {
        return R.success(dpPmsOperateService.complete(dto));
    }

    @PostMapping("/salesContract")
    @ApiOperation(value = "销售系统")
    public R<DpSalesContractVO> salesContract(@RequestBody TupleDTO<LocalDate,LocalDate> dto) {
        return R.success(adsOpcUaAll3Service.salesContract(dto));
    }

    @GetMapping("/theoryPullForce")
    @ApiOperation(value = "理论拉引量")
    public R<DpTheoryPullForceVO> theoryPullForce(@RequestParam("date")LocalDate date){
        return R.success(pullProduParCountService.getPullForce(date));
    }

    @GetMapping("/speedTrend")
    @ApiOperation(value = "拉引速度趋势")
    public R<EpDeepConsumeContrastVO> getSpeedTrend(@RequestParam("date")LocalDate date, @RequestParam("productLine")Integer productLine){
        return R.success(pullProduParDetail1Service.getSpeedTrend(date,productLine));
    }

    @GetMapping("/thickTrend")
    @ApiOperation(value = "板宽趋势")
    public R<EpDeepConsumeContrastVO> getThickTrend(@RequestParam("date")LocalDate date, @RequestParam("productLine")Integer productLine){
        return R.success(pullProduParDetail2Service.getThickTrend(date,productLine));
    }

}
