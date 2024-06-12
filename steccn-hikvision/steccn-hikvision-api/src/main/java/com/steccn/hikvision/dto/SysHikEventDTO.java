package com.steccn.hikvision.dto;

import lombok.Data;

import java.util.List;

/**
 * 海康威视DTO
 *
 * @author AndrewLuo
 * @date 2024/05/28
 */
@Data
public class SysHikEventDTO {
    private List<Long> eventTypes;
    private String eventDest;
    private Integer subType;
    private List<Integer> eventLvl;
}
