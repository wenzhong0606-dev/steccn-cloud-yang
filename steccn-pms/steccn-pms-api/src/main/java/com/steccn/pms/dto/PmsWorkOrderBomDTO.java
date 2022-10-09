package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
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
public class PmsWorkOrderBomDTO {

    @ApiModelProperty("id")
    private Long id;

    @ApiModelProperty("物料id")
    private Long moduleId;

    @ApiModelProperty("数量")
    private BigDecimal quantity;

    @ApiModelProperty("单位Id")
    private Long unitId;

    @ApiModelProperty("工序Id")
    private Long processId;

    @ApiModelProperty("固定数量")
    private Integer fixedQuantityFlag;

    @ApiModelProperty("副产品")
    private Integer coproductFlag;

    @ApiModelProperty("联产品")
    private Integer jointProductFlag;

    @ApiModelProperty("反冲")
    private Integer backCalculateFlag;

}
