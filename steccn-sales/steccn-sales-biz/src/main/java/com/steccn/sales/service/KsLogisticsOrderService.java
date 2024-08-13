package com.steccn.sales.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.constant.ResultCode;
import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.ems.vo.EpDeepConsumeContrastVO;
import com.steccn.sales.domain.KsLogisticsOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.sales.dto.KsLogisticsDTO;
import com.steccn.sales.dto.KsLogisticsOrderDTO;
import com.steccn.sales.dto.KsShippingReconciliationDTO;
import com.steccn.sales.vo.KsLogisticsVO;
import com.steccn.sales.vo.KsShippingReconciliationVO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

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


    /**
     * 发货对账数据
     * @param dto
     * @return
     */
    Page<KsShippingReconciliationVO> shippingReconciliation(KsShippingReconciliationDTO dto);

    /**
     * 销售发货单
     * @param dto
     * @return
     */
    BigDecimal salesDeliverySum(TupleDTO<LocalDate,LocalDate> dto);

    /**
     * 物流单发货数
     * @param start
     * @param end
     * @return
     */
    EpDeepConsumeContrastVO logisticsNum(LocalDate start,LocalDate end);

    Boolean updateOrderByOrderCode(KsLogisticsOrderDTO dto);

    Map<String,String> salesOrderCarCode(Set<String> salesOrders);
}
