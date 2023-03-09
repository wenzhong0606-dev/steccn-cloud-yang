package com.steccn.wms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/12/13
 */
@Data
@ApiModel
public class WmsStockInventoryDTO {
    @ApiModelProperty("库存id")
    private Long inventoryId;

    @ApiModelProperty("批次号")
    private String batchNo;

    @ApiModelProperty("箱号")
    private String caseNo;

    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("物料编码")
    private String materialCode;

    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("可用数量")
    private BigDecimal availableQuantity;

    @ApiModelProperty("实际数量")
    private BigDecimal quantity;

    @ApiModelProperty("总数量")
    private BigDecimal totalQuantity;

//    @ApiModelProperty("仓库code")
//    private String warehouseCode;
//
//    @ApiModelProperty("货架code")
//    private String shelfCode;

    @ApiModelProperty("仓库id")
    private Integer warehouseId;

    @ApiModelProperty("仓库名称")
    private String warehouseName;

    @ApiModelProperty("货架id")
    private Integer  shelfId;

    @ApiModelProperty("货架名称")
    private String shelfName;

    @ApiModelProperty("创建时间")
    private LocalDateTime createDate;

    @ApiModelProperty("打包时间")
    private LocalDateTime packTime;

    @ApiModelProperty("单位")
    private Long unit;

    @ApiModelProperty("单位名称")
    private String unitName;
}
