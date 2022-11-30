package com.steccn.wms.mapper;

import com.steccn.wms.domain.WmsCheckOrderInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author W10
* @description 针对表【wms_check_order_info(盘点单明细)】的数据库操作Mapper
* @createDate 2022-11-30 09:45:17
* @Entity com.steccn.wms.domain.WmsCheckOrderInfo
*/
@Mapper
public interface WmsCheckOrderInfoMapper extends BaseMapper<WmsCheckOrderInfo> {

}




