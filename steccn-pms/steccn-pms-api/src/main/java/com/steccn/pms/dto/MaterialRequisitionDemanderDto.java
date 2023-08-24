package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
@ApiModel("领用单明细需求人dto")
@Data
public class MaterialRequisitionDemanderDto {


    /**
     * 需求人id
     */
    @ApiModelProperty("需求人id")
    private Long demanderId;

    /**
     * 需求人名称
     */
    @ApiModelProperty("需求人名称")
    private String demanderName;
}
