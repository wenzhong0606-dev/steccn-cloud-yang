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
    /**
     * 告警类型 0能耗 1单耗
     */
    private Integer type;
    private LocalDateTime endDate;
    private Integer energyType;
    private Long partitionId;
    private LocalDateTime startDate;
}
