package com.steccn.wms.mapper;

import com.steccn.wms.domain.WmsCheckOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author W10
* @description 针对表【wms_check_order(盘点单)】的数据库操作Mapper
* @createDate 2022-11-30 09:45:17
* @Entity com.steccn.wms.domain.WmsCheckOrder
*/
@Mapper
public interface WmsCheckOrderMapper extends BaseMapper<WmsCheckOrder> {

}




