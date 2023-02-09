package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author yequan
 * @desc 生产管理-报工加工数预填
 */
@Data
@ApiModel("生产管理-报工加工数预填")
public class PmRwPreloadDTO implements Serializable {

    @ApiModelProperty("工位id")
    @NotNull(message = "工位不能为空")
    private Integer stationId;

    @ApiModelProperty("开始时间")
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @ApiModelProperty("结束时间")
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;
}
