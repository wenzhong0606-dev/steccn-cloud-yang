package com.steccn.platform.dto;

import lombok.Data;

import java.util.List;

/**
 * 物流单详情DTO
 *
 * @author AndrewLuo
 * @date 2023/09/21
 */
@Data
public class SalesLogisticsInfoDTO {
    /**
     * 物流单号
     */
    private String logistics_code;

    /**
     * 详情数据
     */
    private List<SalesLogisticsInfoDetailDTO> data;
}
