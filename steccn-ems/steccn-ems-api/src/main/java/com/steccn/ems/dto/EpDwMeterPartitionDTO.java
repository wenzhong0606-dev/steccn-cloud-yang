package com.steccn.ems.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author yequan
 * @desc 抄表归档-仪表分区dto
 */
@Data
@ApiModel("抄表归档-仪表分区dto")
public class EpDwMeterPartitionDTO implements Serializable {

    @ApiModelProperty("班次日期")
    private LocalDate classDate;

    @ApiModelProperty("能源类型")
    private Integer energyType;

    @ApiModelProperty("组织机构id")
    private Long partitionId;

    @ApiModelProperty("组织机构id")
    private Integer officeId;

    @ApiModelProperty("远传仪表编码")
    private String distantMapCode;

    @ApiModelProperty("仪表id")
    private Integer meterId;

    @ApiModelProperty("产线id-可能是父节点也可能是自己")
    private Integer lineOfficeId;
}
