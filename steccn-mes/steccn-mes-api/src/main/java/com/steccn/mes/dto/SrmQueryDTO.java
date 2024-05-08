package com.steccn.mes.dto;

import lombok.Data;

import java.util.List;

/**
 * 请求采购系统DTO
 *
 * @author AndrewLuo
 * @date 2024/05/07
 */
@Data
public class SrmQueryDTO {
    private List<String> itemCodes;
    private Integer pageSize;
    private Integer pageIndex;
    private String startTime;
    private String endTime;
}
