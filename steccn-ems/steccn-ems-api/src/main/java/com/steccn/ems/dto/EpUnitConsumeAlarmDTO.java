package com.steccn.ems.dto;

import com.steccn.common.dto.BasePageDTO;
import lombok.Data;

import java.time.LocalDate;

/**
 * @author chenhuan
 * @date 2022/12/7 14:34
 */
@Data
public class EpUnitConsumeAlarmDTO extends BasePageDTO {
    private LocalDate endDate;
    private Integer energyType;
    private String partitionId;
    private LocalDate startDate;
}
