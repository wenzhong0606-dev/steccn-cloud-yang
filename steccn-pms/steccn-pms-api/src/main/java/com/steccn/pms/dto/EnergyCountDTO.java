package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;

/**
 * @author yequan
 */
@Data
@ApiModel("能耗统计dto")
public class EnergyCountDTO implements Serializable {

    @ApiModelProperty("时间范围类型：本周，本月，本日 见DateRangeTypeEnum枚举")
    private Integer dateRangeType;

    @ApiModelProperty("统计维度 班组，产线 见DataGaugeTypeEnum")
    private Integer dataGaugeType;

    @ApiModelProperty("生产车间id")
    private Integer workshopId;
}
