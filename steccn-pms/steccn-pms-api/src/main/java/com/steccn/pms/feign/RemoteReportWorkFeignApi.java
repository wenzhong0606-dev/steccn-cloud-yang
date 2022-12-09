package com.steccn.pms.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.pms.vo.PmsReportWorkVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author chenhuan
 * @date 2022/12/9 11:11
 */
@FeignClient(contextId = "RemoteReportWorkFeignApi", value = FeignConstant.STECCN_PMS_SERVICE_NAME)
public interface RemoteReportWorkFeignApi {

    @GetMapping("/feign/pms/report/work/listByClassTime")
    @ApiOperation("根据班次时间查询报工信息")
    R<List<PmsReportWorkVO>> listByClassTime(LocalDateTime startTime, LocalDateTime endTime);
}
