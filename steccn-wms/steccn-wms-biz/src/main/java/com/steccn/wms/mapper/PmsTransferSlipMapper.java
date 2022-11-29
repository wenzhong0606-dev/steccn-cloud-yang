package com.steccn.wms.mapper;

import com.steccn.wms.domain.PmsTransferSlip;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author yequan
* @description 针对表【pms_transfer_slip(调拨单)】的数据库操作Mapper
* @createDate 2022-11-29 19:32:11
* @Entity com.steccn.wms.domain.PmsTransferSlip
*/
@Mapper
public interface PmsTransferSlipMapper extends BaseMapper<PmsTransferSlip> {

}




