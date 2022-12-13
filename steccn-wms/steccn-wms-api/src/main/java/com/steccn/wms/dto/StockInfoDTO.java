package com.steccn.wms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 仓库信息
 *
 * @author Mongknife
 * @date 2022/10/20
 */
@Data
@ApiModel("仓库信息")
public class StockInfoDTO {

    @ApiModelProperty("箱号")
    private String caseNo;
    @ApiModelProperty("仓库编码")
    private String warehouseCode;
    @ApiModelProperty("货架编码")
    private String shelfCode;
}
