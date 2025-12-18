package com.steccn.platform.dto;

import com.steccn.common.dto.BasePageDTO;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2025/12/15
 */
@Data
public class IotAlertRecordDTO extends BasePageDTO {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
