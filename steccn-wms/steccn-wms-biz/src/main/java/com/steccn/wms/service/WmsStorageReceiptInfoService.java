package com.steccn.wms.service;

import com.steccn.common.constant.ResultCode;
import com.steccn.common.vo.EpChartGraphVO;
import com.steccn.pms.dto.RawMaterialReportDTO;
import com.steccn.wms.domain.WmsStorageReceiptInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.wms.vo.WmsDeliveryReceiptInfoVO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

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


    /**
     * 根据箱号获取数据（不包含已删除以及已冲销）
     * @param caseNo
     * @return
     */
    WmsStorageReceiptInfo getByCaseNo(String caseNo);


    /**
     * 获取指定物料在对应时间的数量
     * @param rawMaterialReportDTO
     * @return
     */
    Map<String, BigDecimal> purchaseQuantity(RawMaterialReportDTO rawMaterialReportDTO);
}
