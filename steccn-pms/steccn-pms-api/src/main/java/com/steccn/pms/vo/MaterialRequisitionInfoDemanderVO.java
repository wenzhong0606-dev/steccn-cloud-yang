package com.steccn.pms.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("领料单明细dto")
public class MaterialRequisitionInfoDemanderVO {

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
