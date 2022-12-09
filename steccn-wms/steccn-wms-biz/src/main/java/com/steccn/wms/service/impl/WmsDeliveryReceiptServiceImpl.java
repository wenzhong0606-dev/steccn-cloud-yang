package com.steccn.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.wms.domain.WmsDeliveryReceipt;
import com.steccn.wms.service.WmsDeliveryReceiptService;
import com.steccn.wms.mapper.WmsDeliveryReceiptMapper;
import org.springframework.stereotype.Service;

/**
* @author W10
* @description 针对表【wms_delivery_receipt(出库单)】的数据库操作Service实现
* @createDate 2022-12-09 11:18:09
*/
@Service
public class WmsDeliveryReceiptServiceImpl extends ServiceImpl<WmsDeliveryReceiptMapper, WmsDeliveryReceipt>
    implements WmsDeliveryReceiptService{

}




