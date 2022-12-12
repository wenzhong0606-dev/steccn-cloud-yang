package com.steccn.wms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author yequan
 */
@Data
@ApiModel("箱子位置dto")
public class CaseSiteDTO implements Serializable {

    @ApiModelProperty("箱号")
    private String caseNo;

    @ApiModelProperty("仓库code")
    private String warehouseCode;

    @ApiModelProperty("仓库id")
    private Integer warehouseId;

    @ApiModelProperty("仓库名称")
    private String warehouseName;

    @ApiModelProperty("货架code")
    private String shelfCode;

    @ApiModelProperty("货架id")
    private Integer shelfId;

    @ApiModelProperty("货架名称")
    private String shelfName;
}
