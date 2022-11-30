package com.steccn.ems.mapper;

import com.steccn.ems.domain.EpMeterOperationRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author yequan
* @description 针对表【ep_meter_operation_record(仪表操作记录)】的数据库操作Mapper
* @createDate 2022-11-30 15:05:32
* @Entity com.steccn.ems.domain.EpMeterOperationRecord
*/
@Mapper
public interface EpMeterOperationRecordMapper extends BaseMapper<EpMeterOperationRecord> {

}




