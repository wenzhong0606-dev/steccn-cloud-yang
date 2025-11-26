package com.steccn.platform.dto;

import lombok.Data;

import java.util.List;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2025/11/18
 */
@Data
public class IotDataQueryDTO {
    private List<String> aggregations;
    private String end;
    private String fill;
    private String interval;
    private String start;
    private List<IotDataQueryDetailDTO> points;
}
