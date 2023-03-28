package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author yequan
 */
@Data
@ApiModel("能耗分组集合数据")
public class EnergyGroupListVO implements Serializable {

    @ApiModelProperty("产线")
    private Integer productLineId;

    @ApiModelProperty("开始时间")
    private LocalDateTime startTime;

    @ApiModelProperty("班次日期")
    private LocalDate classDate;

    @ApiModelProperty("班组")
    private Long classGroupId;

    @ApiModelProperty("产线")
    private Long classesId;

    @ApiModelProperty("类型")
    private Integer type;

    @ApiModelProperty("数量")
    private BigDecimal quantity;

    @ApiModelProperty("拉引量")
    private BigDecimal pullForce;

    @ApiModelProperty("报工信息集合")
    private Long reportWorkId;

}
