package com.steccn.wms.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.dto.Tuple4DTO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.dto.RawMaterialReportDTO;
import com.steccn.pms.vo.PmsProduceOrderVO;
import com.steccn.wms.domain.WmsTransferSlip;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.wms.domain.WmsTransferSlipInfo;
import com.steccn.wms.dto.WmsInventoryDaySheetDTO;
import com.steccn.wms.dto.WmsTransferSlipPageDTO;
import com.steccn.wms.vo.WmsInventoryDaySheetCaseVO;
import com.steccn.wms.vo.WmsInventoryDaySheetVO;
import com.steccn.wms.vo.WmsTransferSlipInfoVO;
import com.steccn.wms.vo.WmsTransferSlipVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
* @author W10
* @description 针对表【wms_transfer_slip(调拨单)】的数据库操作Mapper
* @createDate 2022-11-30 09:45:17
* @Entity com.steccn.wms.domain.WmsTransferSlip
*/
@Mapper
public interface WmsTransferSlipMapper extends BaseMapper<WmsTransferSlip> {

    /**
     * 调拨单条件筛选分页列表
     * @param page
     * @param dto
     * @return
     */
    Page<WmsTransferSlipVO> pageList(@Param("page") Page page, @Param("dto") WmsTransferSlipPageDTO dto);


    /**
     * 获取调拨单明细总数
     * @param warehouseIds
     * @param startTime
     * @param endTime
     * @return
     */
    List<WmsTransferSlipInfo> totalQuantity(@Param("warehouseIds") Set<Long> warehouseIds, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 获取主
     * @param startTime
     * @param endTime
     * @param materialName
     * @param id
     * @return
     */
    List<WmsTransferSlipInfo> materialDepot(@Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime,
                                            @Param("materialName") List<String> materialName,
                                            @Param("id") Integer id);

    List<WmsTransferSlipInfo> materialQuantityByDateAndWarehouse(@Param("dto") RawMaterialReportDTO dto);

    List<Tuple4DTO<String, Long, BigDecimal,Long>> materialQuantityDay(@Param("start") LocalDateTime start,
                                                                       @Param("end") LocalDateTime end,
                                                                       @Param("warehouseIds") List<Integer> wsIdSet,
                                                                       @Param("materialIds") List<Long> materialIds);

    List<Tuple4DTO<String, Long, BigDecimal, Long>> materialQuantityMonth(@Param("start") LocalDateTime start,
                                                                          @Param("end") LocalDateTime end,
                                                                          @Param("warehouseIds") List<Integer> wsIdSet,
                                                                          @Param("materialIds") List<Long> materialIds);

    List<Tuple4DTO<String, Long, BigDecimal, Long>> materialQuantityYear(@Param("start") LocalDateTime start,
                                                                         @Param("end") LocalDateTime end,
                                                                         @Param("warehouseIds") List<Integer> wsIdSet,
                                                                         @Param("materialIds") List<Long> materialIds);

    Integer wmsTransferSlipService(@Param("shelfIds") Set<Long> shelfIds);

    List<WmsTransferSlipInfoVO> WmsTransferSlipInfoVO(@Param("startTime") LocalDateTime startTime,@Param("endTime") LocalDateTime endTime,@Param("materialIds") Set<Long> materialIds,@Param("warehouseId") Long warehouseId);

    List<WmsTransferSlipInfoVO> intervalOutQuantity(@Param("startTime") LocalDateTime startTime,@Param("endTime") LocalDateTime endTime,@Param("materialIds") Set<Long> materialIds,@Param("warehouseId") Long warehouseId);

    List<WmsTransferSlipInfoVO> intervalOutOrder(@Param("startTime") LocalDateTime startTime,@Param("endTime") LocalDateTime endTime,@Param("materialIds") Set<Long> materialIds,@Param("warehouseId") Long warehouseId);

    List<WmsTransferSlipInfoVO> intervalEnterOrder(@Param("startTime") LocalDateTime startTime,@Param("endTime") LocalDateTime endTime,@Param("materialIds") Set<Long> materialIds,@Param("warehouseId") Long warehouseId);

    /**
     * 调拨日报表
     * @param dto
     * @return
     */
    List<WmsInventoryDaySheetVO> transferDaySheet(@Param("dto") WmsInventoryDaySheetDTO dto);

    /**
     * 调拨日报表箱号
     * @param dto
     * @return
     */
    List<WmsInventoryDaySheetCaseVO> transferDaySheetCaseList(@Param("dto")WmsInventoryDaySheetDTO dto);

}




