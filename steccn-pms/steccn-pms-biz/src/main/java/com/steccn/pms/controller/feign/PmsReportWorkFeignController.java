package com.steccn.pms.controller.feign;

import cn.hutool.core.lang.Assert;
import com.google.common.collect.Maps;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.EpMeterReadRowDTO;
import com.steccn.pms.service.PmsReportWorkService;
import com.steccn.pms.vo.PmsReportWorkVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    @PostMapping("/listByClassTime")
    @ApiOperation("根据班次时间查询报工信息")
    public R<List<PmsReportWorkVO>> listByClassTime(@RequestBody TupleDTO<LocalDateTime,LocalDateTime> tupleDTO) {
        Assert.notNull(tupleDTO.getField1(),"开始时间不能为空");
        Assert.notNull(tupleDTO.getField2(),"结束时间不能为空");
        return R.success(pmsReportWorkService.listByClassTime(tupleDTO.getField1(), tupleDTO.getField2()));
    }

    @ApiOperation("报工班组信息")
    @PostMapping("/class/group")
    R<Map<Long, Long>> reportWorkClassGroup(@RequestBody Set<Long> reportWorkIdSet) {
        return R.success(pmsReportWorkService.reportWorkClassGroup(reportWorkIdSet));
    }

    @ApiOperation("获取深加工产线-订单的报工记录")
    @PostMapping("/sjg/order/rw")
    R<List<EpMeterReadRowDTO>> sjgOrderRw(@RequestBody List<EpMeterReadRowDTO> sjgDataList){
        return R.success(pmsReportWorkService.sjgOrderRw(sjgDataList));
    }

}
