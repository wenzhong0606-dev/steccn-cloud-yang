package com.steccn.wms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * @author yequan
 */
@Data
@ApiModel("物料库存系统")
public class MaterialStorageDTO implements Serializable {

    @ApiModelProperty("物料")
    private Long materialId;

    @ApiModelProperty("数量")
    private BigDecimal quantity;

    @ApiModelProperty("货架code")
    private List<String> shelfCodes;

    @ApiModelProperty("工位id")
    private Set<Integer> stationId;

}
