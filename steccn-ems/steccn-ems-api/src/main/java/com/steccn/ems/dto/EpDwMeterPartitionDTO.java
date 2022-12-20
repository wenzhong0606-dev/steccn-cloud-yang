package com.steccn.ems.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;

/**
 * @author yequan
 * @desc 抄表归档-仪表分区dto
 */
@Data
@ApiModel("抄表归档-仪表分区dto")
public class EpDwMeterPartitionDTO implements Serializable {

    @ApiModelProperty("能源类型")
    private Integer energyType;

    @ApiModelProperty("组织机构id")
    private Integer officeId;

    @ApiModelProperty("远传仪表编码")
    private String distantMapCode;

    @ApiModelProperty("仪表id")
    private Integer meterId;
}
