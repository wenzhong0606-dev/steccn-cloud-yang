package com.steccn.wms.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.pms.vo.PmsProduceOrderVO;
import com.steccn.wms.domain.WmsTransferSlip;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.wms.dto.WmsTransferSlipPageDTO;
import com.steccn.wms.vo.WmsTransferSlipVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
}




