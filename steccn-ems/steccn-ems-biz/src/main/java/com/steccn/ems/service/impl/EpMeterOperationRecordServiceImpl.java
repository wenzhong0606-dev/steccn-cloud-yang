package com.steccn.ems.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.ems.constant.EpManagerConstant;
import com.steccn.ems.domain.EpMeterOperationRecord;
import com.steccn.ems.domain.EpMeterRead;
import com.steccn.ems.mapper.EpMeterOperationRecordMapper;
import com.steccn.ems.service.EpMeterOperationRecordService;
import com.steccn.ems.service.EpMeterReadService;
import com.steccn.ems.vo.EpMeterOperationRecordVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author yequan
 * @description 针对表【ep_meter_operation_record(仪表操作记录)】的数据库操作Service实现
 * @createDate 2022-11-30 15:05:32
 */
@Service
public class EpMeterOperationRecordServiceImpl extends ServiceImpl<EpMeterOperationRecordMapper, EpMeterOperationRecord>
        implements EpMeterOperationRecordService {

    @Autowired
    private EpMeterReadService epMeterReadService;

    @Override
    public List<EpMeterOperationRecordVO> list(Integer meterId, LocalDate readDate) {
        EpMeterRead epMeterRead = epMeterReadService.getOne(new LambdaQueryWrapper<EpMeterRead>().eq(EpMeterRead::getMeterId, meterId).eq(EpMeterRead::getReadDate, readDate));

        List<EpMeterOperationRecordVO> list = new ArrayList<>();
        if (Objects.nonNull(epMeterRead)) {
            List<EpMeterOperationRecord> meterOperationRecordList = list(new LambdaQueryWrapper<EpMeterOperationRecord>().eq(EpMeterOperationRecord::getReadId, epMeterRead.getId()).orderByAsc(EpMeterOperationRecord::getOpTime));
            for (EpMeterOperationRecord epMeterOperationRecord : meterOperationRecordList) {
                EpMeterOperationRecordVO epMeterOperationRecordVO = new EpMeterOperationRecordVO();
                BeanUtils.copyProperties(epMeterOperationRecord,epMeterOperationRecordVO);
                epMeterOperationRecordVO.setOpNodeName(EpManagerConstant.OperationType.getNameByCode(epMeterOperationRecord.getOpNode()));
                list.add(epMeterOperationRecordVO);
            }
        }
        return list;
    }
}




