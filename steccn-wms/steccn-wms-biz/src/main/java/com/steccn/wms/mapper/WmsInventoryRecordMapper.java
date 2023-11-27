package com.steccn.wms.mapper;

import com.steccn.wms.domain.WmsInventoryRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 34872
* @description 针对表【wms_inventory_record(库存流水记录表)】的数据库操作Mapper
* @createDate 2023-10-11 14:17:05
* @Entity com.steccn.wms.domain.WmsInventoryRecord
*/

@Mapper
public interface WmsInventoryRecordMapper extends BaseMapper<WmsInventoryRecord> {

}




