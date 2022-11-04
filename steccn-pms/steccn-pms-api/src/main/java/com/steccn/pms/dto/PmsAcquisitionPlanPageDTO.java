package com.steccn.pms.dto;

import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 领料计划分页DTO
 *
 * @author Mongknife
 * @date 2022/11/4
 */
@ApiModel("领料计划分页DTO")
@Data
public class PmsAcquisitionPlanPageDTO extends BasePageDTO {

    @ApiModelProperty("工位id")
    private List<Integer> station;

}
