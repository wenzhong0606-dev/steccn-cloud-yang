package com.steccn.pms.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.ProducePassRateDTO;
import com.steccn.pms.vo.ProducePassRateVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author chenhuan
 * @date 2022/11/10 15:01
 */
@FeignClient(contextId = "remotePassRateFeignApi", value = FeignConstant.STECCN_PMS_SERVICE_NAME)
public interface RemotePassRateFeignApi {

    /**
     * 根据分组类型查询合格率
     *
     * @param dto
     * @return
     */
    @PostMapping("/feign/pms/produce/pass/rate/producePassRateGroup")
    @ApiOperation("根据分组类型查询合格率")
    R<List<ProducePassRateVO>> producePassRateGroup(@RequestBody ProducePassRateDTO dto);
}
