package com.steccn.platform.service;

import com.steccn.platform.domain.IotAlert;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.platform.dto.IotAlertDTO;

/**
* @author Admin
* @description 针对表【iot_alert(告警表)】的数据库操作Service
* @createDate 2025-12-10 14:27:19
*/
public interface IotAlertService extends IService<IotAlert> {
    Boolean addAlert(IotAlertDTO dto);
}
