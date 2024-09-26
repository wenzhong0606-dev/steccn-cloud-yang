package com.steccn.wms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 调拨单明细VO
 *
 * @author Mongknife
 * @date 2022/11/30
 */
@Data
@ApiModel("调拨单明细VO")
public class WmsTransferSlipInfoVO {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("源库存id")
    private Long sourceInventoryId;

    @ApiModelProperty("来源货架")
    private Integer sourceShelf;

    @ApiModelProperty("来源货架名称")
    private String  sourceShelfName;

    @ApiModelProperty("目标货架")
    private Integer targetShelf;

    @ApiModelProperty("目标货架名称")
    private String targetShelfName;

    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("物料编码")
    private String materialCode;

    @ApiModelProperty("数量")
    private BigDecimal quantity;

    @ApiModelProperty("关联单据")
    private String associatedNo;

    @ApiModelProperty("批次")
    private String batch;

    @ApiModelProperty("箱号")
    private String caseNo;

    @ApiModelProperty("单位")
    private Long unit;

    @ApiModelProperty("单位名称")
    private String unitName;


    private Integer count;

}
