package com.steccn.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.constant.ResultCode;
import com.steccn.wms.domain.WmsDeliveryReceiptInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.wms.dto.WmsDeliveryReceiptDTO;
import com.steccn.wms.dto.WmsDeliveryReceiptPageDTO;
import com.steccn.wms.vo.WmsDeliveryReceiptInfoVO;
import com.steccn.wms.vo.WmsDeliveryReceiptVO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
* @author W10
* @description 针对表【wms_delivery_receipt_info(出库单明细)】的数据库操作Service
* @createDate 2022-12-09 11:18:09
*/
public interface WmsDeliveryReceiptInfoService extends IService<WmsDeliveryReceiptInfo> {


    /**
     * 获取原始数量
     * @param updateIds
     * @return
     */
    Map<Long, BigDecimal> originalQuantity(List<Long> updateIds);

    /**
     * 获取明细信息
     * @param id
     * @return
     */
    List<WmsDeliveryReceiptInfoVO> infoList(Long id);

    Page<WmsDeliveryReceiptInfoVO> infoPageList(WmsDeliveryReceiptPageDTO dto);
}
