package com.steccn.data.controller.feign;

import cn.hutool.core.bean.BeanUtil;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.data.domain.EpMeterCollect;
import com.steccn.data.domain.PmStationCollect;
import com.steccn.data.dto.*;
import com.steccn.data.service.EpMeterCollectService;
import com.steccn.data.service.PmStationCollectService;
import com.steccn.data.service.QtDefectCollectService;
import com.steccn.data.vo.DwDefectCollectVO;
import com.steccn.data.vo.DwEpMeterCollectVO;
import com.steccn.data.vo.DwStationCollectVO;
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
 * @author yequan
 * @desc 数据中台服务
 */
@Api("数据中台服务")
@RestController
@RequiredArgsConstructor
@RequestMapping("/feign/data/warehouse")
public class DataPlatformFeignController {

    private final EpMeterCollectService epMeterCollectService;
    private final PmStationCollectService pmStationCollectService;
    private final QtDefectCollectService qtDefectCollectService;

    @PostMapping("/meter/read")
    @ApiOperation(value = "数仓条件列表")
    public R<DwEpMeterCollectVO> meterRead(@RequestBody DwMeterReadDTO dto) {
        return R.success(epMeterCollectService.meterRead(dto));
    }

    @PostMapping("/meter/read/seValue")
    @ApiOperation("能源绩效系统-抄表归档-差值")
    R<List<BigDecimal>> seValue(@RequestBody TripleDTO<String, LocalDateTime, LocalDateTime> tripleDTO) {
        return R.success(epMeterCollectService.seValue(tripleDTO));
    }

    @PostMapping("/meter/read/diff")
    @ApiOperation(value = "数仓条件列表-一块仪表，2个时间点的差值")
    public R<BigDecimal> meterReadDiff(@RequestBody DwMeterReadDiffDTO dto) {
        return R.success(epMeterCollectService.meterReadDiff(dto));
    }

    @PostMapping("/meter/last/read/time")
    @ApiOperation(value = "最近抄表时间")
    R<Map<String, LocalDateTime>> lastReadTime(@RequestBody Set<String> distantCodeSet) {
        return R.success(epMeterCollectService.lastReadTime(distantCodeSet));
    }

    @PostMapping("/station/collect")
    @ApiOperation("生产管理-进出站计数归档")
    R<List<DwStationCollectVO>> stationCollect(@RequestBody DwStationCollectDTO dto) {
        return R.success(pmStationCollectService.stationCollect(dto));
    }

    @PostMapping("/defect")
    @ApiOperation("质量管理-缺陷项")
    R<List<DwDefectCollectVO>> collectDetail(@RequestBody TripleDTO<LocalDateTime, LocalDateTime, Set<String>> tripleDTO) {
        return R.success(qtDefectCollectService.collectDetail(tripleDTO.getField1(), tripleDTO.getField2(), tripleDTO.getField3()));
    }

    @PostMapping("/report/work/preload")
    @ApiOperation("生产管理-报工数值预填")
    R<BigDecimal> preload(@RequestBody PmReportWorkPreloadDTO dto) {
        return R.success(pmStationCollectService.rwPreLoad(dto));
    }

    @PostMapping("/deal/data")
    @ApiOperation("迁移数据")
    R<Boolean> dealData(@RequestBody List<EpMeterCollectDTO> dto) {
        List<EpMeterCollect> epMeterCollects = BeanUtil.copyToList(dto, EpMeterCollect.class);
        boolean save = epMeterCollectService.saveBatch(epMeterCollects);
        return R.success(save);
    }

    @PostMapping("/psc/deal/data")
    @ApiOperation("迁移数据pm_station_collect")
    R<Boolean> pmStationCollectDealData(@RequestBody List<PmStationCollectDTO> list){
        List<PmStationCollect> resList = BeanUtil.copyToList(list, PmStationCollect.class);
        boolean save = pmStationCollectService.saveBatch(resList);
        return R.success(save);
    }

    @PostMapping("/station/info")
    @ApiOperation("生产管理-进出站计数信息")
    R<List<DwStationCollectVO>> stationInfo(@RequestBody DwStationInfoDTO dto) {
        return R.success(pmStationCollectService.stationInfo(dto));
    }

    @PostMapping("/feign/data/warehouse/heat/power")
    @ApiOperation("计算余热发电")
    R<List<BigDecimal>> heatPower(@RequestBody TupleDTO<LocalDate,LocalDate> tupleDTO){
        return R.success(epMeterCollectService.heatPower(tupleDTO.getField1(),tupleDTO.getField2()));
    }
}
