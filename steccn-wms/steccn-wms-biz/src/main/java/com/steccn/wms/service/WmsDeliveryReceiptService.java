package com.steccn.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.common.constant.ResultCode;
import com.steccn.common.vo.EpChartGraphVO;
import com.steccn.wms.domain.WmsInventory;
import com.steccn.wms.dto.StockInfoDTO;
import com.steccn.common.vo.R;
import com.steccn.wms.domain.WmsDeliveryReceipt;
import com.steccn.wms.dto.WmsDeliveryReceiptDTO;
import com.steccn.wms.dto.WmsDeliveryReceiptFlagInfoDto;
import com.steccn.wms.dto.WmsDeliveryReceiptPageDTO;
import com.steccn.wms.vo.*;
import com.steccn.wms.dto.WmsReportWarehouseDTO;
import com.steccn.wms.vo.WmsDeliveryReceiptVO;
import com.steccn.wms.vo.WmsReportWarehouseVO;
import com.steccn.wms.vo.WmsScanInventoryVO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import java.util.Map;
import java.util.Set;

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
}
