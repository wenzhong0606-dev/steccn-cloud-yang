package com.steccn.pms.dto;

import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 物料需求计划分页DTO
 *
 * @author Mongknife
 * @date 2022/11/4
 */
@Data
@ApiModel("物料需求计划分页DTO")
public class PmsMrpPageDTO extends BasePageDTO {

    @ApiModelProperty("物料名称")
    private String name;
}
