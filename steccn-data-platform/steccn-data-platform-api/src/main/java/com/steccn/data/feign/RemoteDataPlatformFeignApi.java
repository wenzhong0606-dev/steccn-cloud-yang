package com.steccn.data.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.data.dto.DwConditionDTO;
import com.steccn.data.dto.DwEpMeterCollectVO;
import com.steccn.data.dto.DwMeterReadDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author yequan
 * @desc 数据中台服务
 */
@FeignClient(contextId = "remoteDataPlatformFeignApi", value = FeignConstant.STECCN_DATA_PLATFORM_SERVICE_NAME)
public interface RemoteDataPlatformFeignApi {


    /**
     * 抄表归档
     * @param dwList 数仓条件列表
     * @return list
     */
    @PostMapping("/feign/data/platform/meter/read")
    @ApiOperation("抄表归档")
    R<List<DwEpMeterCollectVO>> meterRead(@RequestBody List<DwMeterReadDTO> dwList);
}
