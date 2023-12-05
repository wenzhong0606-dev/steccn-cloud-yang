package com.steccn.wms.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.pms.vo.PmsProduceOrderVO;
import com.steccn.wms.domain.WmsTransferSlip;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.wms.domain.WmsTransferSlipInfo;
import com.steccn.wms.dto.WmsTransferSlipPageDTO;
import com.steccn.wms.vo.WmsTransferSlipVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
}




