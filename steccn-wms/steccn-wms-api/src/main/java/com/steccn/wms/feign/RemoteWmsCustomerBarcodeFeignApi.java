package com.steccn.wms.feign;


import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.CaseSiteDTO;
import com.steccn.wms.dto.WmsCustomerBarcodeDTO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


/**
 * 客户条码绑定
 *
 * @author 孟乐
 * @date 2023/10/20
 */
@FeignClient(contextId = "RemoteWmsCustomerBarcodeFeignApi", value = FeignConstant.STECCN_WMS_SERVICE_NAME)
public interface RemoteWmsCustomerBarcodeFeignApi {
    /**
     * 根据箱号查询客户条码
     *
     * @param caseNo
     * @return
     */
    @GetMapping("/feign/wms/customer/barcode/caseNo")
    @ApiOperation("根据箱号查询客户条码")
    R<String> getCustomerBarcodeByCaseNO(@ApiParam("箱号") @RequestParam("caseNo") String caseNo);

    @ApiOperation("批量更新客户条码")
    @PostMapping("/feign/wms/customer/barcode/batch/update")
     R<Boolean> batchUpdate(@RequestBody List<WmsCustomerBarcodeDTO> list);
}
