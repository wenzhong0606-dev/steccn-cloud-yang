package com.steccn.pms.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author chenhuan
 * @date 2022/11/7 10:50
 */
@Data
public class PmsPullForceDTO {

    @ApiModelProperty("车间id")
    private Long workshopId;

    @ApiModelProperty("月份")
    private String monthDate;
}
