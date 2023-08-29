package com.steccn.pms.dto;

import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;

/**
 * @author yequan
 * @desc 产线加工信息dto
 * @date 2023/8/29
 */
@Data
@ApiModel("产线加工信息dto")
public class DashboardProducePageDTO extends BasePageDTO {

    @ApiModelProperty("产线主键")
    private Integer productLineId;

    @ApiModelProperty("日期")
    private LocalDate date;
}
