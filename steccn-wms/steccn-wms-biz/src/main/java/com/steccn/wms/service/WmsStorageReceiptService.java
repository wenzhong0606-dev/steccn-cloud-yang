package com.steccn.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.constant.ResultCode;
import com.steccn.common.vo.EpChartGraphVO;
import com.steccn.common.vo.EpChartGraphXAxisVO;
import com.steccn.pms.dto.PmExcelDTO;
import com.steccn.wms.domain.WmsStorageReceipt;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.wms.domain.WmsStorageReceiptInfo;
import com.steccn.wms.dto.*;
import com.steccn.wms.vo.*;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
* @author W10
* @description 针对表【wms_storage_receipt】的数据库操作Service
* @createDate 2022-12-09 11:18:09
*/
public interface WmsStorageReceiptService extends IService<WmsStorageReceipt> {

    /**
     * 分页列表
     * @param dto
     * @return
     */
    Page<WmsStorageReceiptVO> pageList(WmsStorageReceiptPageDTO dto);

    /**
     * 新增
     * @param dto
     * @return
     */
    Boolean add(WmsStorageReceiptDTO dto);

    /**
     * 采购系统添加采购入库单(地磅入库)
     * @param dto
     * @return
     */
    Boolean salesAdd(WmsStorageReceiptSalesDTO dto);


    /**
     * 新增
     * @param dto
     * @return code
     */
    String addReturnCode(WmsStorageReceiptDTO dto);



    /**
     * 编辑
     * @param dto
     * @return
     */
    Boolean edit(WmsStorageReceiptDTO dto);

    /**
     * 确认
     * @param id
     * @return
     */
    Boolean affirm(Long id,String userName);

    /**
     * 删除
     * @param id
     * @return
     */
    Boolean delete(Long id);

    /**
     * 获取详情内容
     * @param id
     * @return
     */
    WmsStorageReceiptVO get(Long id);

    /**
     * 创建单据
     * @param wmsStorageReceiptDTO
     * @return
     */
    String createOrder(WmsStorageReceiptDTO wmsStorageReceiptDTO);

    /**
     * 数据处理
     * @param dto
     * @return
     * @throws IOException
     */
    Boolean excelImportData(PmExcelDTO dto) throws IOException;


    /**
     * 冲销
     * @param id
     * @return
     */
    Boolean chargeAgainstById(Long id);


    /**
     * 冲销
     * @param code
     * @return
     */
    WmsDeliveryReceiptDTO chargeAgainstByCode(String code,String userName);

    /**
     * 同步erp
     * @param id
     * @return
     */
    Boolean linkErp(Long id);

    /**
     * 采购入库同步erp
     * @param id
     * @return
     */
    Boolean purchaseLinkErp(Long id);


    String inStorageCodeRepeat(LocalDate current);

    Boolean deleteData();

    WmsStorageFlagInfoVO storageFlagInfo(WmsStorageReceiptFlagInfoDTO dto);

    /**
     * 获取分页信息
     * @return
     */
    Page<WmsSalesDeliveryVO> salesDeliveryPage(WmsDeliveryOrderDTO dto);

    String produceOldLineAdd(WmsStorageReceiptDTO dto);

    /**
     * 采购入库
     * @param dto
     * @return
     */
    String salesReceiptAdd(SalesStorageReceiptDTO dto);

    /**
     * 报工关联供应商
     * @param rwIdSet 报工主键集合
     * @return
     */
    Map<Long, String> rwLinkSupplier(Set<Long> rwIdSet);
//    Boolean salesReceiptAdd(SalesStorageReceiptDTO dto);


    /**
     * 获取入库单明细
     * @param wmsReportWarehouseDTO
     * @return
     */
    Page<WmsReportWarehouseVO> warehouseWarrant(WmsReportWarehouseDTO wmsReportWarehouseDTO);


    /**
     * 入库单分类分析
     * @param wmsReportWarehouseDTO
     * @return
     */
    EpChartGraphVO<List<String>, BigDecimal> warehousingClassification(WmsReportWarehouseDTO wmsReportWarehouseDTO);

    /**
     * 入库单明细总数
     * @param dto
     * @return
     */
    BigDecimal totalQuantity(WmsReportWarehouseDTO dto);


    /**
     * 根据关联单号批量对出库单进行冲销
     * @param dtoList
     * @return
     */
    Boolean chargeAgainstAssociated(@RequestBody List<BatchChargeAgainstDTO> dtoList);

    /**
     * 根据入库单号批量修改入库单的明细货架内容
     * @param dto
     * @return
     */
    Boolean updateBatchShelf(WmsUpdateBatchShelfDTO dto);

    /**
     *
     * 对出库单进行冲销
     * @param deliveryId
     * @param userName
     * @return
     */
    Boolean chargeAgainstDelivery(Long deliveryId,String userName);

    BigDecimal totalQuantityM(WmsReportWarehouseDTO dto);

    /**
     * 销售入库获取物流单相关标识
     * @param number
     * @return
     */
    WmsSalesReturnsFlagVO salesReturnsFlag(String number);


    /**
     * 区间的数据
     * @return
     */
    List<WmsStorageReceiptInfo> intervalData(LocalDateTime startTime, LocalDateTime endTime,Set<Long> materialIds);

    List<WmsStorageReceiptInfoVO> intervalDataVO(LocalDateTime morningStartTime, LocalDateTime nightStartTime);

    String salesReturns(WmsSalesReturnsDTO wmsSalesReturnsDTO);

    List<WmsInventoryDaySheetVO> daySheetData(WmsInventoryDaySheetDTO dto);

    List<WmsInventoryDaySheetCaseVO> daySheetCaseList(WmsInventoryDaySheetDTO dto);

    Boolean deleteInfo(WmsStorageReceiptDTO dto);

    WmsCodeInfoVO codeInfo(WmsStorageReceiptFlagInfoDTO dto);

    Integer unAffirmByShelf(Set<Long> shelfIds);
}
