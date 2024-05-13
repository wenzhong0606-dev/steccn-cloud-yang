package com.steccn.data.service;

import com.steccn.data.domain.MesEqpSignal;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Admin
* @description 针对表【mes_eqp_signal】的数据库操作Service
* @createDate 2023-04-25 10:43:42
*/
public interface MesEqpSignalService extends IService<MesEqpSignal> {
    /**
     * 填充报表
     * @return
     */
    Boolean saveReport();

    /**
     * 保存设备报表
     * @return
     */
    Boolean saveReportEqp(String eqpCode);
}
