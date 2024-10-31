package com.steccn.dp.controller.feign;

import com.steccn.common.dto.TupleDTO;
import com.steccn.common.dto.ValidList;
import com.steccn.common.vo.R;
import com.steccn.dp.dto.DpBusiMonitorDTO;
import com.steccn.dp.service.TbBusiMonitorService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author yequan
 * @desc 业务监控
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/feign/dp/busi/monitor")
public class DpBusiMonitorFeignController {

    private final TbBusiMonitorService busiMonitorService;

    @GetMapping("/alarmCode")
    @ApiOperation(value = "根据报警类型获取报警配置")
    public R<List<DpBusiMonitorDTO>> getBusiMonitorAlarm(@RequestParam("alarmCode") String alarmCode, @RequestParam(value = "granularity", required = false) Integer granularity) {
        return R.success(busiMonitorService.getBusiMonitorAlarm(alarmCode, granularity));
    }
}
