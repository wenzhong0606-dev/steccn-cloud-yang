package com.steccn.wms.mapper;

import com.steccn.wms.domain.WmsCustomerBarcode;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 34872
* @description 针对表【wms_customer_barcode(客户条码绑定)】的数据库操作Mapper
* @createDate 2023-10-20 13:59:53
* @Entity com.steccn.wms.domain.WmsCustomerBarcode
*/

@Mapper
public interface WmsCustomerBarcodeMapper extends BaseMapper<WmsCustomerBarcode> {

}




