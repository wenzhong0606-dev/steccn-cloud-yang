package com.steccn.pms.dto;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;


@Data
@ApiModel("扫描单明细dto")
public class ScanInfoDto {

    @ApiModelProperty("物料id")
    @NotNull(message = "物料id不能为空")
    private Long materialId;

    @ApiModelProperty("物料名称")
    @NotNull(message = "物料名称不能为空")
    private String materialName;

    @ApiModelProperty("物料编码")
    @NotNull(message = "物料编码不能为空")
    private String materialCode;

    @ApiModelProperty("箱号")
    @NotNull(message = "箱号不能为空")
    private String caseNo;

    @ApiModelProperty("批次")
    @NotNull(message = "批次不能为空")
    private String batch;

    @ApiModelProperty("仓库id")
    @NotNull(message = "仓库id不能为空")
    private Integer warehouseId;

    @ApiModelProperty("货架")
    @NotNull(message = "货架id不能为空")
    private Integer shelfId;

    @ApiModelProperty("货架名称")
    private String shelfName;

    @ApiModelProperty("货架编码")
    private String shelfCode;

    @ApiModelProperty("库存id")
    @NotNull(message = "库存id不能为空")
    private Long inventoryId;

    @ApiModelProperty("数量")
    @NotNull(message = "数量不能为空")
    private BigDecimal quantity;

    @ApiModelProperty("单位")
    private Long unitId;

    @ApiModelProperty("需求货架id")
    private Integer demandShelfId;

    @ApiModelProperty("需求货架名称")
    private String demandShelfName;


    @ApiModelProperty("需求货架编码")
    private String demandShelfCode;
}
