package com.steccn.data.controller.feign;

import com.steccn.common.dto.TripleDTO;
import com.steccn.common.vo.R;
import com.steccn.data.dto.DwMeterReadDTO;
import com.steccn.data.dto.DwStationCollectDTO;
import com.steccn.data.dto.PmReportWorkPreloadDTO;
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
@RequestMapping("feign/data/platform")
public class DataPlatformFeignController {

    private final EpMeterCollectService epMeterCollectService;
    private final PmStationCollectService pmStationCollectService;
    private final QtDefectCollectService qtDefectCollectService;

    @PostMapping("/meter/read")
    @ApiOperation(value = "数仓条件列表")
    public R<List<DwEpMeterCollectVO>> meterRead(@RequestBody List<DwMeterReadDTO> dwList) {
        return R.success(epMeterCollectService.meterRead(dwList));
    }

    @PostMapping("/meter/last/read/time")
    @ApiOperation(value = "最近抄表时间")
    R<Map<String, LocalDateTime>> lastReadTime(@RequestBody Set<String> distantCodeSet){
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
}
