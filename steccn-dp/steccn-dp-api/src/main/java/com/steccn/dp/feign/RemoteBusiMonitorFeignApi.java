package com.steccn.dp.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.dp.dto.DpBusiMonitorDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author yequan
 * @desc 数据业务监控
 * @date 2024/10/22
 */
@FeignClient(contextId = "remoteBusiMonitorFeignApi", value = FeignConstant.STECCN_DP_SERVICE_NAME)
public interface RemoteBusiMonitorFeignApi {

    @GetMapping("/feign/dp/busi/monitor/alarmCode")
    @ApiOperation(value = "根据报警类型获取报警配置")
    R<List<DpBusiMonitorDTO>> getBusiMonitorAlarm(@RequestParam("alarmCode") String alarmCode);

    @GetMapping("/feign/dp/busi/monitor/alarmCode")
    @ApiOperation(value = "根据报警类型获取报警配置")
    R<List<DpBusiMonitorDTO>> getBusiMonitorAlarm(@RequestParam("alarmCode") String alarmCode, @RequestParam(value = "granularity", required = false) Integer granularity);

}
