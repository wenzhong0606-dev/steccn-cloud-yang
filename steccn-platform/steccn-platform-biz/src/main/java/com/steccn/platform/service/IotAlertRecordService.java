package com.steccn.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.dto.BasePageDTO;
import com.steccn.platform.domain.IotAlertRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.platform.dto.IotAlertRecordDTO;
import com.steccn.platform.vo.IotAlertRecordVO;
import com.steccn.platform.vo.IotAlertVO;

/**
* @author Admin
* @description 针对表【iot_alert_record(告警记录表)】的数据库操作Service
* @createDate 2025-12-15 15:32:15
*/
public interface IotAlertRecordService extends IService<IotAlertRecord> {
    /**
     * 报警记录
     * @param dto
     * @return
     */
    Page<IotAlertRecordVO> listInfo(IotAlertRecordDTO dto);
}
