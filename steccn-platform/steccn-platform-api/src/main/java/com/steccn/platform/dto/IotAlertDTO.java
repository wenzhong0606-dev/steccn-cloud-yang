package com.steccn.platform.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2025/12/10
 */
@Data
public class IotAlertDTO {
    /**
     * 名称
     */
    private String name;

    /**
     * 设备id
     */
    @NotEmpty(message = "设备id不能为空")
    private String deviceId;

    /**
     * 测点id
     */
    @NotEmpty(message = "测点id不能为空")
    private String pointId;

    /**
     * 聚合规则
     */
    @NotEmpty(message = "聚合规则不能为空")
    private String aggregations;

    /**
     * 下限
     */
    private BigDecimal downValue;

    /**
     * 上限
     */
    private BigDecimal upValue;

    /**
     * 时间间隔
     */
    @NotNull(message = "时间间隔不能为空")
    private Integer interval;

    /**
     * 接收人
     */
    private List<Long> receiver;
}
