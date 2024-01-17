package com.steccn.wms.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.RawMaterialReportDTO;
import com.steccn.wms.vo.WmsTransferSlipInfoVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yequan
 * @desc
 * @date 2023/9/13
 */
@FeignClient(contextId = "remoteWmsTransferSlipFeignApi", value = FeignConstant.STECCN_WMS_SERVICE_NAME)
public interface RemoteWmsTransferSlipFeignApi {

    /**
     * 根据线边仓获取领用量
     *
     * @param tupleDTO dto
     * @return
     */
    @PostMapping(value = "/feign/wms/transfer/slip/line/warehouse")
    @ApiOperation("根据线边仓获取领用量")
    R<BigDecimal> slipLineWarehouse(@RequestBody TupleDTO<LocalDate, Set<Integer>> tupleDTO);

    @ApiOperation("根据时间获取调拨单源仓库为主要材料库的指定物料数量")
    @PostMapping("/feign/wms/transfer/slip/material/depot/quantity")
    R<Map<String, BigDecimal>> materialDepot(@RequestBody RawMaterialReportDTO dto);

    @ApiOperation("根据时间获取调拨单目标仓库为指定仓库的指定物料数量")
    @PostMapping("/feign/wms/transfer/slip/material/date/warehouse/quantity")
    R<Map<Long,WmsTransferSlipInfoVO>> materialQuantityByDateAndWarehouse(@RequestBody RawMaterialReportDTO dto);

}
