package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author yequan
 */
@Data
@ApiModel("mess入库单物料信息")
public class MessInStorageVoucherListDTO implements Serializable {

    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("数量")
    private BigDecimal quantity;

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("物料编码")
    private String materialCode;

    @ApiModelProperty("规格")
    private String specifications;

    @ApiModelProperty("箱号")
    private String caseNo;

    @ApiModelProperty("仓库")
    private String warehouse;

    @ApiModelProperty("货架")
    private String shelf;

}
