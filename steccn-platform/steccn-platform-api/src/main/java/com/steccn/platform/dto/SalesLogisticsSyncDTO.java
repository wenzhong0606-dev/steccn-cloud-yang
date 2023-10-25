package com.steccn.platform.dto;

import lombok.Data;

import java.util.List;

/**
 * 出库单同步DTO
 *
 * @author AndrewLuo
 * @date 2023/09/21
 */
@Data
public class SalesLogisticsSyncDTO {
    /**
     * 默认传 mes_logistics
     */
    private String server_code;

    /**
     * 物料单详情
     */
    private List<SalesLogisticsInfoDTO> data;
}
