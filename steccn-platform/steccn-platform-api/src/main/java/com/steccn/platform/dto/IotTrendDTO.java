package com.steccn.platform.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
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
public class IotTrendDTO {
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
    @NotEmpty(message = "")
    private List<String> aggregations;

    /**
     * 排序
     */
    @NotNull(message = "排序不能为空")
    private Integer sort;

}
