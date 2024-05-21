package com.steccn.platform.dto;

import lombok.Data;

/**
 * 海康外层DTO
 *
 * @author AndrewLuo
 * @date 2024/05/20
 */
@Data
public class HikDbDTO {
    private String method;
    private HikDbParamsDTO params;
}
