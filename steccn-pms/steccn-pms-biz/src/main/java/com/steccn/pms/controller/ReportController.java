package com.steccn.pms.controller;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.dto.QtLineChartBiBarDTO;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.vo.EpChartGraphVO;
import com.steccn.common.vo.R;
import com.steccn.oa.constant.OaConstant;
import com.steccn.platform.feign.RemoteSysOauthClientDetailFeignApi;
import com.steccn.pms.dto.EnergyCountDTO;
import com.steccn.pms.dto.OutputQualityDTO;
import com.steccn.pms.dto.PmsDayReportDTO;
import com.steccn.pms.service.ReportService;
import com.steccn.pms.vo.*;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * @author yequan
 * @desc 报表控制器
 */
@RestController
@RequestMapping("/pms/report")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportService reportService;

    private final RemoteSysOauthClientDetailFeignApi remoteSysOauthClientDetailFeignApi;

    @PostMapping("/sub/produce")
    @ApiOperation("基片日报表-产量")
    public R<SubstrateDayReportVO> substrateDayReport(@RequestBody PmsDayReportDTO dto) {
        return R.success(reportService.substrateDayReport(dto));
    }

    @PostMapping("/sub/ingredient")
    @ApiOperation("基片日报表-原料耗料")
    public R<List<SubstrateIngredientVO>> subIngredientReport(@RequestBody PmsDayReportDTO dto) {
        return R.success(reportService.subIngredientReport(dto));
    }

    @PostMapping("/fur/produce")
    @ApiOperation("深加工日报表-产量")
    public R<FurtherProduceReportVO> furtherProduceDayReport(@RequestBody PmsDayReportDTO dto) {
        return R.success(reportService.furtherProduceDayReport(dto));
    }

    @PostMapping("/fur/ingredient")
    @ApiOperation("深加工日报表-原料耗料")
    public R<FurtherIngredientReportVO> furtherIngredientDayReport(@RequestBody PmsDayReportDTO dto) {
        return R.success(reportService.furtherIngredientDayReport(dto));
    }

    @PostMapping("/consume")
    @ApiOperation("能耗水电油气")
    public R<EnergyCountVO> consume(@RequestBody EnergyCountDTO dto) {
        return R.success(reportService.consume(dto));
    }

    @PostMapping("/outputCount")
    @ApiOperation("产量统计")
    public R<OutputCountVO> outputCount(@Valid @RequestBody EnergyCountDTO dto) {
        return R.success(reportService.outputCount(dto));
    }

    @GetMapping("/orderProgress/chart/{workshopId}")
    @ApiOperation("订单进度跟踪图表")
    public R<QtLineChartBarDTO> orderProgress(@PathVariable("workshopId")Integer workshopId){
        return R.success(reportService.orderProgress(workshopId));
    }

    @PostMapping("/output/quality/jp")
    @ApiOperation("产量质量报表-基片")
    public R<List<OutputQualityVO>> outputQualityJp(@RequestBody OutputQualityDTO outputQualityDTO){
        return R.success(reportService.outputQualityJp(outputQualityDTO));
    }

    @PostMapping("/output/quality/sjg")
    @ApiOperation("产量质量报表-深加工")
    public R<List<OutputQualitySjgVO>> outputQualitySjg(@RequestBody OutputQualityDTO outputQualityDTO){
        return R.success(reportService.outputQualitySjg(outputQualityDTO));
    }

    @PostMapping("/thirdparty/output/quality/jp")
    @ApiOperation("三方产量质量报表-基片")
    public R<List<OutputQualityVO>> thirdPartyOutputQualityJp(HttpServletRequest request, @RequestBody OutputQualityDTO outputQualityDTO){
        validRequestHeader(request);
        return R.success(reportService.outputQualityJp(outputQualityDTO));
    }

    @PostMapping("/thirdparty/output/quality/sjg")
    @ApiOperation("三方产量质量报表-深加工")
    public R<List<OutputQualitySjgVO>> thirdPartyOutputQualitySjg(HttpServletRequest request, @RequestBody OutputQualityDTO outputQualityDTO){
        validRequestHeader(request);
        return R.success(reportService.outputQualitySjg(outputQualityDTO));
    }

    @GetMapping("/raw/material/workshop/histogram")
    @ApiOperation("原料车间生产驾驶舱-柱状图数据")
    public R<EpChartGraphVO<List<String>,List<TripleDTO<String, BigDecimal, String>>>> rawMaterialWorkshopHistogram(@RequestParam("startDate") LocalDate startDate,@RequestParam("endDate") LocalDate endDate){
        return R.success(reportService.rawMaterialWorkshopHistogram(startDate,endDate));
    }


    /**
     * 校验请求头信息
     *
     * @param request 请求
     */
    private void validRequestHeader(HttpServletRequest request) {
        List<String> pwdSet = FeignOptional.of(() -> remoteSysOauthClientDetailFeignApi.clientEncodePassword(), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        String clientId = request.getHeader(OaConstant.CLIENT_ID);
        Assert.isTrue(StrUtil.isNotBlank(clientId), "请求头中必须携带client-id");
        Assert.isTrue(pwdSet.contains(clientId), "非法访问，请携带正确的client-id");
    }
}
