package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author yequan
 */
@Data
@ApiModel("mess系统物料库存系统")
public class MessMaterialStorageDTO implements Serializable {

    @ApiModelProperty("物料")
    private Long materialId;

    @ApiModelProperty("数量")
    private BigDecimal quantity;
}
