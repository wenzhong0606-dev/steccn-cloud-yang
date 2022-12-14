package com.steccn.qms.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.qms.vo.QtCheckTaskDetailItemMaterialVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author chenhuan
 * @date 2022/12/9 11:22
 */
@FeignClient(contextId = "RemoteCheckTaskDetailFeignApi", value = FeignConstant.STECCN_QMS_SERVICE_NAME)
public interface RemoteCheckTaskDetailFeignApi {

    @ApiOperation("根据班次日期查询单片掰边重量、单片重量、拉引速度")
    @PostMapping("/feign/qms/task/detail/getByClassTime")
    R<List<QtCheckTaskDetailItemMaterialVO>> getByClassTime(@RequestBody TupleDTO<LocalDateTime,LocalDateTime> tupleDTO);
}
