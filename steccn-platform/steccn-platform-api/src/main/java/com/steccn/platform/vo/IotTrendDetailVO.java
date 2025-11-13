package com.steccn.platform.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2025/11/13
 */
@Data
public class IotTrendDetailVO {
    private Integer trendId;

    /**
     * 设备id
     */
    private String deviceId;

    /**
     * 测点id
     */
    private String pointId;

    /**
     * 聚合规则
     */
    private List<String> aggregations;

    /**
     * y轴名称
     */
    private String yName;

    /**
     * 排序
     */
    private Integer sort;
}
