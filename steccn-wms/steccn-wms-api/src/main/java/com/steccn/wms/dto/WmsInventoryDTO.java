package com.steccn.wms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/12/9
 */
@Data
@ApiModel("仓储库存DTO")
public class WmsInventoryDTO {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("物料id")
    private String materialId;

    @ApiModelProperty("数量")
    private BigDecimal quantity;

    @ApiModelProperty("原始数量")
    private BigDecimal originalQuantity;

    @ApiModelProperty("箱号")
    private String caseNo;


}
