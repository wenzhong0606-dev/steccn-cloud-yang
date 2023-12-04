package com.steccn.sales.dto;

import lombok.Data;

/**
 * 物流单图表DTO
 *
 * @author AndrewLuo
 * @date 2023/11/30
 */
@Data
public class KsLogisticsDashBoardDTO {
    /**
     * 值
     */
    private Integer value;
    /**
     * 物流公司
     */
    private Integer companyId;
    /**
     * 地址
     */
    private Integer customerId;
}
