package com.steccn.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.constant.ResultCode;
import com.steccn.common.vo.EpChartGraphVO;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.RawMaterialReportDTO;
import com.steccn.wms.domain.WmsTransferSlip;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.wms.dto.WmsReportWarehouseDTO;
import com.steccn.wms.dto.WmsTransferSlipDTO;
import com.steccn.wms.dto.WmsTransferSlipPageDTO;
import com.steccn.wms.vo.WmsReportWarehouseVO;
import com.steccn.wms.vo.WmsTransferSlipInfoVO;
import com.steccn.wms.vo.WmsTransferSlipVO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
* @author W10
* @description 针对表【wms_transfer_slip(调拨单)】的数据库操作Service
* @createDate 2022-11-30 09:45:17
*/
public interface WmsTransferSlipService extends IService<WmsTransferSlip> {

    /**
     * 调拨单分页内容
     * @param dto
     * @return
     */
    Page<WmsTransferSlipVO> pageList(WmsTransferSlipPageDTO dto);

    /**
     * 调拨单新增
     * @param dto
     * @return
     */
    Boolean add(WmsTransferSlipDTO dto);

    /**
     * 调拨单编辑
     * @param dto
     * @return
     */
    Boolean edit(WmsTransferSlipDTO dto);

    /**
     * 调拨单确认
     * @param id
     * @return
     */
    Boolean affirm(Long id);

    /**
     * 调拨单删除
     * @param id
     * @return
     */
    Boolean delete(Long id);

    /**
     * 调拨单详情
     * @param id
     * @return
     */
    WmsTransferSlipVO get(Long id);

    /**
     * 同步erp
     * @param id
     * @return
     */
    Boolean linkErp(Long id);

    /**
     * 根据线边仓获取领用量
     *
     * @param date       日期
     * @param shelfIdSet 货架集合
     * @return
     */
    BigDecimal slipLineWarehouse(LocalDate date, Set<Integer> shelfIdSet);

    /**
     * 获取调拨单
     * @param wmsReportWarehouseDTO
     * @return
     */
    Page<WmsReportWarehouseVO> transferSlip(WmsReportWarehouseDTO wmsReportWarehouseDTO);


    /**
     * 调拨单分类分析
     * @param wmsReportWarehouseDTO
     * @return
     */
    EpChartGraphVO<List<String>, BigDecimal> transferSlipClassification(WmsReportWarehouseDTO wmsReportWarehouseDTO);

    /**
     * 获取调拨单明细总数
     * @param dto
     * @return
     */
    BigDecimal totalQuantity(WmsReportWarehouseDTO dto);

    /**
     * 根据时间获取调拨单源仓库为主要材料库的指定物料数量
     * @param startTime
     * @param endTime
     * @param materialName
     * @return
     */
    Map<String, BigDecimal> materialDepot(LocalDateTime startTime, LocalDateTime endTime, List<String> materialName);

    /**
     * 根据时间获取调拨单目标仓库为指定仓库的指定物料数量
     * @param dto
     * @return
     */
    Map<Long, WmsTransferSlipInfoVO> materialQuantityByDateAndWarehouse(RawMaterialReportDTO dto);


    /**
     * 根据时间获取调拨单目标仓库为指定仓库的指定物料数量
     * @param dto
     * @return
     */
    Map<String,BigDecimal> materialQuantityByDateAndWarehouseQuantity(RawMaterialReportDTO dto);

    BigDecimal totalQuantityM(WmsReportWarehouseDTO dto);
}
