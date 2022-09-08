package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 工艺路线物料组件DTO
 *
 * @author Mongknife
 * @date 2022/9/7
 */
@Data
@ApiModel("工艺路线物料组件DTO")
public class PmCraftRouteModuleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键id")
    private Long id;

    @ApiModelProperty("物料组件id")
    private Long materialId;

    @ApiModelProperty("物料编码")
    private String materialCode;

    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("物料数量")
    private Integer quantity;

    @ApiModelProperty("单位id")
    private Long unitId;

    @ApiModelProperty("是否是固定数量：1是0否")
    private Integer isFixedQuantity;

    @ApiModelProperty("是否是副产品：1是0否")
    private Integer isCopruduct;

    @ApiModelProperty("是否是联产品：1是0否")
    private Integer isJointProduct;

    @ApiModelProperty("是否是反冲：1是0否")
    private Integer isBackCalculation;

    @ApiModelProperty("工序报废百分比")
    private Integer processScrapingPercent;

    @ApiModelProperty("组件报废百分比")
    private Integer componentScrapingPercent;

    @ApiModelProperty("工序信息Id")
    private Long processId;

    @ApiModelProperty("工序名称")
    private String processName;

    @ApiModelProperty("工艺路线id")
    private Long craftRouteId;

}
