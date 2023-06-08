package com.steccn.qms.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.dto.ValidList;
import com.steccn.common.vo.R;
import com.steccn.qms.dto.OutputQualityDetailDTO;
import com.steccn.qms.dto.QtEmsPullAmountDTO;
import com.steccn.qms.vo.OutputQualityDetailVO;
import com.steccn.qms.vo.QtCheckTaskDetailItemMaterialVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
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

    @ApiOperation("ems获取拉引量")
    @PostMapping("/feign/qms/task/detail/ems/getPullAmount")
    R<List<QtEmsPullAmountDTO>> getEmsPullAmount(@RequestBody ValidList<QtEmsPullAmountDTO> dto);


    @ApiOperation("根据条件获取在线检缺陷信息")
    @PostMapping("/feign/qms/task/detail/list/defective")
    R<List<OutputQualityDetailVO>> listDefectiveData(@RequestBody OutputQualityDetailDTO outputQualityDetailDTO);

    @ApiOperation("根据条件获取巡检平均厚度数据")
    @PostMapping("/feign/qms/task/detail/list/polling")
    R<List<OutputQualityDetailVO>> listPollingDate(@RequestBody OutputQualityDetailDTO outputQualityDetailDTO);

}
