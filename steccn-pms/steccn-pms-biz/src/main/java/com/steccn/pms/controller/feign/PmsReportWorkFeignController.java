package com.steccn.pms.controller.feign;

import com.steccn.common.vo.R;
import com.steccn.pms.service.PmsReportWorkService;
import com.steccn.pms.vo.PmsReportWorkVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author chenhuan
 * @date 2022/12/9 10:45
 */
@RestController
@Api("报工feign")
@RequestMapping("/feign/pms/report/work")
@RequiredArgsConstructor
public class PmsReportWorkFeignController {

    private final PmsReportWorkService pmsReportWorkService;

    @GetMapping("/listByClassTime")
    @ApiOperation("根据班次时间查询报工信息")
    public R<List<PmsReportWorkVO>> listByClassTime(@RequestParam("startTime") LocalDateTime startTime,@RequestParam("endTime") LocalDateTime endTime) {
        return R.success(pmsReportWorkService.listByClassTime(startTime, endTime));
    }
}
