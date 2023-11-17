package com.steccn.wms.dto;

import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 库存分页DTO
 *
 * @author Mongknife
 * @date 2022/12/9
 */
@Data
@ApiModel("库存流水分页DTO")
public class WmsInventoryRecordPageDTO extends BasePageDTO {


    @ApiModelProperty("单据编号")
    private String code;

    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("仓库")
    private Long warehouseId;

    @ApiModelProperty("货架id")
    private Long shelfId;

    @ApiModelProperty("变动类型")
    private Long typeId;

    @ApiModelProperty("变动筛选时间-开始")
    private LocalDateTime startTime;

    @ApiModelProperty("变动筛选时间-结束")
    private LocalDateTime endTime;
}
