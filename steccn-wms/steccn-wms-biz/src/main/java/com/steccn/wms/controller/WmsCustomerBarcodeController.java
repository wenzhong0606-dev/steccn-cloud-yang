package com.steccn.wms.controller;


import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.ValidList;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.vo.R;
import com.steccn.oa.constant.OaConstant;
import com.steccn.platform.feign.RemoteSysOauthClientDetailFeignApi;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.wms.domain.WmsCustomerBarcode;
import com.steccn.wms.dto.WmsCustomerBarcodeDTO;
import com.steccn.wms.dto.WmsDeliveryReceiptDTO;
import com.steccn.wms.service.WmsCustomerBarcodeService;
import com.steccn.wms.vo.WmsCustomerBarcodeVO;
import com.steccn.wms.vo.WmsDeliveryCustomerBarCodeVO;
import com.steccn.wms.vo.WmsDeliverySellFlagVO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

/**
 * 客户条码绑定
 *
 * @author Mongknife
 * @date 2023/10/20
 */
@RestController
@RequestMapping("/wms/customer/barcode")
@RequiredArgsConstructor
@Slf4j
public class WmsCustomerBarcodeController {

    private final WmsCustomerBarcodeService wmsCustomerBarcodeService;
    private final RemoteSysOauthClientDetailFeignApi remoteSysOauthClientDetailFeignApi;


    @ApiOperation("客户条码绑定")
    @PostMapping()
    @SysLog("客户条码绑定")
    public R<Boolean> add(@Validated(InsertGroup.class) @RequestBody WmsCustomerBarcodeDTO dto) {
        return R.success(wmsCustomerBarcodeService.add(dto));
    }

    @ApiOperation("批量更新客户条码")
    @PostMapping("/batch/update")
    public R<Boolean> batchUpdate(@RequestBody List<WmsCustomerBarcodeDTO> list){
        return R.success(wmsCustomerBarcodeService.batchUpdate(list));
    }

    @ApiOperation("根据客户条码获取绑定的箱号以及生产批次号")
    @GetMapping("/caseNo")
    public R<WmsCustomerBarcodeVO> getCaseNoCustomerBarcode(@ApiParam("客户条码") @RequestParam("customerBarcode") String customerBarcode) {
        return R.success(wmsCustomerBarcodeService.CustomerBarcodeByCaseNO(customerBarcode));
    }


    @ApiOperation("根据客户条码获取物流信息")
    @GetMapping("/customerBarcode")
    public R<WmsDeliverySellFlagVO> customerBarcode(@ApiParam("客户条码") @RequestParam("customerBarcode") String customerBarcode) {
        return R.success(wmsCustomerBarcodeService.customerBarcode(customerBarcode));
    }


    @PostMapping("/thirdParty/logistics")
    @ApiOperation("三方根据物流单号获取客户条码")
    public R<List<WmsDeliveryCustomerBarCodeVO>> getDeliveryCustomerBarCode(HttpServletRequest request, @RequestBody Set<String> logisticsCodes) {
        validRequestHeader(request);
        return R.success(wmsCustomerBarcodeService.getDeliveryCustomerBarCode(logisticsCodes));
    }

    /**
     * 校验请求头信息
     *
     * @param request 请求
     */
    private void validRequestHeader(HttpServletRequest request) {
        List<String> pwdSet = FeignOptional.of(() -> remoteSysOauthClientDetailFeignApi.clientEncodePassword(), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        String clientId = request.getHeader(OaConstant.CLIENT_ID);
        Assert.isTrue(StrUtil.isNotBlank(clientId), "请求头中必须携带client-id");
        Assert.isTrue(pwdSet.contains(clientId), "非法访问，请携带正确的client-id");
    }

    @ApiOperation("根据箱号获取绑定的客户条码")
    @GetMapping()
    public R<String> getCustomerBarcodeByCaseNO(@ApiParam("箱号") @RequestParam("caseNo") String caseNo) {
        return R.success(wmsCustomerBarcodeService.getCustomerBarcodeByCaseNO(caseNo));
    }
}
