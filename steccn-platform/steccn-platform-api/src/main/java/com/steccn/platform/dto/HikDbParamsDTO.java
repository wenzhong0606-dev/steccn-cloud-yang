package com.steccn.platform.dto;

import lombok.Data;

import java.util.List;

/**
 * 海康地磅DTO
 *
 * @author AndrewLuo
 * @date 2024/05/15
 */
@Data
public class HikDbParamsDTO {
    private String ability;
    private List<HikDbEventDTO> events;
    private String sendTime;
}
