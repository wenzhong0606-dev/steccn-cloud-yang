package com.steccn.platform.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author chenhuan
 * @date 2022/11/2 17:24
 */
@Data
public class SysStationWarehouseDTO {

    private Long id;

    @ApiModelProperty("货架id")
    private Long shelfId;

    @ApiModelProperty("工位id")
    private Long stationId;

    @ApiModelProperty("仓库id")
    private Long warehouseId;

    @ApiModelProperty("备注")
    private String note;
}
