package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 生产订单工艺报工信息
 *
 * @author Mongknife
 * @date 2022/10/11
 */
@Data
@ApiModel("生产订单工艺-报工信息")
public class PmsReportWorkVO {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("报工编号")
    private String code;

    @ApiModelProperty("报工时间")
    private LocalDateTime createTime;

    @ApiModelProperty("报工人")
    private String createBy;

    @ApiModelProperty("加工数")
    private BigDecimal processQuantity;

    @ApiModelProperty("不良数")
    private BigDecimal badnessQuantity;

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("单位id")
    private Long unitId;

    @ApiModelProperty("批次")
    private String batchNo;

    @ApiModelProperty("工位id")
    private Integer stationId;

    @ApiModelProperty("工位")
    private String station;

    @ApiModelProperty("班次")
    private String classes;

    private Long classesId;

    private Integer productLineId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long materialId;

    private String workshopName;

}
