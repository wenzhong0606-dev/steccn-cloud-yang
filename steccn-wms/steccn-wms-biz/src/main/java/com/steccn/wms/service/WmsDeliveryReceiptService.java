package com.steccn.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.common.constant.ResultCode;
import com.steccn.common.vo.EpChartGraphVO;
import com.steccn.wms.domain.WmsDeliveryReceiptInfo;
import com.steccn.wms.domain.WmsInventory;
import com.steccn.wms.dto.*;
import com.steccn.common.vo.R;
import com.steccn.wms.domain.WmsDeliveryReceipt;
import com.steccn.wms.vo.*;
import com.steccn.wms.vo.WmsDeliveryReceiptVO;
import com.steccn.wms.vo.WmsReportWarehouseVO;
import com.steccn.wms.vo.WmsScanInventoryVO;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
* @author W10
* @description 针对表【wms_delivery_receipt(出库单)】的数据库操作Service
* @createDate 2022-12-09 11:18:09
*/
public interface WmsDeliveryReceiptService extends IService<WmsDeliveryReceipt> {

    /**
     * 分页列表
     * @param dto
     * @return
     */
    Page<WmsDeliveryReceiptVO> pageList(WmsDeliveryReceiptPageDTO dto);

    /**
     * 新增
     * @param dto
     * @return
     */
    Boolean add(WmsDeliveryReceiptDTO dto);

    /**
     * 新增
     * @param dto
     * @return
     */
    Long addId(WmsDeliveryReceiptDTO dto);

    /**
     * 编辑
     * @param dto
     * @return
     */
    Boolean edit(WmsDeliveryReceiptDTO dto);

    /**
     * 确认
     * @param id
     * @return
     */
    Boolean affirm(Long id);


    /**
     * 删除
     * @param id
     * @return
     */
    Boolean delete(Long id);


    /**
     * 获取详情
     * @param id
     * @return
     */
    WmsDeliveryReceiptVO get(Long id);

    /**
     * 生成出库单
     * @param dto
     * @return
     */
    Boolean generateDelivery(WmsDeliveryReceiptDTO dto);



    /*****
     * 扫描 根据箱号获取库存信息
     * @param caseNo
     * @return
     */
    R<WmsScanInventoryVO> getCaseNoByScanInventoryInfo(String caseNo);

    /****
     * 手机端 扫描出库
     * @param paramMap
     * @return
     */
    R<String> scanDeliveryReceipt( Map<String, Object> paramMap);

    /**
     * 入库单转成出库单
     * @param code
     * @return
     */
    Boolean storageToReceipt(String code);

    /**
     *  创建出库单确认
     * @param wmsDeliveryReceiptDTO
     * @return
     */
    WmsDeliveryReceiptDTO createAndAffirm(WmsDeliveryReceiptDTO wmsDeliveryReceiptDTO,String userName);

    WmsDeliverySellFlagVO sellFlag(String number);

    String sell(WmsDeliveryReceiptDTO wmsDeliveryReceiptDTO);

    /**
     * 箱号获取出库标识
     * @param dto
     * @return
     */
    WmsDeliveryFlagVO deliveryFlagInfo(WmsDeliveryReceiptFlagInfoDto dto);

    WmsDeliverySellInfoVO deliverySellMaterialInfo(String number);

    /**
     * 同步至erp
     * @param id
     * @return
     */
    Boolean linkErp(Long id);


    /**
     * 获取出库单及明细
     * @param wmsReportWarehouseDTO
     * @return
     */
    Page<WmsReportWarehouseVO> outboundDelivery(WmsReportWarehouseDTO wmsReportWarehouseDTO);

    /**
     * 出库单分类分析
     * @param wmsReportWarehouseDTO
     * @return
     */
    EpChartGraphVO<List<String>, BigDecimal> outboundDeliveryClassification(WmsReportWarehouseDTO wmsReportWarehouseDTO);

    /**
     * 出库单明细总数
     * @param dto
     * @return
     */
    BigDecimal totalQuantity(WmsReportWarehouseDTO dto);

    BigDecimal totalQuantityM(WmsReportWarehouseDTO dto);

    /**
     * 获取区间时间的入库单数据明细总和，单位为平方米
     * @param startTime
     * @param endTime
     * @return
     */
    List<WmsDeliveryReceiptInfo> intervalData(LocalDateTime startTime, LocalDateTime endTime,Set<Long> materialIds);


    /**
     * 获取区间时间的入库单数据明细总和，单位为平方米
     * @param startTime
     * @param endTime
     * @return
     */
    List<WmsDeliveryReceiptInfoVO> intervalDataVO(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 删除明细并还原库存
     * @param dto
     * @return
     */
    Boolean deleteInfo(WmsDeliveryReceiptDTO dto);

    Page<SalesLogisticsBillVO> shippingReconciliation(SalesLogisticsBillDTO dto);

    Integer unAffirmByShelf(Set<Long> shelfIds);

    BigDecimal monthSum(Set<Long> warehouseIds);

    /**
     * 获取区间时间的入库单物料数据总和
     * @param startTime
     * @param endTime
     * @param materialIds
     * @param warehouseIds
     * @return
     */
    Map<Long, BigDecimal>  intervalDataQuantity(LocalDateTime startTime, LocalDateTime endTime,Set<Long> materialIds,Long warehouseIds);

    Map<Long, Integer> intervalOrderQuantity(LocalDateTime startTime, LocalDateTime endTime, Set<Long> materialIds, Long warehouseId);

    /**
     * 获取客户指定日期范围内的出库汇总
     *
     * @param materialIdSet 物料id集合
     * @param start         开始日期
     * @param end           结束日期
     * @return
     */
    List<WmsDeliveryReceiptClientNumDTO> clientNum(Set<Long> materialIdSet, LocalDate start, LocalDate end);

    /**
     * 同步至erp
     * @param id
     * @return
     */
    Boolean LinkErp(Long id);

    /**
     * 生产领料出库日报表
     * @param dto
     * @return
     */
    List<WmsInventoryDaySheetVO> productionDaySheet(WmsInventoryDaySheetDTO dto);
}
