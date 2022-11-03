package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;


/**
 * 领料单DTO
 *
 * @author Mongknife
 * @date 2022/11/3
 */
@Data
@ApiModel("领料单DTO")
public class MaterialRequisitionDTO {

    @ApiModelProperty("id")
    private Long id;

    @ApiModelProperty("类型id")
    private Long typeId;

    @ApiModelProperty("部门id")
    private Long departmentId;

    @ApiModelProperty("仓库id")
    private Long warehouseId;

    @ApiModelProperty("备注")
    private String note;

    @ApiModelProperty("领料单详情")
    private List<MaterialRequisitionInfoDTO> list;
}
