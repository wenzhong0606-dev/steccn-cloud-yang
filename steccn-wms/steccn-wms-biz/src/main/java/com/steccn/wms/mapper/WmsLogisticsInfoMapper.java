package com.steccn.wms.mapper;

import com.steccn.wms.domain.WmsLogisticsInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author W10
* @description 针对表【wms_logistics_info(收发货信息)】的数据库操作Mapper
* @createDate 2022-11-30 09:45:17
* @Entity com.steccn.wms.domain.WmsLogisticsInfo
*/
@Mapper
public interface WmsLogisticsInfoMapper extends BaseMapper<WmsLogisticsInfo> {

}




