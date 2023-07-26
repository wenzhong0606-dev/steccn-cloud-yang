package com.steccn.pms.controller.feign;

import cn.hutool.core.lang.Assert;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.EpMeterReadRowDTO;
import com.steccn.pms.service.PmsReportWorkService;
import com.steccn.pms.vo.PmsReportWorkVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    public R<List<PmsReportWorkVO>> listByClassTime(@RequestBody TupleDTO<LocalDateTime, LocalDateTime> tupleDTO) {
        Assert.notNull(tupleDTO.getField1(), "开始时间不能为空");
        Assert.notNull(tupleDTO.getField2(), "结束时间不能为空");
        return R.success(pmsReportWorkService.listByClassTime(tupleDTO.getField1(), tupleDTO.getField2()));
    }

    @ApiOperation("报工班组信息")
    @PostMapping("/class/group")
    R<Map<Long, Long>> reportWorkClassGroup(@RequestBody Set<Long> reportWorkIdSet) {
        return R.success(pmsReportWorkService.reportWorkClassGroup(reportWorkIdSet));
    }

    @ApiOperation("获取深加工产线-订单的报工记录")
    @PostMapping("/sjg/order/rw")
    R<List<EpMeterReadRowDTO>> sjgOrderRw(@RequestBody List<EpMeterReadRowDTO> sjgDataList) {
        return R.success(pmsReportWorkService.sjgOrderRw(sjgDataList));
    }

    @ApiOperation("获取指定日期，指定产线的所有产量")
    @PostMapping("/num/line/date")
    R<Map<String, BigDecimal>> classDateProductNum(@RequestBody TripleDTO<Set<Integer>, LocalDate, LocalDate> dto) {
        return R.success(pmsReportWorkService.classDateProductNum(dto.getField1(), dto.getField2(), dto.getField3()));
    }

    @ApiOperation("获取车间班次日期中班次报工最多的班组")
    @PostMapping("/workshop/class/group")
    R<Map<String, Long>> workshopClassGroupMap(@RequestBody TupleDTO<Set<LocalDate>, Map<Integer, Set<Integer>>> tupleDTO) {
        return R.success(pmsReportWorkService.workshopClassGroupMap(tupleDTO.getField1(), tupleDTO.getField2()));
    }

    @ApiOperation("原料耗用量")
    @PostMapping("/original/consume")
    R<Map<String, BigDecimal>> getUseCount(@RequestBody TripleDTO<List<Integer>, LocalDateTime, LocalDateTime> tripleDTO) {
        return R.success(pmsReportWorkService.getUseCount(tripleDTO.getField1(), tripleDTO.getField2(), tripleDTO.getField3()));
    }

    @ApiOperation("原料耗用量-每个订单")
    @PostMapping("/original/consume/perOrder")
    R<Map<String, BigDecimal>> useCountPerOrder(@RequestBody TripleDTO<List<Integer>, LocalDateTime, LocalDateTime> tripleDTO) {
        return R.success(pmsReportWorkService.useCountPerOrder(tripleDTO.getField1(), tripleDTO.getField2(), tripleDTO.getField3()));
    }
}
