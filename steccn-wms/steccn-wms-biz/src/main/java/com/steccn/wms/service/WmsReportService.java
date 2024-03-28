package com.steccn.wms.service;

import com.steccn.common.constant.ResultCode;
import com.steccn.common.dto.SteccnTableDataDTO;
import com.steccn.wms.dto.WmsReportStatisticalDTO;

public interface WmsReportService {


    /**
     * 仓储统计报表数据
     * @param dto
     * @return
     */
    SteccnTableDataDTO statisticalData(WmsReportStatisticalDTO dto);
}
