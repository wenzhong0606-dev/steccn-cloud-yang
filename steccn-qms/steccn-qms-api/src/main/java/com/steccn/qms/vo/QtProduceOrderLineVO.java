package com.steccn.qms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 根据产线查询生产订单号等信息
 * @author wangzhixiong
 */
@Data
@ApiModel("生产订单VO")
public class QtProduceOrderLineVO implements Serializable {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("类型")
    private String type;

    @ApiModelProperty("类型id")
    private Long typeId;

    @ApiModelProperty("生产订单号")
    private String code;

    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("物料编码")
    private String materialCode;

    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("产线id")
    private Integer productionLineId;

    @ApiModelProperty("产线名称")
    private String productionLineName;

    @ApiModelProperty("批次信息")
    private Long batch;

    @ApiModelProperty("批次vo")
    private String batchNo;

    @ApiModelProperty("生产车间id")
    private Integer workshopId;

    @ApiModelProperty("生产车间名称")
    private String workshopName;

}
