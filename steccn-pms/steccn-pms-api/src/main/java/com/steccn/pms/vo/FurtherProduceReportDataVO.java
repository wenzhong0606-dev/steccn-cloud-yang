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
@ApiModel("深加工日报表数据库查询vo")
public class FurtherProduceReportDataVO implements Serializable {

    @ApiModelProperty("产线id")
    private Integer productLineId;

    @ApiModelProperty("班次日期")
    private LocalDate classDate;

    @ApiModelProperty("班次id")
    private Long classesId;

    @ApiModelProperty("工序id")
    private Long processId;

    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("生产订单id")
    private Long produceOrderId;

    @ApiModelProperty("加工数")
    private BigDecimal processQuantity;

    @ApiModelProperty("不合格数")
    private BigDecimal badnessQuantity;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
}
