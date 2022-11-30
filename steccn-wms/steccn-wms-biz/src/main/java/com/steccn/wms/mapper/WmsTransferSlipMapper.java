package com.steccn.wms.mapper;

import com.steccn.wms.domain.WmsTransferSlip;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author W10
* @description 针对表【wms_transfer_slip(调拨单)】的数据库操作Mapper
* @createDate 2022-11-30 09:45:17
* @Entity com.steccn.wms.domain.WmsTransferSlip
*/
@Mapper
public interface WmsTransferSlipMapper extends BaseMapper<WmsTransferSlip> {

}




