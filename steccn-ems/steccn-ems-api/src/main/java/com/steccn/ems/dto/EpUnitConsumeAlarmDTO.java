package com.steccn.ems.dto;

import com.steccn.common.dto.BasePageDTO;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author chenhuan
 * @date 2022/12/7 14:34
 */
@Data
public class EpUnitConsumeAlarmDTO extends BasePageDTO {
    private LocalDateTime endDate;
    private Integer energyType;
    private Long partitionId;
    private LocalDateTime startDate;
}
