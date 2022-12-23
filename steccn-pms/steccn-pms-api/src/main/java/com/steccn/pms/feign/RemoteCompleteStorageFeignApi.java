package com.steccn.pms.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.PmsCompleteStorageDTO;
import com.steccn.pms.vo.PmsReportWorkVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author chenhuan
 * @date 2022/12/9 11:11
 */
@FeignClient(contextId = "RemoteCompleteStorageFeignApi", value = FeignConstant.STECCN_PMS_SERVICE_NAME)
public interface RemoteCompleteStorageFeignApi {


    /**
     * 完工入库变更状态
     * @param dto
     * @return
     */
    @ApiOperation("完工入库变更状态")
    @PostMapping("/feign/pms/complete/storage/complete")
    R<Boolean> completeStorageReviseStatue(@RequestBody PmsCompleteStorageDTO dto);

}
