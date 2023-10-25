package com.steccn.wms.service;

import com.steccn.common.constant.ResultCode;
import com.steccn.wms.domain.WmsCustomerBarcode;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.wms.dto.WmsCustomerBarcodeDTO;
import com.steccn.wms.vo.WmsCustomerBarcodeVO;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

    /**
     * 根据箱号获取客户条码
     * @param caseNo
     * @return
     */
    String getCustomerBarcodeByCaseNO(String caseNo);

    Boolean batchUpdate(List<WmsCustomerBarcodeDTO> list);

    /**
     * 根据箱号获取客户条码
     * @param caseNos
     * @return
     */
    Map<String,String> customerBarcodeMap(Set<String> caseNos);

    /**
     * 根据客户条码模糊查询箱号
     * @param customerBarcode
     * @return
     */
    List<String> listByCustomerBarcode(String customerBarcode);

    WmsCustomerBarcodeVO CustomerBarcodeByCaseNO(String customerBarcode);
}
