package com.steccn.wms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@ApiModel("入库信息标识DTO")
public class WmsStorageReceiptFlagInfoDTO {

    @ApiModelProperty("箱号")
    private String caseNo;

    @ApiModelProperty("物料编码")
    private String materialCode;

    @ApiModelProperty("数量")
    private BigDecimal quantity;
}
