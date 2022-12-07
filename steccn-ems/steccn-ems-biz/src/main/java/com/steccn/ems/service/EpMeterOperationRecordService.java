package com.steccn.ems.service;

import com.steccn.ems.domain.EpMeterOperationRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.ems.vo.EpMeterOperationRecordVO;

import java.time.LocalDate;
import java.util.List;

/**
* @author yequan
* @description 针对表【ep_meter_operation_record(仪表操作记录)】的数据库操作Service
* @createDate 2022-11-30 15:05:32
*/
public interface EpMeterOperationRecordService extends IService<EpMeterOperationRecord> {

    /**
     * 查询操作记录列表
     *
     * @param meterId
     * @param readDate
     * @return
     */
    List<EpMeterOperationRecordVO> list(Integer meterId, LocalDate readDate);
}
