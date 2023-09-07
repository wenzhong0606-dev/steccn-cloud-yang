package com.steccn.wms.dto;

import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 库存分页DTO
 *
 * @author Mongknife
 * @date 2022/12/9
 */
@Data
@ApiModel("库存分页DTO")
public class WmsInventoryPageDTO  extends BasePageDTO {

    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("物料编码")
    private String materialCode;

    @ApiModelProperty("箱号")
    private String caseNo;

    @ApiModelProperty("物料规格")
    private String specification;

    @ApiModelProperty("批次")
    private String batch;

    @ApiModelProperty("仓库")
    private Long warehouseId;

    @ApiModelProperty("货架")
    private String shelfName;

    @ApiModelProperty("库存id")
    private Long inventoryId;
}
