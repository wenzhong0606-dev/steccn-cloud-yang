package com.steccn.wms.service;

import com.steccn.wms.domain.WmsStorageReceiptInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.wms.vo.WmsDeliveryReceiptInfoVO;

import java.util.List;
import java.util.Map;

/**
* @author W10
* @description 针对表【wms_storage_receipt_info】的数据库操作Service
* @createDate 2022-12-09 11:18:09
*/
public interface WmsStorageReceiptInfoService extends IService<WmsStorageReceiptInfo> {

    /**
     * 获取详情
     * @param updateIds
     * @return
     */
    Map<Long, WmsStorageReceiptInfo> infoMap(List<Long> updateIds);
}
