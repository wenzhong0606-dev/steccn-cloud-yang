package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 生产订单-物料清单
 *
 * @author Mongknife
 * @date 2022/10/8
 */
@Data
@ApiModel("生产订单-物料清单")
public class PmsWorkOrderBomVO {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("订单id")
    private Long workProduceId;

    @ApiModelProperty("物料id")
    private Long moduleId;

    @ApiModelProperty("物料名称")
    private String name;

    @ApiModelProperty("物料编码")
    private String code;

    @ApiModelProperty("数量")
    private BigDecimal quantity;

    @ApiModelProperty("单位Id")
    private Long unitId;

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("工序Id")
    private Long processId;

    @ApiModelProperty("工序名称")
    private String processName;

    @ApiModelProperty("固定数量")
    private Integer fixedQuantityFlag;

    @ApiModelProperty("副产品")
    private Integer coproductFlag;

    @ApiModelProperty("联产品")
    private Integer jointProductFlag;

    @ApiModelProperty("反冲")
    private Integer backCalculateFlag;

    @ApiModelProperty("工序报废百分比")
    private Float processScrapingPercent;

    @ApiModelProperty("组件报废百分比")
    private Float componentScrapingPercent;

    @ApiModelProperty("工艺路线组件id")
    private Long craftRouteModuleId;

    @ApiModelProperty("是否回收")
    private Integer recycleFlag;

    @ApiModelProperty("来源")
    private Integer source;

    @ApiModelProperty("来源主键")
    private Long sourceId;

}
