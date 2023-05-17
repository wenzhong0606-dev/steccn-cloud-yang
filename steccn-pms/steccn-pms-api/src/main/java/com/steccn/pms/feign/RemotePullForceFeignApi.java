package com.steccn.pms.feign;


import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.ValidList;
import com.steccn.common.valid.UpdateGroup;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.PmsPullForceEditDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;


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
}
