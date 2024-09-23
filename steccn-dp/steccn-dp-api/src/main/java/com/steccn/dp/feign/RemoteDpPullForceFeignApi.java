package com.steccn.dp.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.dp.dto.DpPullForceDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author yequan
 * @desc 数据中台 - 拉引量
 * @date 2023/12/1
 */
@FeignClient(contextId = "RemoteDpPullForceFeignApi", value = FeignConstant.STECCN_DP_SERVICE_NAME)
public interface RemoteDpPullForceFeignApi {

    /**
     * 拉引量
     *
     * @param dto dto
     * @return
     */
    @PostMapping("/feign/dp/pull/force/sum")
    @ApiOperation(value = "拉引量汇总")
    R<Map<String, BigDecimal>> pullForceSum(@RequestBody DpPullForceDTO dto);

    @GetMapping("/feign/dp/pull/force/push")
    @ApiOperation(value = "给卡夫卡推送理论拉引量数据")
    R<Boolean> pushPullForce();
}
