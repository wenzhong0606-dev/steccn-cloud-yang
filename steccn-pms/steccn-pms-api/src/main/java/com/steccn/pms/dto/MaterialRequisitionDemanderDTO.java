package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel("领用单明细需求人dto")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MaterialRequisitionDemanderDTO {


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

    /**
     * 需求人岗位
     */
    private String demanderPost;
}
