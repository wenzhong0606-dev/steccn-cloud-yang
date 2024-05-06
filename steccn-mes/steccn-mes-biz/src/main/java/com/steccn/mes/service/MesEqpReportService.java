package com.steccn.mes.service;

import com.steccn.mes.domain.MesEqpReport;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.mes.dto.EqpOperationReportDTO;
import com.steccn.mes.vo.EqpOperationReportVO;

import java.util.List;

/**
* @author Admin
* @description 针对表【mes_eqp_report(设备运行报表)】的数据库操作Service
* @createDate 2024-05-06 16:36:23
*/
public interface MesEqpReportService extends IService<MesEqpReport> {

    /**
     * 查询报表
     * @param dto
     * @return
     */
    List<EqpOperationReportVO> queryReport(EqpOperationReportDTO dto);
}
