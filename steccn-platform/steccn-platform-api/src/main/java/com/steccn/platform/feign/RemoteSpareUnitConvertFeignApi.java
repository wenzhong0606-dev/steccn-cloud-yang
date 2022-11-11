package com.steccn.platform.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.platform.vo.SysSpareUnitConversionVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yequan
 */
@FeignClient(contextId = "remoteSpareUnitConvertFeignApi", value = FeignConstant.STECCN_PLATFORM_SERVICE_NAME)
public interface RemoteSpareUnitConvertFeignApi {

    /**
     * 单位转换
     *
     * @param spareSet 物料id集合
     * @return
     */
    @PostMapping("/feign/platform/spare/convert/unit")
    @ApiOperation("单位转换")
    R<Map<Long, List<SysSpareUnitConversionVO>>> convert(@RequestBody Set<Long> spareSet);
}
