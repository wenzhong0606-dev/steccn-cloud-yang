package com.steccn.ems.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.ems.dto.EpUnitConsumeAlarmDTO;
import com.steccn.ems.service.EpUnitConsumeAlarmService;
import com.steccn.ems.vo.EpUnitConsumeAlarmVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * @author chenhuan
 * @date 2022/11/30 15:38
 */
@Api("单耗告警")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ems/unit/consume/alarm")
public class EpUnitConsumeAlarmController {

    private final EpUnitConsumeAlarmService epUnitConsumeAlarmService;

    @PostMapping("/page")
    @ApiOperation(value = "单耗告警列表")
    public R<Page<EpUnitConsumeAlarmVO>> list(@RequestBody EpUnitConsumeAlarmDTO dto) {
        return R.success(epUnitConsumeAlarmService.page(dto));
    }

    @GetMapping("/trigger")
    @ApiOperation(value = "单耗告警-触发")
    public R<Boolean> trigger(@RequestParam("classDate") LocalDate classDate){
        return R.success(epUnitConsumeAlarmService.trigger(classDate));
    }
}
