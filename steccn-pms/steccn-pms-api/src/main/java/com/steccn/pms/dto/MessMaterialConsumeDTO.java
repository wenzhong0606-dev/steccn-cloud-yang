package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author yequan
 */
@Data
@ApiModel("物料库存信息")
public class MessMaterialConsumeDTO implements Serializable {

    @ApiModelProperty("库存id")
    private Long inventoryId;

    @ApiModelProperty("批次号")
    private String batchNo;

    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("可用数量")
    private BigDecimal availableQuantity;

    @ApiModelProperty("仓库code")
    private String warehouseCode;

    @ApiModelProperty("货架code")
    private String shelfCode;

    @ApiModelProperty("创建时间")
    private LocalDateTime createDate;
}
