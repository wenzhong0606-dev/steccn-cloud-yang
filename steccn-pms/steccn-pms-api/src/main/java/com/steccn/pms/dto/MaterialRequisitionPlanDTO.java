package com.steccn.pms.dto;

import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 领料计划DTO
 *
 * @author Mongknife
 * @date 2022/11/11
 */
@ApiModel("领料计划DTO")
@Data
public class MaterialRequisitionPlanDTO extends BasePageDTO {


    @ApiModelProperty("工位id")
    private List<Integer> station;
}
