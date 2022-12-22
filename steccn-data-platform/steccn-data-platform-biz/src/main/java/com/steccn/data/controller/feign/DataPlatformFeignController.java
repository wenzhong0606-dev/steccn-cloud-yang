package com.steccn.data.controller.feign;

import com.steccn.common.vo.R;
import com.steccn.data.dto.DwStationCollectDTO;
import com.steccn.data.service.PmStationCollectService;
import com.steccn.data.vo.DwEpMeterCollectVO;
import com.steccn.data.dto.DwMeterReadDTO;
import com.steccn.data.service.EpMeterCollectService;
import com.steccn.data.vo.DwStationCollectVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

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

    @PostMapping("/meter/read")
    @ApiOperation(value = "数仓条件列表")
    public R<List<DwEpMeterCollectVO>> meterRead(@RequestBody List<DwMeterReadDTO> dwList) {
        return R.success(epMeterCollectService.meterRead(dwList));
    }

    @PostMapping("/station/collect")
    @ApiOperation("生产管理-进出站计数归档")
    R<List<DwStationCollectVO>> stationCollect(@RequestBody DwStationCollectDTO dto){
        return R.success(pmStationCollectService.stationCollect(dto));
    }
}
