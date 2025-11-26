package com.steccn.platform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2025/11/18
 */
@Data
public class IotDataQueryDetailDTO {
    private String deviceID;
    private String name;
    private String pointID;
    @JsonProperty("yname")
    private String yname;
    private List<String> aggregations;
}
