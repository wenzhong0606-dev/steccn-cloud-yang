package com.steccn.dp.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.dp.dto.DpPmsOperateDTO;
import com.steccn.dp.dto.WeightCountDTO;
import com.steccn.dp.vo.DpValueDateVO;
import com.steccn.dp.vo.DpWeightVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2023/11/23
 */
@FeignClient(contextId = "RemoteDpFeignApi", value = FeignConstant.STECCN_DP_SERVICE_NAME)
public interface RemoteDpFeignApi {

    /**
     * 电子秤读数
     * @param dto
     * @return
     */
    @PostMapping("/feign/dp/weight/count")
    @ApiOperation(value = "电子秤读数")
    R<List<DpWeightVO>> count(@RequestBody TupleDTO<LocalDateTime,LocalDateTime> dto);

    /**
     * 电子秤读数按天
     * @param dto
     * @return
     */
    @PostMapping("/feign/dp/weight/countAsDay")
    @ApiOperation(value = "电子秤读数按天")
    R<List<DpValueDateVO>> countAsDay(@RequestBody DpPmsOperateDTO dto);

    /**
     * 耗料数
     * @param dto
     * @return
     */
    @PostMapping("/feign/dp/weight/consume")
    @ApiOperation(value = "耗料数")
    R<List<WeightCountDTO>> consumeMaterial(@RequestBody DpPmsOperateDTO dto);
}
