package com.steccn.data.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.data.domain.MesEqpSignal;
import com.steccn.data.dto.OperationRecordsDTO;
import com.steccn.data.vo.OperationRecordVO;
import com.steccn.data.vo.OperationStatusVO;

/**
* @author Admin
* @description 针对表【mes_eqp_signal】的数据库操作Service
* @createDate 2023-09-05 09:19:04
*/
public interface MesEqpSignalService extends IService<MesEqpSignal> {
    /**
     * 获取设备运行状态
     * @param dto
     * @return
     */
    OperationStatusVO getEqpStatus(OperationRecordsDTO dto);

    /**
     * 获取设备记录
     * @param dto
     * @return
     */
    Page<OperationRecordVO> getEqpRecord(OperationRecordsDTO dto);
}
