package com.steccn.pms.controller.feign;

import cn.hutool.core.lang.Assert;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.dto.EpMeterReadRowDTO;
import com.steccn.pms.dto.ProcessReportWorkHourQmsDTO;
import com.steccn.pms.dto.SubstrateDataDTO;
import com.steccn.pms.service.PmsReportWorkService;
import com.steccn.pms.service.ReportService;
import com.steccn.pms.vo.PmsReportWorkVO;
import com.steccn.pms.vo.ProcessReportWorkHourVO;
import com.steccn.pms.vo.SubstrateDataListVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
@Slf4j
public class PmsReportWorkFeignController {

    private final PmsReportWorkService pmsReportWorkService;
    private final ReportService reportService;

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

    @ApiOperation("原料耗用量-周|产线")
    @PostMapping("/original/consume/week")
    R<Map<String, BigDecimal>> useCountWeek(@RequestBody TripleDTO<List<Integer>, LocalDateTime, LocalDateTime> tripleDTO) {
        return R.success(pmsReportWorkService.useCountWeek(tripleDTO.getField1(), tripleDTO.getField2(), tripleDTO.getField3()));
    }

    @ApiOperation("原料耗用量-月|产线")
    @PostMapping("/original/consume/month")
    R<Map<String, BigDecimal>> useCountMonth(@RequestBody TripleDTO<List<Integer>, LocalDateTime, LocalDateTime> tripleDTO) {
        return R.success(pmsReportWorkService.useCountMonth(tripleDTO.getField1(), tripleDTO.getField2(), tripleDTO.getField3()));
    }

    @ApiOperation("获取停工时长")
    @PostMapping("/hour")
    R<List<ProcessReportWorkHourVO>> getWorkHour(@RequestBody ProcessReportWorkHourQmsDTO dto) {
        return R.success(pmsReportWorkService.getReportWorkHour(dto));
    }

    @ApiOperation("获取停工时长")
    @PostMapping("/substrate")
    R<List<SubstrateDataListVO>> getSubstrateDataList(@RequestBody SubstrateDataDTO dto) {
        return R.success(reportService.substrateDataList(dto));
    }

    @ApiOperation("获取钢化工位的报工产量")
    @PostMapping("/ghgw/quantity")
    R<Map<Integer, BigDecimal>> getGhQuantity(@RequestBody TripleDTO<LocalDate, LocalDate, List<Integer>> tripleDTO) {
        return R.success(pmsReportWorkService.getGhQuantity(tripleDTO.getField1(), tripleDTO.getField2(), tripleDTO.getField3()));
    }

    @ApiOperation("按班次获取入库量(吨)")
    @PostMapping("/instorage/dateClassLine")
    R<Map<String, BigDecimal>> dateClassLineInStorageUnit(@RequestBody TripleDTO<LocalDate, LocalDate, List<Integer>> tripleDTO) {
        return R.success(pmsReportWorkService.dateClassLineInStorageUnit(tripleDTO.getField3(), tripleDTO.getField1(), tripleDTO.getField2(), PmManagerConstant.MEASURE_UNIT_TON));
    }
}
