package com.steccn.pms.feign;


import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.dto.ValidList;
import com.steccn.common.valid.UpdateGroup;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.PmsPullForceEditDTO;
import com.steccn.pms.dto.PmsPullForceQueryDTO;
import com.steccn.pms.vo.PmsPullForceDataVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;


/**
 * 拉引量Feign
 *
 * @author Mongknife
 * @date 2023/05/17
 */
@FeignClient(contextId = "remotePullForceFeignApi", value = FeignConstant.STECCN_PMS_SERVICE_NAME)
public interface RemotePullForceFeignApi {


    /**
     * 拉引量预填值
     * @param dtoList
     * @return
     */
    @ApiOperation("拉引量值预填")
    @PostMapping("/feign/pms/pull/force")
    R<Boolean> edit(@Validated(UpdateGroup.class)@Valid @RequestBody ValidList<PmsPullForceEditDTO> dtoList);

    /**
     * 查询拉引量
     * @param dto
     * @return
     */
    @ApiOperation("获取时间段内的拉引量详细值")
    @PostMapping("/feign/pms/pull/force/data")
    R<List<PmsPullForceDataVO>> getDataFromPeriods(@Valid @RequestBody PmsPullForceQueryDTO dto);
}
