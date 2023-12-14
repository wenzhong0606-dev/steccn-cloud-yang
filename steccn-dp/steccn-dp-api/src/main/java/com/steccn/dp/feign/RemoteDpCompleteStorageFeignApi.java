package com.steccn.dp.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.dp.dto.DpPullForceDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.math.BigDecimal;
import java.util.Map;

/**
 * @author yequan
 * @desc 数据中台 - 入库
 * @date 2023/12/14
 */
@FeignClient(contextId = "RemoteDpCompleteStorageFeignApi", value = FeignConstant.STECCN_DP_SERVICE_NAME)
public interface RemoteDpCompleteStorageFeignApi {

    /**
     * 拉引量
     *
     * @param dto dto
     * @return
     */
    @PostMapping("/feign/dp/in/storage/sum")
    @ApiOperation(value = "入库汇总")
    R<Map<String, BigDecimal>> inStorageSum(@RequestBody DpPullForceDTO dto);
}
