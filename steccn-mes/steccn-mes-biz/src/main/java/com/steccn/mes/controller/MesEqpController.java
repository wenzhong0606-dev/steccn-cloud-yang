package com.steccn.mes.controller;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.dto.SteccnTableDataDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.mes.dto.EqpOperationReportDTO;
import com.steccn.mes.dto.MesDefectChartDTO;
import com.steccn.mes.dto.MesInsTaskTmpParamDTO;
import com.steccn.mes.service.MesDefectService;
import com.steccn.mes.service.MesEqpInsTaskService;
import com.steccn.mes.service.MesEqpReportService;
import com.steccn.mes.service.MesEquipmentService;
import com.steccn.mes.vo.EqpOperationReportVO;
import com.steccn.mes.vo.MesEqpVO;
import com.steccn.mes.vo.MesItemIdNameVO;
import com.steccn.mes.vo.YlEqpDashboardVO;
import com.steccn.oa.constant.OaConstant;
import com.steccn.platform.feign.RemoteSysOauthClientDetailFeignApi;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 获取设备信息
 *
 * @author AndrewLuo
 * @date 2023/09/01
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/mes/eqp")
@Slf4j
public class MesEqpController {
    private final MesEquipmentService mesEquipmentService;
    private final RemoteSysOauthClientDetailFeignApi remoteSysOauthClientDetailFeignApi;
    private final MesEqpInsTaskService mesEqpInsTaskService;
    private final MesDefectService mesDefectService;
    private final MesEqpReportService mesEqpReportService;

    @GetMapping("/thirdParty")
    @ApiOperation(value = "获取设备信息")
    public R<List<MesEqpVO>> getEqpInfo(HttpServletRequest request) {
        validRequestHeader(request);
        return R.success(mesEquipmentService.getEqpInfo());
    }

    @GetMapping("/dashboard/task")
    @ApiOperation(value = "点检任务图表")
    public R<QtLineChartBarDTO> taskCompleteChart(@RequestParam("startTime")LocalDateTime startTime,@RequestParam("endTime") LocalDateTime endTime, @RequestParam("type")Integer type) {
        return R.success(mesEqpInsTaskService.taskComplete(startTime,endTime,type));
    }

    @GetMapping("/dashboard/item")
    @ApiOperation(value = "点检项图表")
    public R<QtLineChartBarDTO> itemCompleteChart(@RequestParam("startTime")LocalDateTime startTime,@RequestParam("endTime") LocalDateTime endTime, @RequestParam("type")Integer type) {
        return R.success(mesEqpInsTaskService.itemComplete(startTime,endTime,type));
    }

    @PostMapping("/dashboard/defect")
    @ApiOperation(value = "缺陷图表")
    public R<QtLineChartBarDTO> defectChart(@RequestBody @Valid MesDefectChartDTO dto) {
        return R.success(mesDefectService.defectChart(dto));
    }

    @GetMapping("/dashboard/temperature/item")
    @ApiOperation(value = "获取温度项")
    public R<List<MesItemIdNameVO>> temperatureItem() {
        return R.success(mesEqpInsTaskService.tmpItemIdAndName());
    }

    @PostMapping("/dashboard/temperature/report")
    @ApiOperation(value = "获取温度项报表")
    public R<SteccnTableDataDTO> temperatureReport(@RequestBody @Valid MesInsTaskTmpParamDTO dto) {
        return R.success(mesEqpInsTaskService.tempReport(dto));
    }

    @PostMapping("/dashboard/temperature/lineChart")
    @ApiOperation(value = "获取温度项图表")
    public R<QtLineChartBarDTO> tempLineChart(@RequestBody @Valid MesInsTaskTmpParamDTO dto) {
        return R.success(mesEqpInsTaskService.tempLineChart(dto));
    }

    @PostMapping("/dashboard/yl")
    @ApiOperation(value = "原料图表")
    public R<YlEqpDashboardVO> ylDashboard(@RequestBody TupleDTO<LocalDate,LocalDate> dto) {
        return R.success(mesEquipmentService.dashBoard(dto));
    }

    @PostMapping("/operation/report")
    @ApiOperation(value = "获取设备运行报表")
    public R<Page<EqpOperationReportVO>> getOperationReport(@RequestBody EqpOperationReportDTO dto) {
        return R.success(mesEqpReportService.queryReport(dto));
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
