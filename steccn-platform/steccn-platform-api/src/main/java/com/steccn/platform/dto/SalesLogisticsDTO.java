package com.steccn.platform.dto;

import lombok.Data;

import java.util.List;

/**
 * 物流单查询DTO
 *
 * @author AndrewLuo
 * @date 2023/09/21
 */
@Data
public class SalesLogisticsDTO {
    private String server_code;
    private List<String> code;
}
