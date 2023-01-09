package com.steccn.data.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.data.vo.DwEpMeterCollectVO;
import com.steccn.data.dto.DwMeterReadDTO;
import com.steccn.data.dto.DwStationCollectDTO;
import com.steccn.data.vo.DwStationCollectVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yequan
 * @desc 数据中台服务
 */
@FeignClient(contextId = "remoteDataPlatformFeignApi", value = FeignConstant.STECCN_DATA_PLATFORM_SERVICE_NAME)
public interface RemoteDataPlatformFeignApi {


    /**
     * 能源绩效系统-抄表归档
     * @param dwList 数仓条件列表
     * @return list
     */
    @PostMapping("/feign/data/platform/meter/read")
    @ApiOperation("能源绩效系统-抄表归档")
    R<List<DwEpMeterCollectVO>> meterRead(@RequestBody List<DwMeterReadDTO> dwList);

    /**
     * 生产管理-进出站计数归档
     * @param dto 查询条件
     * @return list
     */
    @PostMapping("/feign/data/platform/station/collect")
    @ApiOperation("生产管理-进出站计数归档")
    R<List<DwStationCollectVO>> stationCollect(@RequestBody DwStationCollectDTO dto);

    /**
     * 最近抄表时间
     * @param distantCodeSet 远传仪表编码
     * @return map
     */
    @PostMapping("/feign/data/platform/meter/last/read/time")
    @ApiOperation("能源绩效-最近抄表时间")
    R<Map<String, LocalDateTime>> lastReadTime(@RequestBody Set<String> distantCodeSet);
}
