package com.steccn.platform.dto;

import lombok.Data;

/**
 * 海康地磅DTO
 *
 * @author AndrewLuo
 * @date 2024/05/15
 */
@Data
public class HikDbEventDataDTO {
    private HikDbModelDataDTO modelData;
    private String weighRegId;
}
