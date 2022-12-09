package com.steccn.wms.service;

import com.steccn.wms.domain.WmsTransferSlipInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
* @author W10
* @description 针对表【wms_transfer_slip_info(调拨单明细)】的数据库操作Service
* @createDate 2022-11-30 09:45:17
*/
public interface WmsTransferSlipInfoService extends IService<WmsTransferSlipInfo> {

    /**
     * 获取原始数量
     * @param idList
     * @return
     */
    Map<Long, BigDecimal> originalQuantity(List<Long> idList);

    /**
     * 获取原始箱号
     * @param idList
     * @return
     */
    Map<Long, String> originalCaseNo(List<Long> idList);
}
