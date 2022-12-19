package com.steccn.wms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/12/13
 */
@Data
@ApiModel
public class MaterialStorageInfoDTO {
    @ApiModelProperty("物料")
    private Long materialId;

    @ApiModelProperty("数量")
    private BigDecimal quantity;

    @ApiModelProperty("货架code")
    private List<String> shelfCodes;

    @ApiModelProperty("工位id")
    private Set<Integer> stationId;

}
