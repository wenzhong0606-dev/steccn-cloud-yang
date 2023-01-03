package com.steccn.qms.controller;

import com.steccn.common.vo.R;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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

    @GetMapping("/online")
    @ApiOperation("质量日报表-在线检")
    public R online(@RequestParam("date") LocalDate date,
                    @RequestParam("workshopId") Integer workshopId,
                    @RequestParam("productLineId") Integer productLineId) {
        return R.success();
    }
}
