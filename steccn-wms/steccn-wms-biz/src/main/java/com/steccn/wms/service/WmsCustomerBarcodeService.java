package com.steccn.wms.service;

import com.steccn.common.constant.ResultCode;
import com.steccn.wms.domain.WmsCustomerBarcode;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.wms.dto.WmsCustomerBarcodeDTO;

/**
* @author 34872
* @description 针对表【wms_customer_barcode(客户条码绑定)】的数据库操作Service
* @createDate 2023-10-20 13:59:53
*/
public interface WmsCustomerBarcodeService extends IService<WmsCustomerBarcode> {

    /**
     * 客户条码新增
     * @param dto
     * @return
     */
    Boolean add(WmsCustomerBarcodeDTO dto);
}
