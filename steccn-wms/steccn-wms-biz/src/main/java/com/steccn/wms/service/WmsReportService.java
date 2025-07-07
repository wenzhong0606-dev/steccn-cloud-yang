package com.steccn.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.constant.ResultCode;
import com.steccn.common.dto.SteccnTableDataDTO;
import com.steccn.wms.dto.WmsInventoryDaySheetDTO;
import com.steccn.wms.dto.WmsInventoryPageDTO;
import com.steccn.wms.dto.WmsOutInWarehouseInfoDTO;
import com.steccn.wms.dto.WmsReportStatisticalDTO;
import com.steccn.wms.vo.WmsCaseNoHistoryVO;
import com.steccn.wms.vo.WmsHistoricalInventoryVO;
import com.steccn.wms.vo.WmsInventoryDaySheetCaseVO;
import com.steccn.wms.vo.WmsInventoryDaySheetVO;

import java.math.BigDecimal;
import java.util.List;

public interface WmsReportService {


    /**
     * 仓储统计报表数据
     * @param dto
     * @return
     */
    SteccnTableDataDTO statisticalData(WmsReportStatisticalDTO dto);


    /**
     * 仓储出入库明细统计报表
     * @return
     */
    SteccnTableDataDTO outInWarehouseInfo(WmsOutInWarehouseInfoDTO dto);


    /**
     * 仓储日报表
     * @param dto
     * @return
     */
    List<WmsInventoryDaySheetVO> daySheetData(WmsInventoryDaySheetDTO dto);

    /**
     * 生产领料出库日报表
     * @param dto
     * @return
     */
    List<WmsInventoryDaySheetVO> productionDaySheet(WmsInventoryDaySheetDTO dto);

    /**
     * 生产领料出库日报表箱列表
     * @param dto
     * @return
     */
    List<WmsInventoryDaySheetCaseVO> productionDaySheetCase(WmsInventoryDaySheetDTO dto);

    /**
     * 调拨单日报表
     * @param dto
     * @return
     */
    List<WmsInventoryDaySheetVO> transferDaySheet(WmsInventoryDaySheetDTO dto);

    /**
     * 调拨单日报表箱列表
     * @param dto
     * @return
     */
    List<WmsInventoryDaySheetCaseVO> transferDaySheetCase(WmsInventoryDaySheetDTO dto);

    /**
     * 同步日报表数据
     * @param dto
     * @return
     */
    Boolean synDaySheet(WmsInventoryDaySheetDTO dto);

    List<WmsInventoryDaySheetCaseVO> daySheetCaseList(WmsInventoryDaySheetDTO dto);

    /**
     * 成品库实时库存
     * @return
     */
    BigDecimal finishedProductTotalQuantity();

    /**
     * 锁定总数
     * @return
     */
    BigDecimal finishedProductLockTotalQuantity();


    /**
     *在途总数
     * @return
     */
    BigDecimal finishedProductOnTheWayTotalQuantity();

    String inventoryAccuracy();

    String breakageRate();

    String inactionStockProportion();

    String inventoryCarryRate();

    /**
     * 仓库历史库存查询
     * @param dto
     * @return
     */
    Page<WmsHistoricalInventoryVO> historical(WmsInventoryPageDTO dto);

    /**
     * 箱号历史记录
     * @param caseNo
     * @return
     */
    List<WmsCaseNoHistoryVO> caseNoHistory(String caseNo);

}
