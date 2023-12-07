package com.steccn.sales.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.sales.domain.KsLogisticsOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.sales.dto.KsLogisticsDTO;
import com.steccn.sales.vo.KsLogisticsVO;

import java.time.LocalDate;

/**
* @author Admin
* @description 针对表【ks_logistics_order(物流单)】的数据库操作Service
* @createDate 2023-11-29 14:16:04
*/
public interface KsLogisticsOrderService extends IService<KsLogisticsOrder> {
    /**
     * 获取物流单列表
     * @param dto
     * @return
     */
    Page<KsLogisticsVO> getLogisticsRecord(KsLogisticsDTO dto);

    /**
     * 获取物流单图表
     * @param dto
     * @return
     */
    QtLineChartBarDTO logisticsRecordDashBoard(KsLogisticsDTO dto);
}
