package com.steccn.platform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Iot趋势组DTO
 *
 * @author AndrewLuo
 * @date 2025/11/12
 */
@Data
public class IotTrendDetailDTO {
    @NotNull(message = "趋势组Id不能为空")
    private Integer trendId;

    /**
     * 设备id
     */
    @NotBlank(message = "设备id不能为空")
    private String deviceId;

    /**
     * 测点id
     */
    @NotBlank(message = "测点id不能为空")
    private String pointId;

    /**
     * 聚合规则
     */
    @NotEmpty(message = "聚合规则不能为空")
    private List<String> aggregations;

    /**
     * y轴名称
     */
    @JsonProperty("yName")
    private String yName;

    /**
     * 名称
     */
    private String name;

    /**
     * 排序
     */
    @NotNull(message = "排序不能为空")
    private Integer sort;

}
