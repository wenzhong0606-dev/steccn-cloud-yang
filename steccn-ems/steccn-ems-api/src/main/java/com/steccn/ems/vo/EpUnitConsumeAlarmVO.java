package com.steccn.ems.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author chenhuan
 * @date 2022/12/7 14:56
 */
@Data
public class EpUnitConsumeAlarmVO {
    private LocalDateTime alarmDate;
    private String alarmDetail;
    private String alarmType;
    private String energyTypeName;
    private String partitionName;
    private String partitionNameAll;

}
