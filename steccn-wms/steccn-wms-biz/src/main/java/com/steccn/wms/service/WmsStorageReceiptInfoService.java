package com.steccn.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.constant.ResultCode;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.EpChartGraphVO;
import com.steccn.pms.dto.RawMaterialReportDTO;
import com.steccn.wms.domain.WmsStorageReceiptInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.wms.dto.WmsStorageReceiptPageDTO;
import com.steccn.wms.vo.WmsDeliveryReceiptInfoVO;
import com.steccn.wms.vo.WmsStorageReceiptInfoVO;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
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

    /**
     * 根据时间获取类型为生产领料和生产退料的物料数量-用于碳核算
     *
     * @param materialId 物料主键
     * @param start      开始日期
     * @param end        结束日期
     * @return
     */
    Map<LocalDate, BigDecimal> produceConsumeQuantity(Long materialId, LocalDate start, LocalDate end);

    /**
     * yuan
     * @param tupleDTO
     * @return
     */
    BigDecimal purchaseInventoryRawGlass(TupleDTO<LocalDateTime, LocalDateTime> tupleDTO);

    Page<WmsStorageReceiptInfoVO> infoPageList(WmsStorageReceiptPageDTO dto);

    void excelExportData(HttpServletResponse response, Long id) throws IOException;
}
