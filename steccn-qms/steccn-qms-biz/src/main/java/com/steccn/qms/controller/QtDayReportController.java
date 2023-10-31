package com.steccn.qms.controller;

import com.steccn.common.vo.R;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.dto.QtDayReportOnlineDTO;
import com.steccn.qms.dto.QtLossReportDTO;
import com.steccn.qms.dto.QtReportFirstPatrolDTO;
import com.steccn.qms.service.QtDayReportService;
import com.steccn.qms.vo.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * @author yequan
 * @desc 质量日报表-控制器
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/qms/day/report")
@Slf4j
@ApiModel("质量日报表-控制器")
public class QtDayReportController {

    private final QtDayReportService qtDayReportService;

    @PostMapping("/online/man")
    @ApiOperation("质量日报表-在线检人工")
    public R<QtDayReportOnlineVO> onlineMan(@RequestBody @Validated QtDayReportOnlineDTO dto) {
        return R.success(qtDayReportService.online(dto.getDate(), dto.getProductLineIdList(), QtManagerConstant.CONF_QMS_COLLECT_DISABLE_CONTENT));
    }

    @PostMapping("/online")
    @ApiOperation("质量日报表-在线检")
    public R<QtDayReportOnlineVO> online(@RequestBody @Validated QtDayReportOnlineDTO dto) {
        return R.success(qtDayReportService.online(dto.getDate(), dto.getProductLineIdList(), QtManagerConstant.CONF_QMS_COLLECT_ENABLE_CONTENT));
    }

    @PostMapping("/first")
    @ApiOperation("质量日报表-首检2/巡检4")
    public R<QtDayReportFirstVO> first(@RequestBody @Validated QtReportFirstPatrolDTO dto) {
        return R.success(qtDayReportService.first(dto));
    }


    @PostMapping("/polling/defect/item")
    @ApiOperation("质量日报表-巡检-缺陷项")
    public R<QtDayPollingFirstVO> pollingDefectItem(@RequestBody @Validated QtReportFirstPatrolDTO dto) {
        return R.success(qtDayReportService.pollingDefectItem(dto));
    }

    @GetMapping("/sampling")
    @ApiOperation("配料抽检日报表")
    public R<QtDaySamplingVO> sampling(@RequestParam("date") LocalDate date) {
        return R.success(qtDayReportService.sampling(date));
    }

    @PostMapping("/loss")
    @ApiOperation("损耗日报表")
    public R<QtDayReportLossVO> loss(@RequestBody @Validated QtLossReportDTO dto) {
        return R.success(qtDayReportService.loss(dto));
    }

}
