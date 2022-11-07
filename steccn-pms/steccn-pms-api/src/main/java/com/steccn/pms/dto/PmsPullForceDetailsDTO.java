package com.steccn.pms.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author chenhuan
 * @date 2022/11/7 10:53
 */
@Data
public class PmsPullForceDetailsDTO {

    @ApiModelProperty("车间id")
    private Long workshopId;

    @ApiModelProperty("拉引量日期")
    private String pullForceDate;
}
