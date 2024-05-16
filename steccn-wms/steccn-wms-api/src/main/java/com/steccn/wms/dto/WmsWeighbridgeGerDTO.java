package com.steccn.wms.dto;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 地磅生成入库单DTO
 *
 * @author AndrewLuo
 * @date 2024/05/16
 */
@Data
public class WmsWeighbridgeGerDTO {
    @NotEmpty(message = "id不能为空")
    private List<Long> ids;

    @NotNull(message = "仓库id不能为空")
    private Integer warehouseId;

    @ApiModelProperty("货架id")
    @NotNull(message = "货架id不能为空")
    private Integer shelfId;

    @ApiModelProperty("货架编码")
    @NotNull(message = "货架编码不能为空")
    private String shelfCode;

    @ApiModelProperty("货架名称")
    @NotNull(message = "货架名称不能为空")
    private String shelfName;
}
