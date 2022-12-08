package com.steccn.wms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 调拨单详情DTO
 *
 * @author Mongknife
 * @date 2022/12/7
 */
@Data
@ApiModel("调拨单详情DTO")
public class WmsTransferSlipInfoDTO {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("物料编码")
    private String materialCode;

    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("源库存id")
    private Long sourceInventoryId;

    @ApiModelProperty("目标库存id")
    private Long targetInventoryId;

    @ApiModelProperty("源货架")
    private Integer sourceShelf;

    @ApiModelProperty("来源货架名称")
    private String sourceShelfName;

    @ApiModelProperty("来源货架编码")
    private String sourceShelfCode;

    @ApiModelProperty("目标货架")
    private Integer targetShelf;

    @ApiModelProperty("目标货架名称")
    private String targetShelfName;

    @ApiModelProperty("目标货架编码")
    private String targetShelfCode;

    @ApiModelProperty("数量")
    private BigDecimal quantity;

    @ApiModelProperty("原始数量")
    private BigDecimal originalQuantity;

    @ApiModelProperty("原始箱号")
    private String originalCaseNo;

    @ApiModelProperty("关联单据")
    private String associatedNo;

    @ApiModelProperty("批次")
    private String batch;

    @ApiModelProperty("箱号")
    private String caseNo;

    @ApiModelProperty("单位")
    private Long unit;

}
